package com.cloud4form.app.filescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.util.Stack;

/**
 * Created by Santanu on 8/21/2014.
 */
public class CropView extends SurfaceView {


    private Paint mFill, mStrock, mSelect, mLine, mLine2;
    private Bitmap imgDraw, imgPreview, imgOriginal;
    private int index = -1;
    private float scaleFactor = 1.0f;
    private float iDrawLeft, iDrawTop;
    private float previewLeft = 0, previewTop = 0;
    private boolean isDrawCropper = true;
    private final int mCernorCircleWidth = getSizeInPixel(50), mPointHeight = getSizeInPixel(50);
    private Vertex mVertex;
    private Dims screenDim;

    public CropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mLine = new Paint();
        mLine.setAntiAlias(true);
        mLine.setColor(Color.WHITE);
        mLine.setStrokeWidth(getSizeInPixel(2));
        mLine.setStyle(Paint.Style.STROKE);
        mLine.setStrokeCap(Paint.Cap.ROUND);
        mLine2 = new Paint(mLine);
        mLine2.setStrokeWidth(getSizeInPixel(2));

        mFill = new Paint();
        mFill.setAntiAlias(true);
        mFill.setColor(Color.parseColor("#80ffffff"));
        mFill.setStyle(Paint.Style.FILL);

        mSelect = new Paint(mFill);
        mSelect.setColor(Color.parseColor("#90ffffff"));

