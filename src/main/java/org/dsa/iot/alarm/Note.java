/* THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD
 * TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN
 * NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER
 * IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.dsa.iot.alarm;

import java.util.UUID;

/**
 * Simple note implementation.
 *
 * @author Aaron Hansen
 */
public class Note {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String text;
    private long timestamp;
    private String user;
    private UUID uuid;

    ///////////////////////////////////////////////////////////////////////////
    // Construction
    ///////////////////////////////////////////////////////////////////////////

    public Note() {
    }

    public Note(UUID uuid) {
        this.uuid = uuid;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Copies the internal state of the given note.
     */
    public void copy(Note note) {
        text = note.text;
        timestamp = note.timestamp;
        user = note.user;
        uuid = note.uuid;
    }

    /**
     * The note itself.
     */
    public String getText() {
        return text;
    }

    /**
     * The timestamp the note was added.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * The user or entity who added the current note.
     */
    public String getUser() {
        return user;
    }

    /**
     * The UUID of the correspondning alarm record, will not change with calls to next().
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * The note itself, returns this.
     */
    public Note setText(String arg) {
        text = arg;
        return this;
    }

    /**
     * The timestamp the note was added, returns this.
     */
    public Note setTimestamp(long arg) {
        timestamp = arg;
        return this;
    }

    /**
     * The user or entity who added the current note.
     */
    public Note setUser(String arg) {
        user = arg;
        return this;
    }

    /**
     * The UUID of the corresponding alarm record, must not change with calls to next(),
     * returns this.
     */
    public Note setUUID(UUID arg) {
        uuid = arg;
        return this;
    }

} //class
