/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

/**
 * Enum for the possible alarm state.
 *
 * @author Aaron Hansen
 */
public enum AlarmState {

    ALERT,

    FAULT,

    OFFNORMAL,

    NORMAL;

    /**
     * Returns the enum for the given state, case-independent.
     */
    public static AlarmState decode(String state) {
        if (state.equalsIgnoreCase(AlarmConstants.NORMAL)) {
            return NORMAL;
        }
        if (state.equalsIgnoreCase(AlarmConstants.ALERT)) {
            return ALERT;
        }
        if (state.equalsIgnoreCase(AlarmConstants.FAULT)) {
            return FAULT;
        }
        if (state.equalsIgnoreCase(AlarmConstants.OFFNORMAL)) {
            return OFFNORMAL;
        }
        throw new IllegalArgumentException("Unknown alarm state: " + state);
    }

    /**
     * Returns the string for the given enum.
     */
    public static String encode(AlarmState state) {
        if (state == NORMAL) {
            return AlarmConstants.NORMAL;
        }
        if (state == ALERT) {
            return AlarmConstants.ALERT;
        }
        if (state == FAULT) {
            return AlarmConstants.FAULT;
        }
        if (state == OFFNORMAL) {
            return AlarmConstants.OFFNORMAL;
        }
        throw new IllegalArgumentException("Unexpected alarm state enum: " + state);
    }

}