        mStrock = new Paint(mLine);
    }


    public void applyBrightContrast(float contrast, int brightness) {
        this.imgDraw = changeBitmapContrastBrightness(this.imgDraw, contrast, brightness);
        this.imgOriginal = changeBitmapContrastBrightness(this.imgOriginal, contrast, brightness);
        this.invalidate();
    }

    public boolean applyCrop() {
        if(isDrawCropper){
            this.imgDraw = processCrop(imgDraw, false);
            this.invalidate();
            this.imgOriginal = processCrop(imgOriginal, true);
            isDrawCropper = false;
            return true;
        }else {
            return false;
        }
    }


    /**
     * @param image
     */
    public void setImage(Bitmap image) throws IllegalArgumentException {
        if (image == null) {
            new IllegalArgumentException("Null image.");
        }
        this.imgOriginal = scaleImage(image, 768, 1024);
    }

    public Bitmap getCroppedImage() {
        return imgOriginal;
    }

    /**
     * @param bmp        input bitmap
     * @param contrast   0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    private static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, int brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return ret;
    }

    private Bitmap processCrop(Bitmap bitmap, boolean isOrg) {
        if (bitmap == null) {
            return null;
        }

        Vertex mLocalVertex = this.mVertex.createClone(isOrg ? scaleFactor : 1.0f);
        float nDrawLeft = this.iDrawLeft * (isOrg ? scaleFactor : 1.0f);
        float nDrawTop = this.iDrawTop * (isOrg ? scaleFactor : 1.0f);
        float srcVertises[] = {
                mLocalVertex.A.centerX() - nDrawLeft, mLocalVertex.A.centerY() - nDrawTop,
                mLocalVertex.B.centerX() - nDrawLeft, mLocalVertex.B.centerY() - nDrawTop,
                mLocalVertex.C.centerX() - nDrawLeft, mLocalVertex.C.centerY() - nDrawTop,
                mLocalVertex.D.centerX() - nDrawLeft, mLocalVertex.D.centerY() - nDrawTop
        };

        float avgW = (float) (distance(mLocalVertex.A, mLocalVertex.B) + distance(mLocalVertex.D, mLocalVertex.C)) / 2;
        float avgH = (float) (distance(mLocalVertex.A, mLocalVertex.D) + distance(mLocalVertex.B, mLocalVertex.C)) / 2;

        float minLeft = Math.min(mLocalVertex.A.centerX(), mLocalVertex.D.centerX());

        Dims newA, newD;

        if (minLeft == mLocalVertex.A.centerX()) {
            newD = getAngularPoint(avgH, 0, mLocalVertex.A);
            newA = getAngularPoint(avgH, 180, newD);
        } else {
            newA = getAngularPoint(avgH, 180, mLocalVertex.D);
            newD = getAngularPoint(avgH, 0, newA);
        }

        Dims newB = getAngularPoint(avgW, 90, newA);
        Dims newC = getAngularPoint(avgW, 90, newD);

        float targetVertieses[] = {
                newA.left, newA.top,
                newB.left, newB.top,
                newC.left, newC.top,
                newD.left, newD.top,
        };

        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(srcVertises, 0, targetVertieses, 0, srcVertises.length >> 1);
        WeakReference<Bitmap> bitmapWeakReference = new WeakReference<Bitmap>(applyMatrix(bitmap, matrix,false));
        bitmap = Bitmap.createBitmap(bitmapWeakReference.get(), (int) newA.left, (int) newA.top, (int) avgW, (int) avgH);
        return bitmap;
    }

    /**
     * Distance between two points.
     *
     * @param p1 point one
     * @param p2 point two
     * @return distance value
     */
    private double distance(Dims p1, Dims p2) {
        return Math.sqrt(Math.pow((p1.left - p2.left), 2) + Math.pow((p1.top - p2.top), 2));

    }

    /**
     * Return radial x,y value based on radius and angle;
     *
     * @param r     Radius
     * @param angle angle deviation
     * @return
     */
    private Dims getAngularPoint(float r, float angle, Dims dims) {
        double left = (r * Math.sin(angle * (Math.PI / 180)) + dims.centerX());
        double top = (r * Math.cos(angle * (Math.PI / 180)) + dims.centerY());
        return new Dims((float) left, (float) top);
    }

    /**
     * @param bitmap
     * @param matrix
     * @return
     */
    private Bitmap applyMatrix(Bitmap bitmap, Matrix matrix,boolean forRotation) {

        Bitmap alteredBitmap = forRotation?
                Bitmap.createBitmap( bitmap.getHeight(),bitmap.getWidth(), bitmap.getConfig()):
                Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        Canvas canvas = new Canvas(alteredBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, matrix, paint);
        return alteredBitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(this.imgOriginal==null)return;

        screenDim = new Dims(0, 0, w, h);
        this.imgDraw = scaleImage(this.imgOriginal, w, h);

        this.scaleFactor = (float) this.imgOriginal.getWidth() / (float) this.imgDraw.getWidth();
        mVertex = new Vertex();
        int cropBarLeft = getSizeInPixel(50);
        int cropBarTop = getSizeInPixel(50);
        int cropPointRadius = getSizeInPixel(50);
        int cropBarRight = w - (cropBarLeft + cropPointRadius);
        int cropBarBottom = h - (cropBarLeft + cropPointRadius);


        mVertex.A = new Dims(cropBarLeft, cropBarTop, mCernorCircleWidth, mPointHeight);
        mVertex.B = new Dims(cropBarRight, cropBarTop, mCernorCircleWidth, mPointHeight);
        mVertex.C = new Dims(cropBarRight, cropBarBottom, mCernorCircleWidth, mPointHeight);
        mVertex.D = new Dims(cropBarLeft, cropBarBottom, mCernorCircleWidth, mPointHeight);
        this.invalidate();
    }

    /**
     * @param size
     * @return
     */
    private int getSizeInPixel(int size) {
        return (int) (size * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (imgDraw != null) {

            iDrawLeft = (screenDim.width / 2) - (imgDraw.getWidth() / 2);
            iDrawTop = (screenDim.height / 2) - (imgDraw.getHeight() / 2);
            canvas.drawBitmap(imgDraw, iDrawLeft, iDrawTop, mLine);

            if (isDrawCropper) {
                for (int i = 0; i < mVertex.getCount(); i++) {
                    canvas.drawCircle(mVertex.get(i).centerX(), mVertex.get(i).centerY(), mCernorCircleWidth / 2, mStrock);
                    canvas.drawCircle(mVertex.get(i).centerX(), mVertex.get(i).centerY(), mCernorCircleWidth / 2, (index == i ? mSelect : mFill));

                    int nI = i == 3 ? 0 : i + 1;
                    canvas.drawLine(mVertex.get(i).centerX(), mVertex.get(i).centerY(), mVertex.get(nI).centerX(), mVertex.get(nI).centerY(), mLine);
                }
            }

            if (imgPreview != null) {

                if (mVertex.get(index).left > screenDim.width / 2) {
                    previewLeft = 0;
                } else {
                    previewLeft = screenDim.width - imgPreview.getWidth();
                }
                canvas.drawBitmap(imgPreview, previewLeft, previewTop, mLine);
                RectF rect = new RectF(previewLeft, previewTop, previewLeft + imgPreview.getWidth(), previewTop + imgPreview.getHeight());
                canvas.drawRect(rect, mLine2);

                canvas.drawLine(rect.centerX() - 10, rect.centerY(), rect.centerX() + 10, rect.centerY(), mLine2);
                canvas.drawLine(rect.centerX(), rect.centerY() - 10, rect.centerX(), rect.centerY() + 10, mLine2);
            }
        }

        super.onDraw(canvas);
    }


    /**
     * @param bitmap
     * @return
     */
    private Bitmap scaleImage(Bitmap bitmap, float width, float height) {
        if (bitmap == null) {
            return null;
        }
        Bitmap result = null;

        float deviceAR = height / width;
        float imgAR = (float) bitmap.getHeight() / (float) bitmap.getWidth();

        float Nw, Nh;

        if (imgAR > deviceAR) {
            Nh = height;
            Nw = Nh / imgAR;
        } else {
            Nw = width;
            Nh = Nw * imgAR;
        }
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(Nw / bitmap.getWidth(), Nh / bitmap.getHeight());
        result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), scaleMatrix, true);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawCropper) {
            return false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mVertex.getCount(); i++) {
                    if (mVertex.get(i).contains(event.getX() + iDrawLeft, event.getY() + iDrawTop)) {
                        index = i;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (index > -1) {
                    mVertex.get(index).moveTo(event.getX() - mVertex.get(index).width / 2, event.getY() - mVertex.get(index).width / 2);
                    if (imgDraw != null) {
                        try {
                            int nLeft = (int) mVertex.get(index).left - (int) iDrawLeft;
                            int nTop = (int) mVertex.get(index).top - (int) iDrawTop;
                            int nWidth = (int) mVertex.get(index).width;
                            int nHeight = (int) mVertex.get(index).height;
                            nLeft = nLeft < 0 ? 0 : nLeft;
                            nTop = nTop < 0 ? 0 : nTop;
                            nWidth = nWidth > imgDraw.getWidth() ? imgDraw.getWidth() : nWidth;
                            nHeight = nHeight > imgDraw.getHeight() ? imgDraw.getHeight() : nHeight;
                            Matrix matrix = new Matrix();
                            matrix.postScale(1.5f, 1.5f);
                            imgPreview = Bitmap.createBitmap(imgDraw, nLeft, nTop, nWidth, nHeight, matrix, true);
                        } catch (Exception ex) {
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                index = -1;
                imgPreview = null;
                invalidate();
                break;
        }
        return true;
    }

    private static class Dims {
        float left;
        float top;
        float width;
        float height;

        Dims(float left, float top) {
            this.left = left;
            this.top = top;
            this.width = 0f;
            this.height = 0f;
        }

        Dims(float left, float top, float width, float height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
        }

        public Dims createClone(float scale) {
            return new Dims(this.left * scale, this.top * scale, this.width * scale, this.height * scale);
        }

        boolean contains(float x, float y) {
            return (x > left && x < left + width) && (y > top && y < top + height);
        }

        float centerX() {
            return left + (width / 2);
        }

        float centerY() {
            return top + (height / 2);
        }

        void moveTo(float newLeft, float newTop) {
            this.left = newLeft;
            this.top = newTop;
        }
    }

    private static class Vertex {
        Dims A;
        Dims B;
        Dims C;
        Dims D;

        public int getCount() {
            return 4;
        }

        public Dims get(int x) {
            switch (x) {
                case 0:
                    return A;
                case 1:
                    return B;
                case 2:
                    return C;
                case 3:
                    return D;

            }
            return null;
        }

        public Vertex createClone(float scale) {
            Vertex vertex = new Vertex();
            vertex.A = this.A.createClone(scale);
            vertex.B = this.B.createClone(scale);
            vertex.C = this.C.createClone(scale);
            vertex.D = this.D.createClone(scale);
            return vertex;
        }
    }

}