package com.levalu.study.mycamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class RectArea extends View {
    private Paint mPaint;
    private int mLeft , mTop, mWidth, mHeight;

    public RectArea(Context context) {
        super(context);

        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(6);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        //center
        int x0 = canvas.getWidth() / 2;
        int y0 = canvas.getHeight() / 2;

        int dx = (canvas.getWidth() / 10 ) * 3;
        int dy = canvas.getHeight() / 32;

        mLeft = x0 - dx;
        mTop = y0 - dy;
        mWidth = dx * 2;
        mHeight = dy * 2;

        canvas.drawRoundRect(x0 - dx, y0 - dy, x0 + dx, y0 + dy, 40f, 40f, mPaint);
    }

    public int getMyLeft() {
        return mLeft;
    }

    public int getMyTop() {
        return mTop;
    }

    public int getMyWidth() {
        return mWidth;
    }

    public int getMyHeight() {
        return mHeight;
    }
}
