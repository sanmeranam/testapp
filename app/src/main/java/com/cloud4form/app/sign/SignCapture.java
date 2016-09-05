package com.cloud4form.app.sign;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * TODO: document your custom view class.
 */
public class SignCapture extends SurfaceView {
    private Paint paintTouchDraw;
    private Bitmap bitmap;
    private Canvas biCanvas;
    private Path path;
    private PointF lastPoint;
    private boolean isSigned=false;

    public SignCapture(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }
    public SignCapture(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }
    public SignCapture(Context context) {
        super(context);
        this.init();
    }

    public boolean isSigned() {
        return isSigned;
    }
    public void clear() {
        path.reset();
        if(biCanvas!=null) {
            biCanvas.drawColor(Color.WHITE);
            isSigned = false;
            invalidate();
        }
    }
    private void init(){
        paintTouchDraw = new Paint();
        paintTouchDraw.setAntiAlias(true);
        paintTouchDraw.setDither(true);
        paintTouchDraw.setColor(Color.BLACK);
        paintTouchDraw.setStyle(Paint.Style.STROKE);
        paintTouchDraw.setStrokeJoin(Paint.Join.ROUND);
        paintTouchDraw.setStrokeCap(Paint.Cap.ROUND);
        paintTouchDraw.setStrokeWidth(4);
        path=new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintTouchDraw);
        canvas.drawPath(path, paintTouchDraw);
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        biCanvas=new Canvas(bitmap);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                path.reset();
                lastPoint=new PointF(event.getX(),event.getY());
                path.moveTo(event.getX(),event.getY());

                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(event.getX() - lastPoint.x);
                float dy = Math.abs(event.getY() - lastPoint.y);
                if (dx >= 4 || dy >= 4) {

                    float dMidx = (event.getX() + lastPoint.x) / 2;
                    float dMidy = (event.getY() + lastPoint.y) / 2;

                    path.quadTo(lastPoint.x, lastPoint.y, dMidx, dMidy);

                    lastPoint=new PointF(event.getX(),event.getY());
                }
                isSigned=true;
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                path.lineTo(event.getX(),event.getY());
                biCanvas.drawPath(path, paintTouchDraw);
                invalidate();
                break;
        }
        return true;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
}
