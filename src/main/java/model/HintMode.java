package model;

public enum HintMode {
    OFF, ON, SMART;

    public static HintMode of(String mode) {
        try {
            return HintMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return HintMode.OFF;
        }
    }
}
