package com.example.macyg.androidmediaplayer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {

    Paint paint = new Paint();
    int viewWidth;
    int viewHeight;
    int radius = 100;

    //First get custom views dimensions
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);


        viewWidth = xNew;
        viewHeight = yNew;
    }

    public CustomView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setAntiAlias(true);
        canvas.drawCircle(viewWidth / 2, viewHeight / 2 + 10, radius, paint);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}