package com.pulse.ffvision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ESPView extends View {
    private Paint paint;
    private int targetX = -1, targetY = -1;
    private int screenW, screenH;

    public ESPView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
    }

    public void setTarget(int x, int y, int w, int h) {
        this.targetX = x; this.targetY = y;
        this.screenW = w; this.screenH = h;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (targetX != -1 && targetY != -1) {
            canvas.drawRect(targetX - 50, targetY - 100, targetX + 50, targetY + 100, paint);
            paint.setStrokeWidth(2f);
            canvas.drawLine(screenW/2f, screenH/2f, targetX, targetY, paint);
        }
    }
}
