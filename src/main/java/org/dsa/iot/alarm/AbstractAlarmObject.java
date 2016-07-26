/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.*;
import org.dsa.iot.dslink.node.actions.*;
import org.dsa.iot.dslink.node.value.*;
import java.util.*;

/**
 * Basic implementation of the AlarmObject interface.  Subclasses are not required to
 * implement anything, this class simply provides a bunch of callbacks while managing
 * the lifecycle of the instance.
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
    private boolean started = false;
    private boolean steady = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AbstractAlarmObject() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override public synchronized void addChild(AlarmObject child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        child.setParent(this);
        children.add(child);
        if (started)
            child.start();
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
        Action action = new Action(Permission.WRITE, p -> deleteSelf());
        node.createChild(actionName).setSerializable(false).setAction(action).build();
    }

    /**
     * A convenience, throws an IllegalStateException if not steady.
     */
    protected final void checkSteady() {
        if (!steady) {
            throw new IllegalStateException("Not steady");
        }
    }

    @Override public int childCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Convenience for delete actions.
     */
    public void deleteSelf() {
        getParent().removeChild(this);
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

    @Override public AlarmObject getChild(int index) {
        if (children == null) {
            return null;
        }
        return children.get(index);
    }

    /**
     * The SDK node this object represents.
     */
    public Node getNode() {
        return this.node;
    }

    @Override public AlarmObject getParent() {
        return parent;
    }

    /**
     * Returns the value of the named child of the inner node.
     *
     * @return Possibly null.
     */
    public Value getProperty(String name) {
        Node child = node.getChild(name);
        if (child == null) {
            return null;
        }
        return child.getValue();
    }

    /**
     * Whether or not the inner node has a child node with the given name.
     */
    public boolean hasProperty(String name) {
        return node.getChild(name) != null;
    }

    @Override public void init(Node node) {
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
        AlarmObject alarmObject;
        if (node != null) {
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
        initProperties();
        initActions();
    }

    /**
     * Subclass hook for adding actions, called by init(node) after the subtree has been
     * loaded.
     */
    protected void initActions() {
    }

    /**
     * Subclass hook for adding configs and attributes, called by init(node) after the
     * subtree has been loaded.
     */
    protected void initProperties() {
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
            return initProperty(name, null, value);
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
        Node child = node.getChild(name);
        if (child == null) {
            child = node.createChild(name).setSerializable(true).build();
            child.setValueType(type);
            child.setValue(value);
        }
        child.setHasChildren(false);
        final Node tmp = child;
        child.getListener().setValueHandler(p -> onPropertyChange(tmp, p));
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

    @Override public final boolean isSteady() {
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
            Node node = parent.getChild(name);
            if (node != null) {
                throw new IllegalArgumentException("Name already in use: " + name);
            }
            node = parent.createChild(name).setSerializable(true).build();
            ret = (AlarmObject) alarmObjectType.newInstance();
            ret.init(node);
            addChild(ret);
        } catch (Exception x) {
            AlarmUtil.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Callback for property changes (value changes in child nodes).  The property
     * must have been calling initProperty.
     */
    protected void onPropertyChange(Node child, ValuePair valuePair) {
        System.out.println("Property change!"); //TODO - remove
    }

    /**
     * Will remove all descendant alarm objects thus they will get the removed callback
     * as well.
     * <p/>{@inheritDoc}
     */
    @Override public void removed() {
        removeAllDescendants();
        node.getParent().removeChild(node);
    }

    /**
     * A convenience that calls removeChild for all direct descendants.
     */
    protected void removeAllDescendants() {
        for (int i = childCount(); --i >= 0; ) {
            removeChild(getChild(i));
        }
    }

    @Override public synchronized void removeChild(AlarmObject child) {
        if (child.getParent() == this) {
            child.removed();
            if (child.isSteady()) {
                child.stop();
            }
            child.setParent(null);
            children.remove(child);
        } else {
            throw new IllegalStateException("Child not parented by this object");
        }
    }

    @Override public void setParent(AlarmObject parent) {
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
        Node child = node.getChild(name);
        Value ret = child.getValue();
        child.setValue(value);
        return ret;
    }

    /**
     * Calls doStart on this, then starts all children.
     * <p/>{@inheritDoc}
     */
    @Override public final void start() {
        started = true;
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
    @Override public final void steady() {
        steady = true;
        doSteady();
        if (children != null) {
            for (AlarmObject child : children) {
                child.steady();
            }
        }
    }

    /**
     * Calls doStop on this, then stops all children.
     * <p/>{@inheritDoc}
     */
    @Override public final void stop() {
        started = false;
        steady = false;
        doStop();
        if (children != null) {
            for (AlarmObject child : children) {
                child.stop();
            }
        }
    }

} //class
