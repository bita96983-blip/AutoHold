package com.autohold.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HoldService extends AccessibilityService {
    private WindowManager wm;
    private View targetView;
    private WindowManager.LayoutParams targetParams;
    private View controlView;
    private WindowManager.LayoutParams controlParams;
    private boolean isHolding = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Button toggleBtn;

    private Runnable holdRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isHolding) return;
            performLongHold();
            handler.postDelayed(this, 59500);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createTargetCircle();
        createControlPanel();
    }

    private void createTargetCircle() {
        TextView circle = new TextView(this);
        circle.setText("⊕");
        circle.setTextSize(28);
        circle.setTextColor(Color.RED);
        circle.setBackgroundColor(Color.parseColor("#66FF0000"));
        circle.setGravity(Gravity.CENTER);
        circle.setPadding(25, 10, 25, 10);

        targetParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        targetParams.gravity = Gravity.TOP | Gravity.START;
        targetParams.x = 400;
        targetParams.y = 700;

        circle.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isHolding) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = targetParams.x;
                        initialY = targetParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        targetParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        targetParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        wm.updateViewLayout(targetView, targetParams);
                        return true;
                }
                return false;
            }
        });

        targetView = circle;
        wm.addView(targetView, targetParams);
    }

    private void createControlPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.parseColor("#CC000000"));
        panel.setPadding(15, 15, 15, 15);

        toggleBtn = new Button(this);
        toggleBtn.setText("▶ تثبيت");
        toggleBtn.setBackgroundColor(Color.parseColor("#2E7D32"));
        toggleBtn.setTextColor(Color.WHITE);
        toggleBtn.setOnClickListener(v -> {
            if (!isHolding) {
                isHolding = true;
                toggleBtn.setText("⏹ إيقاف");
                toggleBtn.setBackgroundColor(Color.parseColor("#C62828"));
                setTargetTouchable(false);
                handler.post(holdRunnable);
            } else {
                isHolding = false;
                handler.removeCallbacks(holdRunnable);
                toggleBtn.setText("▶ تثبيت");
                toggleBtn.setBackgroundColor(Color.parseColor("#2E7D32"));
                setTargetTouchable(true);
            }
        });

        panel.addView(toggleBtn);

        controlParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        controlParams.gravity = Gravity.TOP | Gravity.START;
        controlParams.x = 50;
        controlParams.y = 300;

        panel.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = controlParams.x;
                        initialY = controlParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        controlParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        controlParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        wm.updateViewLayout(controlView, controlParams);
                        return true;
                }
                return false;
            }
        });

        controlView = panel;
        wm.addView(controlView, controlParams);
    }

    private void setTargetTouchable(boolean touchable) {
        if (touchable) {
            targetParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            targetParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        wm.updateViewLayout(targetView, targetParams);
    }

    private void performLongHold() {
        int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        float centerX = location[0] + (targetView.getWidth() / 2f);
        float centerY = location[1] + (targetView.getHeight() / 2f);

        Path path = new Path();
        path.moveTo(centerX, centerY);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 59000);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        isHolding = false;
        handler.removeCallbacks(holdRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isHolding = false;
        handler.removeCallbacks(holdRunnable);
        if (wm != null) {
            if (targetView != null) wm.removeView(targetView);
            if (controlView != null) wm.removeView(controlView);
        }
    }
}
