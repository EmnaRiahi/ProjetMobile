package com.example.mama.sport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class TrendChartView extends View {

    private Paint linePaint;
    private Paint dotPaint;
    private Paint textPaint;
    private List<Float> dataPoints = new ArrayList<>();
    private List<String> labels = new ArrayList<>();

    public TrendChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0xFF00B894); // Green
        linePaint.setStrokeWidth(8);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setColor(0xFF0984E3); // Blue
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    public void setData(List<Float> points, List<String> labels) {
        this.dataPoints = points;
        this.labels = labels;
        invalidate(); // Redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 60f;
        float chartWidth = width - (padding * 2);
        float chartHeight = height - (padding * 2);

        float xStep = chartWidth / (dataPoints.size() - 1);
        float maxY = 100f; // Percentage 0-100

        Path path = new Path();

        for (int i = 0; i < dataPoints.size(); i++) {
            float val = dataPoints.get(i);
            float x = padding + (i * xStep);
            float y = padding + chartHeight - (val / maxY * chartHeight);

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);

            canvas.drawCircle(x, y, 10, dotPaint);
            
            // Draw Label (Day)
            if (i < labels.size()) {
                canvas.drawText(labels.get(i), x, height - 10, textPaint);
            }
        }
        canvas.drawPath(path, linePaint);
    }
}
