/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * Basic implementation of the AlarmObject interface.  Subclasses are not required to
 * implement anything, this class simply provides callbacks while managing the lifecycle
 * of the instance.
 *
 * @author Aaron Hansen
 */
public abstract class AbstractAlarmObject implements AlarmObject, AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ArrayList<AlarmObject> children;
    private Node node;
    private AlarmObject parent;
    private AlarmService service;
    private boolean started = false;
    private boolean steady = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public synchronized void addChild(AlarmObject child) {
        AlarmUtil.logTrace("Add " + child.getNode().getPath());
        if (children == null) {
            children = new ArrayList<>();
        }
        child.setParent(this);
        children.add(child);
        if (started) {
            child.start();
        }
        if (steady) {
            child.steady();
        }
    }

    /**
     * A convenience for adding a delete action that will call deleteSelf().
     *
     * @param actionName Node name for the action.
     */
    public void addDeleteAction(String actionName) {
        Action action = new Action(Permission.WRITE, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                deleteSelf();
            }
        });
        node.createChild(actionName, false).setSerializable(false).setAction(action).build();
    }

    @Override
    public int childCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Convenience for delete actions.
     */
    public void deleteSelf() {
        ArrayList<UUID> toClose = new ArrayList<UUID>();
        getOpenUUIDs(toClose);
        for (UUID id : toClose) {
            Alarming.getProvider().acknowledge(id, "DELETED");
            Alarming.getProvider().returnToNormal(id);
        }
        getParent().removeChild(this);
    }

    @Override
    public AlarmObject getChild(int index) {
        if (children == null) {
            return null;
        }
        return children.get(index);
    }

    /**
     * A convenience that looks for the readonly config and if not found, checks
     * for the writable config.
     *
     * @return Possibly null.
     */
    public Value getConfig(String name) {
        Value value = node.getRoConfig(name);
        if (value == null) {
            value = node.getConfig(name);
        }
        return value;
    }

    public String getDBPassword() {
        return String.valueOf(node.getPassword());
    }

    @Override
    public int getHandle() {
        Value value = getConfig(HANDLE);
        if (value == null) {
            return 0;
        }
        return value.getNumber().intValue();
    }

    /**
     * The SDK node this object represents.
     */
    public Node getNode() {
        return this.node;
    }

    @Override
    public void getOpenUUIDs(ArrayList<UUID> uuidList) {
        if (children != null) {
            for (AlarmObject child : children) {
                child.getOpenUUIDs(uuidList);
            }
        }
    }

    @Override
    public AlarmObject getParent() {
        return parent;
    }

    /**
     * The path of the underlying node.
     */
    public String getPath() {
        return node.getPath();
    }

    /**
     * Returns the value of the named child of the inner node.
     *
     * @return Possibly null.
     */
    public Value getProperty(String name) {
        Node child = node.getChild(name, false);
        if (child == null) {
            return null;
        }
        return child.getValue();
    }

    /**
     * The alarm service, or null if this isn't mounted.
     */
    public AlarmService getService() {
        if (service == null) {
            AlarmObject tmp = getParent();
            while (tmp != null) {
                if (tmp instanceof AlarmService) {
                    service = (AlarmService) tmp;
                    break;
                }
                tmp = tmp.getParent();
            }
        }
        return service;
    }

    /**
     * Whether or not the inner node has a child node with the given name.
     */
    public boolean hasProperty(String name) {
        return node.getChild(name, false) != null;
    }

    @Override
    public void init(Node node) {
        if (node == null) {
            throw new NullPointerException("SDK node cannot be null.");
        }
        if (this.node == node) {
            return;
        }
        if (this.node != null) {
            throw new IllegalStateException("SDK node already set.");
        }
        this.node = node;
        node.setRoConfig(JAVA_TYPE, new Value(getClass().getName()));
        initActions();
        initData();
        AlarmObject alarmObject;
        Map<String, Node> children = node.getChildren();
        if (children != null) {
            for (Node child : children.values()) {
                alarmObject = AlarmUtil.tryCreateAlarmObject(child);
                if (alarmObject != null) {
                    addChild(alarmObject);
                }
            }
        }
    }

    public void initDBPassword(String str) {
        if (node.getPassword() == null) {
            setDBPassword(str);
        }
    }

    /**
     * Adds a child value node to the inner node if it is not already present.  This
     * should be called during startup no matter what, it will register with the child
     * listener for value change callbacks, which get routed to onPropertyChange.
     *
     * @param name  Must be unique.
     * @param value Only used if the property does not exist.
     * @return The child value node.
     */
    public Node initProperty(String name, Value value) {
        if (value == null) {
            return initProperty(name, null, null);
        }
        return initProperty(name, value.getType(), value);
    }

    /**
     * Adds a child value node to the inner node if it is not already present.  This
     * should be called during startup no matter what, it will register with the child
     * nodes listener for value change callbacks, which get routed to onPropertyChange.
     *
     * @param name  Must be unique.
     * @param type  Only used if the property does not exist.
     * @param value Only used if the property does not exist.
     * @return The child value node.
     */
    public Node initProperty(String name, ValueType type, Value value) {
        Node child = node.getChild(name, false);
        if (child == null) {
            child = node.createChild(name, false).setSerializable(true).build();
            child.setValueType(type);
            child.setValue(value, true);
        }
        child.setHasChildren(false);
        final Node tmp = child;
        child.getListener().setValueHandler(new Handler<ValuePair>() {
            @Override
            public void handle(ValuePair event) {
                onPropertyChange(tmp, event);
            }
        });
        return child;
    }

    /**
     * Returns the value of the enabled property.  If there is no enabled
     * property, will return true;
     */
    public boolean isEnabled() {
        Value val = getProperty(ENABLED);
        if (val == null) {
            return true;
        }
        return val.getBool().booleanValue();
    }

    @Override
    public final boolean isSteady() {
        return steady;
    }

    /**
     * A convenience for isEnabled() && isSteady()
     */
    public boolean isValid() {
        return isEnabled() && isSteady();
    }

    /**
     * A convenience that creates and adds a new AlarmObject as well as the corresponding
     * persistent SDK Node.
     *
     * @param name            The name of the SDK node.
     * @param alarmObjectType The alarm object class.
     * @return The newly created and added child.
     * @throws IllegalArgumentException If a child with the same name already exists.
     * @throws RuntimeException         Wrapping any other non-runtime exceptions.
     */
    public AlarmObject newChild(String name, Class alarmObjectType) {
        AlarmObject ret = null;
        try {
            Node parent = getNode();
            Node node = parent.getChild(name, true);
            if (node != null) {
                throw new IllegalArgumentException("Name already in use: " + name);
            }
            node = parent.createChild(name, true).setSerializable(true).build();
            ret = (AlarmObject) alarmObjectType.newInstance();
            ret.init(node);
            addChild(ret);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return ret;
    }

    @Override
    public synchronized void removeChild(AlarmObject child) {
        AlarmUtil.logTrace("Remove " + child.getNode().getPath());
        if (child.getParent() != this) {
            throw new IllegalStateException("Child not parented by this object");
        }
        try {
            child.removed();
        } catch (Exception x) {
            AlarmUtil.logError("Remove " + child.getNode().getPath(), x);
        }
        if (child.isSteady()) {
            child.stop();
        }
        child.setParent(null);
        children.remove(child);
        Node tmp = child.getNode();
        if (tmp != null) {
            node.removeChild(tmp, false);
        }
    }

    /**
     * Will remove all descendant alarm objects thus they will get the removed callback
     * as well.
     * <p/>{@inheritDoc}
     */
    @Override
    public void removed() {
        removeAllDescendants();
    }

    public void setDBPassword(String str) {
        node.setPassword(str.toCharArray());
    }

    @Override
    public void setParent(AlarmObject parent) {
        this.parent = parent;
    }

    /**
     * Sets the value of a named child of the inner node.
     *
     * @param name  The name of the child node.
     * @param value The new value.
     * @return The old value.
     */
    public Value setProperty(String name, Value value) {
        Node child = node.getChild(name, false);
        Value ret = child.getValue();
        child.setValue(value, true);
        return ret;
    }

    /**
     * Registers the instance with the service, calls doStart on this, then starts all
     * children.
     * <p/>{@inheritDoc}
     */
    @Override
    public final void start() {
        started = true;
        AlarmService svc = getService();
        if (svc != null) {
            svc.register(this);
        }
        doStart();
        if (children != null) {
            for (AlarmObject child : children) {
                child.start();
            }
        }
    }

    /**
     * Calls doSteady on this, then steadies all children.
     * <p/>{@inheritDoc}
     */
    @Override
    public final void steady() {
        steady = true;
        doSteady();
        if (children != null) {
            for (AlarmObject child : children) {
                child.steady();
            }
        }
    }

    /**
     * Calls doStop on this, stops all children, then unregisters with the service.
     * <p/>{@inheritDoc}
     */
    @Override
    public final void stop() {
        try {
            started = false;
            steady = false;
            doStop();
            if (children != null) {
                for (AlarmObject child : children) {
                    child.stop();
                }
            }
        } finally {
            AlarmService svc = getService();
            if (svc != null) {
                svc.unregister(this);
            }
        }
    }

    /**
     * A convenience, throws an IllegalStateException if not steady.
     */
    protected final void checkSteady() {
        if (!steady) {
            throw new IllegalStateException("Not steady");
        }
    }

    /**
     * Override point, called by the start().  Started will be true, steady will be false.
     */
    protected void doStart() {
    }

    /**
     * Override point, called by the steady().  Started and steady will be true.
     */
    protected void doSteady() {
    }

    /**
     * Override point, called by the stop().  Started and steady will be false.
     */
    protected void doStop() {
    }

    /**
     * Subclass hook for adding actions, called by init(node) after the subtree has been
     * loaded.
     */
    protected void initActions() {
    }

    /**
     * Only adds an attribute that isn't already present, otherwise does nothing.
     *
     * @param name  The name of the attribute.
     * @param value Only used when the attribute is not present.
     */
    protected void initAttribute(String name, Value value) {
        Value tmp = node.getAttribute(name);
        if (tmp != null) {
            return;
        }
        node.setAttribute(name, value);
    }

    /**
     * Only adds a config that isn't already present, otherwise does nothing.
     *
     * @param name     The name of the config.
     * @param value    Only used when the config is not present.
     * @param readOnly Whether or not the config is writable or not.
     */
    protected void initConfig(String name, Value value, boolean readOnly) {
        Value tmp = getConfig(name);
        if (tmp != null) {
            return;
        }
        if (readOnly) {
            node.setRoConfig(name, value);
        } else {
            node.setConfig(name, value);
        }
    }

    /**
     * Subclass hook for initializing data on the inner node, called by init(node) after
     * the subtree has been loaded.
     */
    protected void initData() {
    }

    /**
     * Callback for property changes (value changes in child nodes).  The property
     * must have been calling initProperty.
     */
    protected void onPropertyChange(Node child, ValuePair valuePair) {
    }

    /**
     * A convenience that calls removeChild for all direct descendants.
     */
    protected void removeAllDescendants() {
        for (int i = childCount(); --i >= 0; ) {
            removeChild(getChild(i));
        }
    }

}
