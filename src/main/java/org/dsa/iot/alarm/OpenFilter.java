package org.dsa.iot.alarm;

public enum OpenFilter {

    OPEN,
    CLOSED,
    ANY;

    public static OpenFilter getMode(String arg) {
        if (AlarmConstants.OPEN.equalsIgnoreCase(arg)) {
            return OPEN;
        }
        if (AlarmConstants.CLOSED.equalsIgnoreCase(arg)) {
            return CLOSED;
        }
        return ANY;
    }
}
