package com.shur.qqslidemenu3.Demo;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Shur on 2016/10/20.
 */

public class DragLayout extends FrameLayout {

    private View redView;// 红孩儿
    private View blueView;// 蓝精灵
    private ViewDragHelper viewDragHelper;
    private int horizontalRange, verticalRange;

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        viewDragHelper = viewDragHelper.create(this, mCallback);
    }

    /**
     * 当DragLayout的在 布局文件中的结束标签被解析完成会调用; 一般在该方法中可以初始化子View的引用
     * 但是此时还不能获取子View的宽高
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        redView = getChildAt(0);
        blueView = getChildAt(1);
    }

    /**
     * 在onMeasure执行完成之后执行，所以可以在该方法中获取子View的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //范围
        horizontalRange = getMeasuredWidth() - blueView.getMeasuredWidth();
        verticalRange = getMeasuredHeight() - blueView.getMeasuredHeight();
    }

    /**
     * 测量所有的子View
     */
    // @Override
    // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    // //1.自己实现测量
    // // int measureSpec =MeasureSpec.makeMeasureSpec(redView.getLayoutParams().width,MeasureSpec.EXACTLY);
    // // redView.measure(measureSpec,measureSpec);
    // // blueView.measure(measureSpec,measureSpec);
    //
    // //2.使用View提供的方法实现测量
    // // measureChild(redView, widthMeasureSpec, heightMeasureSpec);
    // // measureChild(blueView, widthMeasureSpec, heightMeasureSpec);
    // for (int i = 0; i < getChildCount(); i++) {
    // View child = getChildAt(i);
    // measureChild(child, widthMeasureSpec, heightMeasureSpec);
    // }
    // }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将TouchEvent传递给ViewDragHelper
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * v4包下google官方提供的viewdraghelper 用于实现拖拽view
     * 需要有一个回调方法  和gesture手势一样需要回调
     */
    private ViewDragHelper.Callback mCallback = new Callback() {
        /**
         * 用来控制是否开始捕获View的触摸事件
         * @param child 表示当前正在触摸的子View
         * @param pointerId
         * @return true:表示捕获    false：不捕获
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == blueView || child == redView;
        }

        /**
         * 当View开始被捕获的回调,如果需要当View被捕获做一些逻辑操作，可以在该方法中写
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 并不是用来控制拖拽范围的，目前用在计算平滑动画的移动时间上面，最好不要返回0
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return horizontalRange;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return verticalRange;
        }

        /**
         * 控制View水平方向的移动
         * child: 当前所触摸的子View
         * left: 表示ViewDragHelper认为你想让当前View的left变成的值，left=view.getLeft()+dx
         * dx: 本次水平移动的距离
         * return: 表示我们想让当前View的left变成的值;
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left < 0)
                left = 0;
            if (left > horizontalRange)
                left = horizontalRange;
            return left;
        }

        /**
         * 控制View垂直方向的移动
         * child: 当前所触摸的子View
         * top: 表示ViewDragHelper认为你想让当前View的top变成的值，top=view.getTop()+dy
         * dy: 本次垂直移动的距离
         * return: 表示我们想让当前View的top变成的值;
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top < 0)
                top = 0;
            if (top > verticalRange)
                top = verticalRange;
            return top;
        }

        /**
         * 手指抬起的时候执行
         * releasedChild： 当前所释放的View
         * xvel: x方向移动的速度
         * yvel：  y方向移动的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            //求出View在水平正中间时候的left的值
            int centerLeft = getMeasuredWidth()/2-releasedChild.getMeasuredWidth()/2;
            if(releasedChild.getLeft()<centerLeft){
                //在左半边,应该缓慢滑动到左边
                viewDragHelper.smoothSlideViewTo(releasedChild,0,releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);//刷新整个DragLayout
            }else {
                //在右半边,应该缓慢滑动到右边
                viewDragHelper.smoothSlideViewTo(releasedChild,horizontalRange,releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);//刷新整个DragLayout
            }

        }

        /**
         * 当View位置改变的回调,一般在该方法中做伴随移动
         * left: view当前的left
         * top: view当前的top
         * dx: 本次水平移动的距离
         * dy： 本次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            Log.e("tag", "left: " + left + "  dx:" + dx);
            if (changedView == blueView) {
                //需要让redView跟随移动
//				redView.layout(redView.getLeft()+dx,redView.getTop()+dy,redView.getRight()+dx,
//						redView.getBottom()+dy);
                redView.offsetLeftAndRight(dx);
                redView.offsetTopAndBottom(dy);
            } else if (changedView == redView) {
                //需要让blueView跟随移动
                blueView.layout(blueView.getLeft() + dx, blueView.getTop() + dy, blueView.getRight() + dx,
                        blueView.getBottom() + dy);
            }

            //1.计算当前view偏移的百分比
            float fraction = changedView.getLeft() * 1f / horizontalRange;
            //2.根据偏移的百分比执行伴随动画
            executeAnim(fraction);
        }
    };

    /**
     * 执行动画
     * @param fraction
     */
    private void executeAnim(float fraction) {

    }

    @Override
    public void computeScroll() {
        if(viewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(DragLayout.this);
        }
    }

    /**
     * 摆放子View
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        int top = 0;
        redView.layout(left, top, left + redView.getMeasuredWidth(), top
                + redView.getMeasuredHeight());
        blueView.layout(left, redView.getBottom(), redView.getRight(),
                redView.getBottom() + blueView.getMeasuredHeight());
    }
}
