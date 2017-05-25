package com.miyue.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.utils.UtilLog;

/**
 * Created by zhangzhendong on 16/4/12.
 */
public class SwipeBackLayout extends FrameLayout {

    private int mTouchSlop;
    private Scroller mScroller;
    private View mContentView;
    private boolean mGestureEnable = true;
    private int downX, downY, tempX, moveX;
    private VelocityTracker mVelocityTracker;
    private int minmumFilingVelocity;
    /**是否正在滑动*/
    private boolean isSliding;

    private boolean isFinish;

    private int viewWidth;
    private Drawable mShadowDrawable;


    public static int SNAP_VELOCITY = 4000;  //最小的滑动速率

    public OnFinishListener finishListener;
    public SwipeBackLayout(Context context) {
        super(context);
    }
    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
        mShadowDrawable = getResources().getDrawable(R.drawable.shadow_left);
//        FrameLayout.LayoutParams lpm =
//                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT);
//        setLayoutParams(lpm);
    }

    /**设为true滑动,false不滑动*/
    public void setSwipeEnable(boolean enable){
        mGestureEnable = enable;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mGestureEnable) {
            return false;
        }
        getVelocityTracker();//用来计算当大于某个速率的时候就关闭View
        mVelocityTracker.addMovement(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = tempX = (int)event.getRawX();
                downY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int)event.getRawX();
                int deltaX = tempX - moveX;
                tempX = moveX;
                if (moveX - downX > mTouchSlop
                        && Math.abs((int) event.getRawY() - downY) < mTouchSlop) {
                    isSliding = true;
                }

                if (moveX - downX >= 0 && isSliding) {
                    scrollBy(deltaX, 0);
                }

                break;
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getRawX();
                isSliding = false;
                mVelocityTracker.computeCurrentVelocity(1000);
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                UtilLog.e("Velocity","xVelocity" + xVelocity);
                if(xVelocity > SNAP_VELOCITY){
                    scrollToRight(viewWidth, 0);
                    isFinish = true;
                } else {
                    if(upX - downX >= viewWidth*2/5){
                        scrollToRight(viewWidth, 0);
                        isFinish = true;
                    } else {
                        scrollToOrigo(0,0);
                        isFinish = false;
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            viewWidth = getWidth();
        }
    }

    /**滚回左边的位置*/
    private void scrollToOrigo(int desX, int desY) {

        int mScrollX = getScrollX();
        mScroller.startScroll(mScrollX, 0, -mScrollX, 0, 500);
        invalidate();
    }

    /**往右边滚出屏幕*/
    private void scrollToRight(int desX, int desY) {

        int mScrollX = getScrollX() ;
        int delatX = desX + getScrollX() ;
        mScroller.startScroll(mScrollX, 0, -delatX, 0, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
        if (mScroller.isFinished() && isFinish) {
            if(finishListener != null){
                finishListener.finish();
            }
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mShadowDrawable != null ) {

            int left = getLeft()
                    - mShadowDrawable.getIntrinsicWidth();
            int right = getLeft();
            int top = getTop();
            int bottom = getBottom();

            mShadowDrawable.setBounds(left, top, right, bottom);
            mShadowDrawable.draw(canvas);
        }

    }

    /**
     * 获取速度追踪器
     *
     * @return
     */
    private VelocityTracker getVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        return mVelocityTracker;
    }

    public void setOnFinishListener(OnFinishListener fl){
        finishListener = fl;
    }

    public interface OnFinishListener{
        void finish();
    }

//    public void attachToActivity(Activity activity) {
//        mActivity = activity;
//        TypedArray a = activity.getTheme().obtainStyledAttributes(
//                new int[] { android.R.attr.windowBackground });
//        int background = a.getResourceId(0, 0);
//        a.recycle();
//
//        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
//        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
//        decorChild.setBackgroundResource(background);
//        decor.removeView(decorChild);
//        addView(decorChild);
//        setContentView(decorChild);
//        decor.addView(this);
//    }

    public void attachToFragment(BaseSwipeBackFragment fragment) {

        ViewGroup rootview = (ViewGroup) fragment.getView();
        ViewGroup childView = (ViewGroup) rootview.getChildAt(0);
        rootview.removeView(childView);
        addView(childView);
        rootview.addView(this);
    }

    /**
     * 回收速度追踪器
     */
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
}
