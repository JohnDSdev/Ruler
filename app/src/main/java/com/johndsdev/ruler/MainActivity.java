package com.johndsdev.ruler;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(11, 20, 24);
    private static final int PANEL = Color.rgb(27, 43, 49);
    private static final int WHITE = Color.rgb(238, 246, 244);
    private static final int MUTED = Color.rgb(166, 192, 192);
    private static final int MINT = Color.rgb(81, 239, 204);
    private static final int AMBER = Color.rgb(255, 184, 103);

    private float calibration = 1f;
    private boolean inchMode = false;
    private RulerView ruler;
    private Button unitsButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calibration = getPreferences(MODE_PRIVATE).getFloat("calibration", 1f);
        calibration = Math.max(0.5f, Math.min(1.5f, calibration));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immerse(getWindow());

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        ruler = new RulerView(this);
        root.addView(ruler, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setPadding(dp(6), dp(6), dp(6), dp(6));
        toolbar.setBackground(rounded(PANEL, 18, 0));

        unitsButton = button("CM");
        unitsButton.setOnClickListener(v -> {
            inchMode = !inchMode;
            unitsButton.setText(inchMode ? "IN" : "CM");
            ruler.invalidate();
        });
        toolbar.addView(unitsButton);

        Button calibrateButton = button("CALIBRATE");
        LinearLayout.LayoutParams buttonGap = new LinearLayout.LayoutParams(-2, dp(43));
        buttonGap.setMargins(dp(6), 0, 0, 0);
        calibrateButton.setOnClickListener(v -> openCalibration());
        toolbar.addView(calibrateButton, buttonGap);

        Button resetButton = button("RESET");
        LinearLayout.LayoutParams resetGap = new LinearLayout.LayoutParams(-2, dp(43));
        resetGap.setMargins(dp(6), 0, 0, 0);
        resetButton.setOnClickListener(v -> ruler.resetMarkers());
        toolbar.addView(resetButton, resetGap);

        FrameLayout.LayoutParams toolsLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        toolsLp.topMargin = dp(12);
        root.addView(toolbar, toolsLp);

        TextView instruction = text("DRAG THE A / B MARKERS TO MEASURE", 10, MUTED, true);
        instruction.setGravity(Gravity.CENTER);
        instruction.setPadding(dp(12), dp(8), dp(12), dp(8));
        instruction.setBackground(rounded(PANEL, 14, 0));
        FrameLayout.LayoutParams infoLp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        infoLp.bottomMargin = dp(12);
        root.addView(instruction, infoLp);

        setContentView(root);
    }

    private void immerse(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private int dp(float amount) {
        return (int) (amount * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float axisDpi(boolean horizontal) {
        float dpi = horizontal
                ? getResources().getDisplayMetrics().xdpi
                : getResources().getDisplayMetrics().ydpi;
        if (dpi < 100f || dpi > 1100f) {
            dpi = getResources().getDisplayMetrics().densityDpi;
        }
        return dpi;
    }

    private GradientDrawable rounded(int color, float radiusDp, int stroke) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radiusDp));
        if (stroke != 0) bg.setStroke(dp(1), stroke);
        return bg;
    }

    private TextView text(String content, float sp, int color, boolean heavy) {
        TextView t = new TextView(this);
        t.setText(content);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (heavy) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return t;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(12), 0, dp(12), 0);
        b.setBackground(rounded(Color.rgb(44, 64, 70), 12, Color.rgb(74, 98, 104)));
        b.setLayoutParams(new LinearLayout.LayoutParams(-2, dp(43)));
        return b;
    }

    private void openCalibration() {
        // Physical 85.60 mm is the ISO/IEC 7810 ID-1 long side.
        final int oldOrientation = getRequestedOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(32), dp(24), dp(24));
        page.setBackgroundColor(BG);

        TextView title = text("CALIBRATE", 24, WHITE, true);
        page.addView(title);
        TextView instructions = text(
                "hold a standard payment card against your screen. adjust the two orange marks until they match its LONG side (85.60 mm).",
                14, MUTED, false);
        instructions.setPadding(0, dp(10), 0, dp(6));
        page.addView(instructions);

        CalibrationPreview preview = new CalibrationPreview(this);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        previewLp.setMargins(0, dp(12), 0, dp(8));
        page.addView(preview, previewLp);

        TextView value = text("", 13, MINT, true);
        value.setGravity(Gravity.CENTER);
        page.addView(value);

        SeekBar slider = new SeekBar(this);
        slider.setMax(1000); // reported DPI x 0.5 to x 1.5, with 0.1% resolution
        slider.setProgress(Math.round((calibration - 0.5f) * 1000f));
        page.addView(slider, new LinearLayout.LayoutParams(-1, dp(50)));

        LinearLayout nudges = new LinearLayout(this);
        nudges.setGravity(Gravity.CENTER);
        Button minus = button("- 0.5%");
        Button plus = button("+ 0.5%");
        Button factory = button("DEFAULT");
        nudges.addView(minus);
        LinearLayout.LayoutParams nudged = new LinearLayout.LayoutParams(-2, dp(43));
        nudged.setMargins(dp(8), 0, 0, 0);
        nudges.addView(plus, nudged);
        LinearLayout.LayoutParams third = new LinearLayout.LayoutParams(-2, dp(43));
        third.setMargins(dp(8), 0, 0, 0);
        nudges.addView(factory, third);
        page.addView(nudges);

        Runnable redraw = () -> {
            float newScale = 0.5f + slider.getProgress() / 1000f;
            preview.setScale(newScale);
            value.setText(String.format(Locale.US, "DISPLAY SCALE: %.1f%%", newScale * 100f));
        };
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar b, int progress, boolean fromUser) { redraw.run(); }
            @Override public void onStartTrackingTouch(SeekBar b) { }
            @Override public void onStopTrackingTouch(SeekBar b) { }
        });
        minus.setOnClickListener(v -> slider.setProgress(Math.max(0, slider.getProgress() - 5)));
        plus.setOnClickListener(v -> slider.setProgress(Math.min(1000, slider.getProgress() + 5)));
        factory.setOnClickListener(v -> slider.setProgress(500));
        redraw.run();

        LinearLayout bottom = new LinearLayout(this);
        bottom.setPadding(0, dp(20), 0, 0);
        bottom.setGravity(Gravity.CENTER);
        Button cancel = button("CANCEL");
        cancel.setOnClickListener(v -> dialog.dismiss());
        Button save = button("SAVE CALIBRATION");
        save.setTextColor(BG);
        save.setBackground(rounded(MINT, 12, 0));
        save.setOnClickListener(v -> {
            calibration = 0.5f + slider.getProgress() / 1000f;
            getPreferences(MODE_PRIVATE).edit().putFloat("calibration", calibration).apply();
            ruler.invalidate();
            dialog.dismiss();
        });
        bottom.addView(cancel, new LinearLayout.LayoutParams(0, dp(48), 1f));
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(0, dp(48), 2f);
        saveLp.setMargins(dp(12), 0, 0, 0);
        bottom.addView(save, saveLp);
        page.addView(bottom);

        dialog.setContentView(page);
        dialog.setOnDismissListener(v -> setRequestedOrientation(oldOrientation));
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(-1, -1);
            immerse(w);
        }
    }

    private final class CalibrationPreview extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float scale = calibration;
        CalibrationPreview(Context context) { super(context); }
        void setScale(float value) { scale = value; invalidate(); }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float x = getWidth() * 0.5f;
            float centerY = getHeight() * 0.5f;
            float cardPx = RulerMath.CARD_LONG_SIDE_MM * RulerMath.pixelsPerMm(axisDpi(false), scale);
            float first = centerY - cardPx / 2f;
            float last = centerY + cardPx / 2f;

            p.setStrokeWidth(dp(2));
            p.setColor(Color.rgb(68, 88, 94));
            canvas.drawLine(x, 0, x, getHeight(), p);
            p.setStrokeWidth(dp(3));
            p.setColor(AMBER);
            canvas.drawLine(x, first, x, last, p);
            canvas.drawLine(x - dp(45), first, x + dp(45), first, p);
            canvas.drawLine(x - dp(45), last, x + dp(45), last, p);
            p.setTextSize(dp(14));
            p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(WHITE);
            canvas.drawText("85.60 mm", x, centerY - dp(12), p);
            p.setColor(MUTED);
            p.setTextSize(dp(11));
            canvas.drawText("top edge", x + dp(75), first + dp(4), p);
            canvas.drawText("bottom edge", x + dp(75), last + dp(4), p);
        }
    }

    private final class RulerView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float markerA = -1f;
        private float markerB = -1f;
        private int dragging = -1;

        RulerView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

        private boolean horizontal() { return getWidth() > getHeight(); }
        private float axisLength() { return horizontal() ? getWidth() : getHeight(); }
        private float pxPerMm() { return RulerMath.pixelsPerMm(axisDpi(horizontal()), calibration); }

        void resetMarkers() {
            if (getWidth() == 0 || getHeight() == 0) return;
            markerA = Math.min(axisLength() * .28f, 24f * pxPerMm());
            markerB = Math.min(axisLength() * .75f, 85f * pxPerMm());
            invalidate();
        }

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            resetMarkers();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float px = pxPerMm();
            if (markerA < 0f || markerB < 0f) resetMarkers();
            boolean sideways = horizontal();
            float len = axisLength();
            float stepMm = inchMode ? 25.4f / 16f : 1f;
            int ticks = (int) (len / (stepMm * px));
            for (int i = 0; i <= ticks; i++) {
                boolean major = i % (inchMode ? 16 : 10) == 0;
                boolean half = i % (inchMode ? 8 : 5) == 0;
                boolean quarter = inchMode && i % 4 == 0;
                float tickLen = dp(major ? 34 : half ? 24 : quarter ? 18 : 10);
                float pos = i * stepMm * px;
                p.setStrokeWidth(dp(major ? 1.5f : 0.85f));
                p.setColor(major ? WHITE : half || quarter ? MUTED : Color.rgb(91, 118, 123));
                if (sideways) {
                    canvas.drawLine(pos, 0, pos, tickLen, p);
                    canvas.drawLine(pos, getHeight(), pos, getHeight() - tickLen, p);
                } else {
                    canvas.drawLine(0, pos, tickLen, pos, p);
                    canvas.drawLine(getWidth(), pos, getWidth() - tickLen, pos, p);
                }
                if (major) {
                    String label = Integer.toString(i / (inchMode ? 16 : 10));
                    p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                    p.setTextSize(dp(12));
                    p.setColor(WHITE);
                    if (sideways) {
                        p.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(label, pos, dp(52), p);
                        canvas.drawText(label, pos, getHeight() - dp(42), p);
                    } else {
                        p.setTextAlign(Paint.Align.LEFT);
                        canvas.drawText(label, dp(43), pos + dp(4), p);
                        p.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText(label, getWidth() - dp(43), pos + dp(4), p);
                    }
                }
            }
            drawMarker(canvas, markerA, AMBER, "A");
            drawMarker(canvas, markerB, MINT, "B");
            drawMeasurement(canvas);
        }

        private void drawMarker(Canvas canvas, float where, int color, String name) {
            boolean sideways = horizontal();
            p.setColor(color);
            p.setStrokeWidth(dp(1.6f));
            if (sideways) {
                canvas.drawLine(where, 0, where, getHeight(), p);
            } else {
                canvas.drawLine(0, where, getWidth(), where, p);
            }
            float hx = sideways ? where : getWidth() * .73f;
            float hy = sideways ? getHeight() * .73f : where;
            p.setColor(color);
            canvas.drawCircle(hx, hy, dp(16), p);
            p.setColor(BG);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(dp(13));
            p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            canvas.drawText(name, hx, hy + dp(5), p);
        }

        private void drawMeasurement(Canvas canvas) {
            float mm = RulerMath.millimetersBetween(markerA, markerB, pxPerMm());
            String number = String.format(Locale.US, "%.1f cm   |   %.2f in", mm / 10f, RulerMath.inchesFromMm(mm));
            boolean sideways = horizontal();
            float cx = sideways ? (markerA + markerB) / 2f : getWidth() / 2f;
            float cy = sideways ? getHeight() / 2f : (markerA + markerB) / 2f;
            p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            p.setTextSize(dp(16));
            float widest = p.measureText(number);
            float halfWidth = (widest + dp(30)) / 2f;
            cx = Math.max(halfWidth + dp(3), Math.min(getWidth() - halfWidth - dp(3), cx));
            cy = Math.max(dp(94), Math.min(getHeight() - dp(54), cy));
            RectF card = new RectF(cx - halfWidth, cy - dp(27), cx + halfWidth, cy + dp(27));
            p.setColor(PANEL);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(card, dp(12), dp(12), p);
            p.setColor(MINT);
            p.setStrokeWidth(dp(1));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(card, dp(12), dp(12), p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(WHITE);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(number, cx, cy + dp(6), p);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            float pos = horizontal() ? event.getX() : event.getY();
            pos = Math.max(0f, Math.min(axisLength(), pos));
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                float a = Math.abs(pos - markerA);
                float b = Math.abs(pos - markerB);
                dragging = a < b ? 0 : 1;
                getParent().requestDisallowInterceptTouchEvent(true);
                if (dragging == 0) markerA = pos; else markerB = pos;
                invalidate();
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if (dragging < 0) return false;
                if (dragging == 0) markerA = pos; else markerB = pos;
                invalidate();
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                dragging = -1;
                invalidate();
                return true;
            }
            return true;
        }
    }
}
