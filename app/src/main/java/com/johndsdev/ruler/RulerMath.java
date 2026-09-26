package com.johndsdev.ruler;

/** Units and calibration math without dependencies on the Android framework. */
public final class RulerMath {
    public static final float CARD_LONG_SIDE_MM = 85.60f;
    private RulerMath() { }

    public static float pixelsPerMm(float displayDpi, float scale) {
        if (displayDpi <= 0f || scale <= 0f) {
            throw new IllegalArgumentException("Display DPI and scale must be positive");
        }
        return (displayDpi / 25.4f) * scale;
    }

    public static float millimetersBetween(float firstPx, float secondPx, float pxPerMm) {
        if (pxPerMm <= 0f) throw new IllegalArgumentException("pxPerMm must be positive");
        return Math.abs(secondPx - firstPx) / pxPerMm;
    }

    public static float inchesFromMm(float mm) {
        return mm / 25.4f;
    }

    public static float calibrationFromCard(float cardLengthPx, float displayDpi) {
        return cardLengthPx / (CARD_LONG_SIDE_MM * pixelsPerMm(displayDpi, 1f));
    }
}
