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
 * Basic implementation of the AlarmObject interface.
 *
 * @author Aaron Hansen
 */
public class AbstractAlarmObject implements AlarmObject, AlarmConstants {

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

    public AbstractAlarmObject(AlarmObject parent) {
        this.parent = parent;
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
     * A convenience that first looks for the config, and if not found, tries for
     * the attribute.
     */
    public Value getProperty(String name) {
        Value ret = node.getConfig(name);
        if (ret == null) {
            ret = node.getRoConfig(name);
        }
        if (ret == null) {
            ret = node.getAttribute(name);
        }
        return ret;
    }

    @Override public void init(Node node) {
        if (node == null) {
            throw new NullPointerException("SDK node cannot be null.");
        }
        if (this.node != null) {
            throw new IllegalStateException("SDK node already set.");
        }
        this.node = node;
        node.setRoConfig(JAVA_TYPE, new Value(getClass().getName()));
        AlarmObject alarmObject;
        if ((node != null) && node.getHasChildren()) {
            Map<String,Node> kids = node.getChildren();
            if (kids != null) {
                for (Node kid : kids.values()) {
                    alarmObject = AlarmUtil.tryCreateAlarmObject(kid);
                    if (alarmObject != null) {
                        addChild(alarmObject);
                    }
                }
            }
        } else {
            System.out.println(getClass().getName());//XXX
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
        if (!isEnabled() || isSteady()) {
            return false;
        }
        /*
        AlarmObject parent = getParent();
        if (parent instanceof AbstractAlarmObject) {
            if (!((AbstractAlarmObject)parent).isValid()) {
                return false;
            }
        }
        */
        return true;
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
            Node parent = getNode().getParent();
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
     * Calls doStart on this, then starts all children.
     * <p/>{@inheritDoc}
     */
    @Override public final void start() {
        started = true;
        doStart();
        for (AlarmObject kid : children) {
            kid.start();
        }
    }

    /**
     * Calls doSteady on this, then steadies all children.
     * <p/>{@inheritDoc}
     */
    @Override public final void steady() {
        steady = true;
        doSteady();
        for (AlarmObject kid : children) {
            kid.steady();
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
        for (AlarmObject kid : children) {
            kid.stop();
        }
    }

} //class
