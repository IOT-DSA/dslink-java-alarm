package org.dsa.iot.alarm;

public enum AckFilter {

    ACKED,
    UNACKED,
    ANY;

    public static AckFilter getMode(String arg) {
        if (AlarmConstants.ACKED.equalsIgnoreCase(arg)) {
            return ACKED;
        }
        if (AlarmConstants.UNACKED.equalsIgnoreCase(arg)) {
            return UNACKED;
        }
        return ANY;
    }
}
