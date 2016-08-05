/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.node.*;

/**
 * The alarm link is a hierarchy of AlarmObjects that proxy the SDK node hierarchy.
 * This interface is used to manage the lifecycle so that resources are properly
 * cleaned up.
 *
 * @author Aaron Hansen
 */
public interface AlarmObject {

    /**
     * Adds the child to the set of children of this node.  This method will call
     * start on the child if isSteady is true.
     *
     * @throws IllegalStateException If the child is already parented.
     */
    public void addChild(AlarmObject child);

    /**
     * The number of AlarmObject children.
     */
    public int childCount();

    /**
     * Returns the child at the given index.
     */
    public AlarmObject getChild(int i);

    /**
     * The handle assigned to the inner node when AlarmService.register is called.
     *
     * @return 0 if the object hasn't been assigned a handle.
     */
    public int getHandle();

    /**
     * The SDK node this object represents.
     */
    public Node getNode();

    /**
     * The parent node or null.
     */
    public AlarmObject getParent();

    /**
     * Sets the SDK node this object represents and can only be called once.  This
     * method will load the entire subtree of alarm objects for the corresponding
     * subtree of SDK nodes.  Will call initData and initActions on this object
     * before loading children.
     */
    public void init(Node node);

    /**
     * All object in the tree have been started.
     */
    public boolean isSteady();

    /**
     * Called when being removed from the parent, before being stopped and actually
     * removed.  Not automatically called on descendants of the object being removed.
     */
    public void removed();

    /**
     * Removes the child from this instance.  This method will call
     * stop on the child if it is steady.
     *
     * @throws IllegalStateException If the child is not parented by this instance.
     */
    public void removeChild(AlarmObject child);

    /**
     * The parent AlarmObject, to be called by addChild.
     */
    public void setParent(AlarmObject parent);

    /**
     * Called after start is called on the parent.  Will be called when added to an
     * already started parent, or when start is called on the parent.  Custom
     * implementations should call register on their associate alarm service.
     */
    public void start();

    /**
     * Called after start has been called on ever member of the tree in which this
     * object is mounted. Steady will be called on a parents before children.
     */
    public void steady();

    /**
     * Called upon removal from the parent, or the parent is stopped.  Customer
     * implementations should call unregister on their associate alarm service.
     */
    public void stop();

} //class
