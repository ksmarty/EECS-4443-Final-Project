package digitalink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import ca.yorku.eecs4443_finalproject_golf.R;

/**
 * Code from Ibrahim Canerdogan
 * https://github.com/icanerdogan/Google-MLKit-Android-Apps/tree/master/Archive%20-%20Java/DigitalInkRecognition
 */
public class DrawView extends View {
    private final Paint currentStrokePaint;
    private final Paint canvasPaint;
    private final Path currentStroke;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        float STROKE_WIDTH_DP = 16.0f;

        currentStrokePaint = new Paint();
        currentStrokePaint.setColor(getResources().getColor(R.color.digital_ink_pen, null));
        currentStrokePaint.setAntiAlias(true);
        currentStrokePaint.setStrokeWidth(STROKE_WIDTH_DP);
        currentStrokePaint.setStyle(Paint.Style.STROKE);
        currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        currentStroke = new Path();

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint);
        canvas.drawPath(currentStroke, currentStrokePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN -> currentStroke.moveTo(x, y);
            case MotionEvent.ACTION_MOVE -> currentStroke.lineTo(x, y);
            case MotionEvent.ACTION_UP -> {
                currentStroke.lineTo(x, y);
                drawCanvas.drawPath(currentStroke, currentStrokePaint);
                currentStroke.reset();
            }
        }
        StrokeManager.addNewTouchEvent(event);
        invalidate();
        return true;
    }

    public void clear() {
        onSizeChanged(canvasBitmap.getWidth(),
                canvasBitmap.getHeight(),
                canvasBitmap.getWidth(),
                canvasBitmap.getHeight());
    }
}
