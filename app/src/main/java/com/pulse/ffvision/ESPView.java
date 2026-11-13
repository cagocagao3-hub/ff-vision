package com.pulse.ffvision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class ESPView extends View {
    private Paint paintBox, paintText;
    private List<PointF> targets = new ArrayList<>();

    public ESPView(Context context) {
        super(context);
        paintBox = new Paint();
        paintBox.setColor(Color.parseColor("#00FFCC")); // Ciano Neon
        paintBox.setStyle(Paint.Style.STROKE);
        paintBox.setStrokeWidth(5f);

        paintText = new Paint();
        paintText.setColor(Color.RED);
        paintText.setTextSize(30f);
        paintText.setAntiAlias(true);
    }

    public void updateTargets(List<PointF> newTargets) {
        this.targets = newTargets;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PointF t : targets) {
            // Desenha a caixa em volta do boneco (HEAD)
            canvas.drawRect(t.x - 40, t.y - 40, t.x + 40, t.y + 120, paintBox);
            // Desenha texto de aviso
            canvas.drawText("INIMIGO", t.x - 40, t.y - 50, paintText);
        }
    }
}
