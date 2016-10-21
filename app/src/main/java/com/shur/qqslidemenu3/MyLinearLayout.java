package com.shur.qqslidemenu3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.shur.qqslidemenu3.SlideMenu.DragState;

/**
 * Created by Shur on 2016/10/20.
 */

public class MyLinearLayout extends LinearLayout {

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context) {
        super(context);
    }

    private SlideMenu slideMenu;
    /**
     * 绑定滑动菜单
     * @param slideMenu
     */
    public void bindSlideMenu(SlideMenu slideMenu){
        this.slideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(slideMenu!=null && slideMenu.getDragState()==DragState.Open){
            //当SLidemenu处于打开状态的时候，需要拦截触摸事件
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(slideMenu!=null && slideMenu.getDragState()==DragState.Open){
            //当SLidemenu处于打开状态的时候，需要拦截触摸事件,并且消费掉触摸事件
            if(event.getAction()==MotionEvent.ACTION_UP){
                //如果抬起，则关闭SlideMenu
                slideMenu.close();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

}
