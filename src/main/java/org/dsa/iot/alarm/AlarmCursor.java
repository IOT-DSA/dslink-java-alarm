/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

/**
 * A cursor of alarm records.  Initially positioned before the first record, properties
 * should only accessed after a call to next() returns true.
 *
 * @author Aaron Hansen
 */
public abstract class AlarmCursor extends AlarmRecord {

    /**
     * Call this if terminating use of the cursor before next returns false.
     */
    public abstract void close();

    /**
     * Returns true if cursor advances to the next record.  The record fields should only
     * accessed after this method returns true.
     */
    public abstract boolean next();

    /**
     * Must be called before the first call to next().  Advances the cursor to the
     * appropriate start of page, and only returns pageSize number of rows.
     */
    public abstract void setPaging(int page, int pageSize);

    /**
     * Calls close().
     */
    @Override
    protected void finalize() {
        close();
    }

}
