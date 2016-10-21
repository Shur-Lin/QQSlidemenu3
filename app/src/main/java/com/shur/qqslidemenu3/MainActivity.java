package com.shur.qqslidemenu3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.shur.qqslidemenu3.SlideMenu.OnDragStateChangeListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        ListView main_listview = (ListView) findViewById(R.id.main_listview);
        final ListView menu_listview = (ListView) findViewById(R.id.menu_listview);
        SlideMenu slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        final ImageView iv_head = (ImageView) findViewById(R.id.iv_head);
        MyLinearLayout my_layout = (MyLinearLayout) findViewById(R.id.my_layout);
        //绑定菜单
        my_layout.bindSlideMenu(slideMenu);
        //主界面listview数据适配
        main_listview.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, Constant.NAMES));
        //菜单界面listview数据适配
        menu_listview.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, Constant.sCheeseStrings){
            //重写getview方法
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });

        slideMenu.setOnDragStateChangeListener(new OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                menu_listview.smoothScrollToPosition(new Random().nextInt(Constant.sCheeseStrings.length));
            }
            @Override
            public void onDragging(float fraction) {
                ViewHelper.setAlpha(iv_head,1-fraction);
            }
            @Override
            public void onClose() {
                ViewPropertyAnimator.animate(iv_head).translationX(10)
                        .setInterpolator(new CycleInterpolator(4))
                        .setDuration(500)
                        .start();
            }
        });
    }
}
