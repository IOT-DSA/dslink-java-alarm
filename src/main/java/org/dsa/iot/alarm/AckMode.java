package org.dsa.iot.alarm;

public enum AckMode {

    ACKED,
    UNACKED,
    ANY;

    public static AckMode getMode(String arg) {
        if (AlarmConstants.ACKED.equalsIgnoreCase(arg)) {
            return ACKED;
        }
        if (AlarmConstants.UNACKED.equalsIgnoreCase(arg)) {
            return UNACKED;
        }
        return ANY;
    }
}
