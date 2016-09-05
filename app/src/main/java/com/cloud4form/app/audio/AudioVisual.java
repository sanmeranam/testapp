package com.cloud4form.app.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cloud4form.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class AudioVisual extends View {

    private int mLineColor = Color.RED;
    private int mLineWidth = 1;
    private List<Integer> amplitudes;
    private int width;
    private int height;
    private Paint linePaint;
    private TextPaint textPaint;
    private String text="00:00";


    public AudioVisual(Context context) {
        super(context);
        init(null, 0);
    }

    public AudioVisual(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AudioVisual(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void clear() {
        amplitudes.clear();
    }

    public void setText(String text) {
        this.text=text;
    }
    public void addAmplitude(int amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * mLineWidth*2 >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }

    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w; // new width of this View
        height = h; // new height of this View
        amplitudes = new ArrayList<Integer>(width / mLineWidth);
    }

    private void init(AttributeSet attrs, int defStyle) {

        amplitudes=new ArrayList<>();

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AudioVisual, defStyle, 0);

        mLineColor = a.getColor(R.styleable.AudioVisual_lineColor, mLineColor);
        mLineWidth = a.getInt(R.styleable.AudioVisual_lineWidth, mLineWidth);


        a.recycle();


        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(80);
        textPaint.setColor(Color.parseColor("#efefef"));


//        textPaint.set
        linePaint = new Paint();
        linePaint.setColor(mLineColor);
        linePaint.setStrokeWidth(mLineWidth);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        int middle = contentHeight / 2;
        float curX = 0;
        float LINE_SCALE=contentHeight/100;

        for (float power : amplitudes) {
            float scaledHeight = power/LINE_SCALE;
            curX += mLineWidth*2;
            Log.v("LINE","Line"+power);
            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle - scaledHeight / 2, linePaint);
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float mTextWidth = textPaint.measureText(this.text);
        float mTextHeight = fontMetrics.bottom;

        canvas.drawText(this.text,
                paddingLeft + (contentWidth - mTextWidth),
                paddingTop + (contentHeight - mTextHeight),
                textPaint);
    }
}
