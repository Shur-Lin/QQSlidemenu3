package com.shur.qqslidemenu3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Shur on 2016/10/20.
 */

public class SlideMenu extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private View menuView;
    private View mainView;
    private int menuWidth, menuHeight;
    private int mainWidth, mainHeight;
    private float dragRange;
    private FloatEvaluator floatEvaluator;

    //定义状态常量
    enum DragState {
        Open, Close
    }

    private DragState mState = DragState.Close;//当前SlideMenu的状态

    public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(Context context) {
        super(context);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);
        floatEvaluator = new FloatEvaluator();//初始化float计算器
    }


    public DragState getDragState() {
        return mState;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //做一下简单的异常处理
        if (getChildCount() > 2) {
            throw new IllegalArgumentException("SlideMenu must only have 2 children!");
        }

        menuView = getChildAt(0);
        mainView = getChildAt(1);
    }

    /**
     * 初始化子View的宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        menuWidth = menuView.getMeasuredWidth();
        menuHeight = menuView.getMeasuredHeight();
        mainWidth = mainView.getMeasuredWidth();
        mainHeight = mainView.getMeasuredHeight();

        dragRange = getMeasuredWidth() * 0.65f;//得到拖拽范围
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == menuView || child == mainView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return (int) dragRange;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mainView) {
                left = fixLeft(left);
            }
            return left;
        }

        /**
         * 一般实现伴随移动的逻辑
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == menuView) {
                //先固定住menuView
                menuView.layout(0, menuView.getTop(), menuWidth, menuView.getBottom());

                //再让mainView移动
                int newLeft = fixLeft(mainView.getLeft() + dx);
                mainView.layout(newLeft, mainView.getTop(), newLeft + mainView.getMeasuredWidth(),
                        mainView.getBottom());
            }

            //1.计算移动的百分比
            float fraction = mainView.getLeft() / dragRange;
            //2.根据移动的百分比执行伴随动画
            executeAnim(fraction);
            //3.进行state改变的逻辑判断
            if (mainView.getLeft() == 0 && mState != DragState.Close) {
                //说明关闭状态
                mState = DragState.Close;
                //回调监听器的关闭的方法
                if (listener != null) {
                    listener.onClose();
                }
            } else if (mainView.getLeft() == (int) dragRange && mState != DragState.Open) {
                //说明是打开状态
                mState = DragState.Open;
                //回调监听器的关闭的方法
                if (listener != null) {
                    listener.onOpen();
                }
            }
            //回调拖拽中的方法
            if (listener != null) {
                listener.onDragging(fraction);
            }

        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mainView.getLeft() < dragRange / 2) {
                //在左半边，应该close
                close();
            } else {
                //在右半边，应该open
                open();
            }

            if (xvel < -100 && mState == DragState.Open) {
                //需要关闭
                close();
            } else if (xvel > 100 && mState == DragState.Close) {
                //用户想打开
                open();
            }
        }
    };

    public void close() {
        viewDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);//刷新整个View
    }

    public void open() {
        viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange, mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);//刷新整个View
    }

    /**
     * 执行伴随动画
     *
     * @param fraction
     */
    protected void executeAnim(float fraction) {
        //fraction:0-1
        //1.让mainView执行缩放动画
//		float scaleValue = 0.8f + (1-fraction)*0.2f;//1-0.8
        ViewHelper.setScaleX(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
        ViewHelper.setScaleY(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
        //2.让menuView执行平移动画和缩放动画
        ViewHelper.setTranslationX(menuView, floatEvaluator.evaluate(fraction, -menuWidth / 2, 0));
        ViewHelper.setScaleX(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
        ViewHelper.setScaleY(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
        ViewHelper.setAlpha(menuView, floatEvaluator.evaluate(fraction, 0.4f, 1f));
        //立体翻转
//		ViewHelper.setRotationY(menuView, floatEvaluator.evaluate(fraction, -90, 0));
//		ViewHelper.setRotationY(mainView, floatEvaluator.evaluate(fraction, 0, 90));

        //3.给SLideMenu的背景添加颜色渐变的遮罩
        getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(
                fraction, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);//刷新整个View
        }
    }

    /**
     * 限制left的值的范围
     *
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if (left < 0) left = 0;//限制mainView的左边
        if (left > dragRange) left = (int) dragRange;//限制mainView的右边
        return left;
    }

    private OnDragStateChangeListener listener;

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {
        this.listener = listener;
    }

    /**
     * SlideMenu拖拽状态改变的监听器
     *
     * @author Administrator
     */
    public interface OnDragStateChangeListener {
        void onOpen();

        void onClose();

        void onDragging(float fraction);
    }

}
