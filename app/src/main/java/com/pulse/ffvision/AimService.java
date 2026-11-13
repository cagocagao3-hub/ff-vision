package com.pulse.ffvision;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class AimService extends AccessibilityService {
    public static AimService instance;

    @Override
    public void onServiceConnected() { instance = this; }

    public void dragAim(float dx, float dy) {
        if (instance == null) return;
        float startX = 540, startY = 1200; // Centro aproximado da tela
        float endX = startX + dx, endY = startY + dy;
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        // 1ms = Snap instantâneo
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
