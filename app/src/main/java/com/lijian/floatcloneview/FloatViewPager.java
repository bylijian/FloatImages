package com.lijian.floatcloneview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by lijian on 2017/11/22.
 */

public class FloatViewPager extends ViewPager {
    private static final String TAG = "FloatViewPager";

    private static final float FLING_RATIO = 0.25f;
    private static final int SCROLL_BACK_DURATION = 400;
    private static final int FLING_OUT_DURATION = 600;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastDownX;
    private float mLastDownY;
    private float mTouchSlop;
    private int mInitLeft = Integer.MIN_VALUE;
    private int mInitTop = Integer.MIN_VALUE;
    private int mInitBottom = Integer.MIN_VALUE;
    private Scroller mScroller;
    private boolean mScrolling = false;
    private boolean mFlinging = false;
    private int mHeight = -1;
    private ViewGroup mParent;

    private OnPositionChangeListener mPositionListener;
    private DisallowInterruptHandler mDisallowInterruptHandler;
    private TouchState mTouchState = TouchState.NONE;

    public FloatViewPager(Context context) {
        this(context, null);
    }

    public FloatViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mScroller = new Scroller(getContext(), sInterpolator);
        setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                // do transformation here
                final float normalizedposition = Math.abs(Math.abs(position) - 1);
                page.setAlpha(normalizedposition);
            }
        });
        setPageMargin((int) getResources().getDimensionPixelOffset(R.dimen.ui_20_dip));
        setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent()" + ev);
        if (mFlinging || mScrolling) {
            Log.d(TAG, "not need handle event when view is anim");
            return true;
        }
        if (mDisallowInterruptHandler != null && mDisallowInterruptHandler.disallowInterrupt()) {
            Log.d(TAG, "disallow interrupt,just handle by super");
            return super.dispatchTouchEvent(ev);
        }
        int actionMask = ev.getActionMasked();
        Log.d(TAG, "actionMask=" + actionMask + "mTouchState=" + mTouchState);
        switch (actionMask) {
            case MotionEvent.ACTION_DOWN:
                mTouchState = TouchState.NONE;
                mLastMotionX = ev.getRawX();
                mLastMotionY = ev.getRawY();
                mLastDownX = ev.getRawX();
                mLastDownY = ev.getRawY();
                Log.d(TAG, "mLastMotionX=" + mLastMotionX);
                Log.d(TAG, "ev.getRawX()=" + ev.getRawX());
                Log.d(TAG, "mLastMotionY=" + mLastMotionY);
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getRawX();

                final float xDistance = Math.abs(x - mLastDownX);
                final float y = ev.getRawY();
                final float yDistance = Math.abs(y - mLastDownY);
                Log.d(TAG, "ev.getRawX()=" + x);
                Log.d(TAG, "mLastMotionX=" + mLastMotionX);
                Log.d(TAG, "ev.getRawY()=" + y);
                Log.d(TAG, "mLastMotionY=" + mLastMotionY);
                Log.d(TAG, "xDistance=" + xDistance + "yDistance=" + yDistance + "mTouchSlop=" + mTouchSlop);
                if (mTouchState == TouchState.NONE) {
                    if (xDistance + mTouchSlop < yDistance) {
                        mTouchState = TouchState.VERTICAL_MOVE;
                    }
                    if (xDistance > yDistance + mTouchSlop) {
                        mTouchState = TouchState.HORIZONTAL_MOVE;
                    }
                }
                if (mTouchState == TouchState.VERTICAL_MOVE) {
                    move(false, x - mLastMotionX, (y - mLastMotionY));
                }
                mLastMotionX = ev.getRawX();
                mLastMotionY = ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                mLastMotionX = ev.getRawX();
                mLastMotionY = ev.getRawY();
                if (mTouchState == TouchState.VERTICAL_MOVE) {
                    if (needToFlingOut()) {
                        int finalY = getTop() < mInitTop ? -(mHeight + mInitTop) : mParent.getHeight();
                        mFlinging = true;
                        startScrollTopView(0, finalY, FLING_OUT_DURATION);
                    } else {
                        startScrollTopView(mInitLeft, mInitTop, SCROLL_BACK_DURATION);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mTouchState != TouchState.VERTICAL_MOVE) {
                    mTouchState = TouchState.MORE_TOUCH;
                }
                break;
        }
        if (mTouchState == TouchState.VERTICAL_MOVE) {
            return true;
        } else {
            Log.d(TAG, "super.dispatchTouchEvent()");
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            //uncomment if you really want to see these errors
            //e.printStackTrace();
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private boolean needToFlingOut() {
        int distance = Math.abs(getTop() - mInitTop);
        return mHeight * FLING_RATIO <= distance;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeight < 0) {
            mHeight = getHeight();
        }
        if (mParent == null) {
            mParent = (ViewGroup) getParent();
        }
    }

    @Override
    public void computeScroll() {
        Log.d(TAG, "mScrolling=" + mScrolling + "mFlinging=" + mFlinging);
        if (mScroller.computeScrollOffset()) {
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();
            Log.d(TAG, "mScroller.getCurrX()=" + x + "mScroller.getCurrY()=" + y);
            final int dx = x - getLeft();
            final int dy = y - getTop();
            Log.d(TAG, " moveTopView() dx=" + dx + "dy=" + dy);
            move(false, dx, dy);
            if (mFlinging && mPositionListener != null && dy == 0) {
                mPositionListener.onFlingOutFinish();
            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            Log.d(TAG, "computeScrollOffset()=false");
            mScrolling = false;
            super.computeScroll();
        }

    }

    private void move(boolean needHorizontalMove, float deltaX, float deltaY) {
        Log.d(TAG, "move()deltaX=" + deltaX + "deltaY=" + deltaY);
        if (mInitLeft == Integer.MIN_VALUE || mInitTop == Integer.MIN_VALUE || mInitBottom == Integer.MIN_VALUE) {
            mInitLeft = getLeft();
            mInitTop = getTop();
            mInitBottom = getBottom();
            Log.d(TAG, "mInitLeft=" + mInitLeft + "mInitTop=" + mInitTop);
        }
        if (needHorizontalMove) {
            offsetLeftAndRight((int) deltaX);
        }
        offsetTopAndBottom((int) deltaY);
        if (mPositionListener != null) {
            mPositionListener.onPositionChange(mInitTop, getTop(), Math.abs(mInitTop - getTop()) * 1.0f / mHeight);
        }
    }

    public void startScrollTopView(int finalLeft, int finalTop, int duration) {
        Log.d(TAG, "startScrollTopView finalLeft=" + finalLeft + "finalTop" + finalTop);
        final int startLeft = getLeft();
        final int startTop = getTop();
        final int dx = finalLeft - startLeft;
        final int dy = finalTop - startTop;
        if (dx != 0 || dy != 0) {
            mScroller.abortAnimation();
            mScrolling = true;
            mScroller.startScroll(startLeft, startTop, dx, dy, duration);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            mScrolling = false;
        }
    }

    public void setPositionListener(OnPositionChangeListener positionListener) {
        mPositionListener = positionListener;
    }

    public void setDisallowInterruptHandler(DisallowInterruptHandler disallowInterruptHandler) {
        mDisallowInterruptHandler = disallowInterruptHandler;
    }


    /**
     * Interpolator defining the animation curve for mScroller
     */
    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1f;
            return t * t * t * t * t + 1f;
        }
    };

    public interface OnPositionChangeListener {

        void onPositionChange(int initTop, int nowTop, float ratio);

        void onFlingOutFinish();
    }

    public interface DisallowInterruptHandler {

        boolean disallowInterrupt();

    }

    public enum TouchState {
        NONE,//普通状态
        DISALLOW_INTERRUPT,//不允许拦截
        HORIZONTAL_MOVE,//横滑动
        VERTICAL_MOVE,//竖滑动
        MORE_TOUCH//多点触摸
    }

}
