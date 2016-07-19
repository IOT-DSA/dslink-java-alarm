/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.*;

/**
 * A cursor for the notes of a specific alarm record.  Initially positioned before the
 * first note, fields should only accessed after a call to next() returns true.  Notes
 * should be returned in chronological order starting with the earliest note.
 *
 * @author Aaron Hansen
 */
public abstract class NoteCursor extends Note {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////////////////////////////////////

    public NoteCursor() {}

    public NoteCursor(UUID uuid) {
        super(uuid);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Call this if terminating use of the cursor before next returns false.
     */
    public abstract void close();

    /**
     * The other fields (except UUID) should only accessed after this method returns
     * true.
     */
    public abstract boolean next();


} //class
