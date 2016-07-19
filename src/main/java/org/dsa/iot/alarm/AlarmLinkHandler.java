/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import org.dsa.iot.dslink.*;
import org.dsa.iot.dslink.node.*;
import org.slf4j.*;

/**
 *
 * @author Aaron Hansen
 */
public class AlarmLinkHandler extends DSLinkHandler implements AlarmConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmLinkHandler.class);

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

    public AlarmLinkHandler() {}

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
     * Cleans up everything related to the requester.
     */
    @Override
    public void onRequesterDisconnected(DSLink link) {
        LOGGER.info("Requester disconnected");
        requesterLink = null;
    }

    /**
     * Captures the reference to the requester link.
     */
    @Override
    public void onRequesterInitialized(DSLink link) {
        LOGGER.info("Requester initialized");
        requesterLink = link;
    }

    /**
     * Cleans up everything related to the responder.
     */
    @Override
    public void onResponderDisconnected(DSLink link) {
        responderLink = null;
    }

    /**
     * Captures the reference to the responder link and creates the alarm service
     * hierarchy.
     */
    @Override
    public void onResponderInitialized(DSLink link) {
        LOGGER.info("Responder initialized");
        responderLink = link;
        Node superRoot = responderLink.getNodeManager().getSuperRoot();
        if (superRoot != this.superRoot) {
            if (alarmService != null) {
                alarmService.stop();
                alarmService = null;
            }
            this.superRoot = superRoot;
            Node serviceNode = superRoot.createChild("Alarm Service")
                    .setSerializable(true)
                    .build();
            alarmService = (AlarmService) AlarmUtil.tryCreateAlarmObject(serviceNode);
            if (alarmService == null) {
                alarmService = Alarming.getProvider().newAlarmService();
                alarmService.init(serviceNode);
                alarmService.start();
                alarmService.steady();
            }
        }
    }

    @Override
    public void stop() {
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
