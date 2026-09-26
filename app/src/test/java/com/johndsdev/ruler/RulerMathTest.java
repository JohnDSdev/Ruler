package com.johndsdev.ruler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Test;

public class RulerMathTest {
    @Test public void metricIsTrueSizeAt254Dpi() {
        assertEquals(10f, RulerMath.pixelsPerMm(254f, 1f), 0.0001f);
        assertEquals(25.4f, RulerMath.millimetersBetween(0f, 254f, 10f), 0.0001f);
    }

    @Test public void negativeDraggingMeasuresPositiveLength() {
        assertEquals(20f, RulerMath.millimetersBetween(320f, 120f, 10f), 0.0001f);
    }

    @Test public void calibrationMatchesStandardCard() {
        assertEquals(1.1f, RulerMath.calibrationFromCard(941.6f, 254f), 0.0001f);
    }

    @Test public void inchesConvertExactly() {
        assertEquals(2f, RulerMath.inchesFromMm(50.8f), 0.0001f);
    }

    @Test public void invalidDpiFails() {
        assertThrows(IllegalArgumentException.class, () -> RulerMath.pixelsPerMm(0f, 1f));
    }
}
