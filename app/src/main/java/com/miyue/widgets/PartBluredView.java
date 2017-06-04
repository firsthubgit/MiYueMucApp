package com.miyue.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.miyue.utils.DisplayUtils;


/**
*
* @author ZZD
* @time 17/5/29 下午2:48
*/
public class PartBluredView extends View {


    private int screenW, screenH;
    private Bitmap srcBitmap;

    private Rect mSrcRect, mDisRect;

    private Paint mPaint;

    public PartBluredView(Context context) {
        this(context, null);

    }

    public PartBluredView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PartBluredView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        screenW = DisplayUtils.getScreenWidth(context);
        screenH = DisplayUtils.getScreenHeight(context);
        mSrcRect = new Rect(0, 0, 0, screenH);
        mDisRect = new Rect(0, 0, 0,screenH);
    }

    public void setSrcBitmap(Bitmap bitmap){
        if(srcBitmap != null){
            srcBitmap.recycle();
            srcBitmap = null;
        }
        srcBitmap = bitmap;
    }

    public void setWidth(int w){
        int x =  (w * srcBitmap.getWidth() / screenW);
        mSrcRect.set(0,0, x, srcBitmap.getHeight());
        mDisRect.set(0,0, w, screenH);
    }
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(srcBitmap != null){
            canvas.drawBitmap(srcBitmap, mSrcRect, mDisRect, mPaint);
        }
    }
}
