/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;

/**
 * @author Aaron Hansen
 */
public class AlarmLinkHandler extends DSLinkHandler implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AlarmService alarmService;
    private Node superRoot;
    private DSLink requesterLink;
    private DSLink responderLink;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the link passed to onRequesterConnected
     */
    DSLink getRequesterLink() {
        return requesterLink;
    }

    /**
     * Returns the link passed to onResponderConnected
     */
    DSLink getResponderLink() {
        return responderLink;
    }

    /**
     * Returns true
     */
    @Override
    public boolean isRequester() {
        return true;
    }

    /**
     * Returns true
     */
    @Override
    public boolean isResponder() {
        return true;
    }

    /**
     * Captures the reference to the requester link.
     */
    @Override
    public void onRequesterConnected(DSLink link) {
        AlarmUtil.logInfo("Requester connected");
        requesterLink = link;
    }

    /**
     * Cleans up everything related to the requester.
     */
    @Override
    public void onRequesterDisconnected(DSLink link) {
        AlarmUtil.logInfo("Requester disconnected");
        requesterLink = null;
    }

    /**
     * Captures the reference to the responder link and creates the alarm service
     * hierarchy.
     */
    @Override
    public void onResponderConnected(DSLink link) {
        AlarmUtil.logInfo("Responder connected");
        responderLink = link;
        Node superRoot = responderLink.getNodeManager().getSuperRoot();
        if (superRoot != this.superRoot) {
            if (alarmService != null) {
                alarmService.stop();
                AlarmUtil.logWarning("AlarmService stopped");
                alarmService = null;
            }
            this.superRoot = superRoot;
            Node serviceNode = superRoot.createChild("Alarm Service", false).setSerializable(
                    true).build();
            alarmService = (AlarmService) AlarmUtil.tryCreateAlarmObject(serviceNode);
            if (alarmService == null) {
                alarmService = Alarming.getProvider().newAlarmService();
                alarmService.init(serviceNode);
            }
            alarmService.setLinkHandler(this);
            alarmService.start();
            AlarmUtil.logInfo("AlarmService started");
            alarmService.steady();
            AlarmUtil.logInfo("AlarmService stable");
        }
    }

    /**
     * Cleans up everything related to the responder.
     */
    @Override
    public void onResponderDisconnected(DSLink link) {
        AlarmUtil.logInfo("Responder disconnected");
        responderLink = null;
    }

    @Override
    public void stop() {
        AlarmUtil.logWarning("AlarmLinkHandler stopping");
        if (alarmService != null) {
            alarmService.stop();
        }
        alarmService = null;
        superRoot = null;
        super.stop();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
