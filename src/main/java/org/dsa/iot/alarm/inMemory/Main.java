/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm.inMemory;

import org.dsa.iot.alarm.*;
import org.dsa.iot.dslink.*;

/**
 * Launches an In Memory alarm link.
 *
 * @author Aaron Hansen
 */
public class Main extends AlarmLinkHandler {

    // Yes, extending AlarmLinkHandler is messed up.  DSLinkFactory has an
    // issue...

    /**
     * Command line bootstrap.
     *
     * @param args Should supply --broker host/conn
     */
    public static void main(String[] args) {
        Alarming.setProvider(new InMemoryProvider());
        DSLinkFactory.start(args, new Main());
    }

}


