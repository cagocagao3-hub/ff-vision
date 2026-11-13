package com.pulse.ffvision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ESPView extends View {
    private Paint paintBox, paintLine;
    private int targetX = -1, targetY = -1;
    private int screenW, screenH;

    public ESPView(Context context) {
        super(context);
        paintBox = new Paint();
        paintBox.setColor(Color.parseColor("#BB86FC")); // Roxo Neon
        paintBox.setStyle(Paint.Style.STROKE);
        paintBox.setStrokeWidth(6f);

        paintLine = new Paint();
        paintLine.setColor(Color.RED);
        paintLine.setStrokeWidth(2f);
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
            // Desenha uma caixa maior em volta do boneco
            canvas.drawRect(targetX - 60, targetY - 50, targetX + 60, targetY + 150, paintBox);
            
            // Linha do centro (Skeleton)
            canvas.drawLine(screenW/2f, screenH/2f, targetX, targetY, paintLine);
        }
    }
}
