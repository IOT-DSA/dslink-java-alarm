package org.dsa.iot.alarm;

public enum AlarmFilter {

    ALARM,
    NORMAL,
    ANY;

    public static AlarmFilter getMode(String arg) {
        if (AlarmConstants.ALARM.equalsIgnoreCase(arg)) {
            return ALARM;
        }
        if (AlarmConstants.NORMAL.equalsIgnoreCase(arg)) {
            return NORMAL;
        }
        return ANY;
    }
}
