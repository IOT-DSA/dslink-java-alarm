package org.dsa.iot.alarm;

import java.util.ArrayList;

/**
 * Parses a line of csv for the StringAlgorithm.
 *
 * @author Aaron Hansen
 */
class Csv {

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Trims, strips surrounding quotes and converts escaped quotes (double quotes).
     */
    public static String readCell(String s) {
        s = s.trim();
        if (s.indexOf('"') < 0) {
            return s;
        }
        int len = s.length() - 1;
        if (len > 1) {
            if ((s.charAt(0) == '"') && (s.charAt(len) == '"')) {
                s = s.substring(1, len);
            }
            s = s.replace("\"\"", "\"");
        }
        return s;
    }

    /**
     * Read a single row and put the columns into a list.
     */
    public static ArrayList<String> readRow(CharSequence row) {
        char delim = ',';
        ArrayList<String> ret = new ArrayList<>();
        int len = row.length();
        if (len == 0) {
            return ret;
        }
        StringBuilder buf = new StringBuilder();
        boolean inquote = false;
        char ch = 0;
        for (int i = 0; i < len; i++) {
            ch = row.charAt(i);
            if (ch == delim) {
                if (inquote) {
                    buf.append(ch);
                } else {
                    if ((delim == ' ') && (buf.length() == 0)) {
                        ;//do nothing
                    } else {
                        ret.add(readCell(buf.toString()));
                        buf.setLength(0);
                    }
                }
            } else if (ch == '"') {
                inquote = !inquote;
                buf.append(ch);
            } else {
                buf.append(ch);
            }
        }
        if (buf.length() > 0) {
            ret.add(readCell(buf.toString()));
        }
        return ret;
    }

}
