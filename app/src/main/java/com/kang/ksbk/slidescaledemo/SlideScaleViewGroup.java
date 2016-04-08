package com.kang.ksbk.slidescaledemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by kang on 2016/4/7.
 */
public class SlideScaleViewGroup extends ViewGroup {
    private static final int TYPE_NONE= 0;
    private static final int TYPE_MOVE= 1;
    private static final int TYPE_ZOOM= 2;
    private static final int DEFWIDTH = 500;
    private static final int DEFHEIGHT = 1000;
    private static final int MAXWIDTH = 1000;
    private static final int MAXHEIGHT = 2000;
    private static final int MINWIDTH = 100;
    private static final int MINHEIGHT = 200;
    private int zoomwidth = DEFWIDTH;//实际宽度
    private int zoomheight = DEFHEIGHT;//实际高度
    private int mvMaxheight;//移动高度界限
    private int mvMaxwidth;//移动宽度界限
    private int screenwidth;//屏幕宽度
    private int screenheight;//屏幕高度

    Context context;
    private float startX;//起始x轴位置
    private float startY;//起始y轴位置
    private float positionX;//目前x轴位置
    private float positionY;//目前y轴位置
    private double startDis;//起始距离
    private double positionDis;//目前距离
    private double zoomTimes = 1.0;//缩放倍率


    private  int touchType = TYPE_NONE;

    public SlideScaleViewGroup(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SlideScaleViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        int top = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            //设置子布局的位置
            child.layout(left,top,left +zoomwidth,top + zoomheight);
            left = left + zoomwidth ;
        }
        mvMaxheight = zoomheight;
        mvMaxwidth = left;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            //设置子布局大小
            child.measure(zoomwidth,zoomheight);
        }
    }

    private void init() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        screenwidth = point.x;
        screenheight = point.y;
        LinearLayout linearLayout1 = new LinearLayout(context);
        linearLayout1.setBackgroundColor(Color.RED);
        linearLayout1.addView(new Button(context));
        addView(linearLayout1);

        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setBackgroundColor(Color.YELLOW);
        addView(linearLayout2);

        LinearLayout linearLayout3 = new LinearLayout(context);
        linearLayout3.setBackgroundColor(Color.BLUE);
        addView(linearLayout3);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                touchType = TYPE_MOVE;
                break;
            //多点触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                touchType = TYPE_ZOOM;
                startDis = getDistance(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchType ==TYPE_MOVE){
                    positionX = event.getX();
                    positionY = event.getY();
                    scrollBy((int)(positionX-startX),(int)(positionY-startY));
                    //控制移动范围
                    if (getScrollX()<=0){
                        scrollTo(0,getScrollY());
                    }
                    if (getScrollY()<=0){
                        scrollTo(getScrollX(),0);
                    }
                    if (getScrollY()>= mvMaxheight -screenheight){
                        scrollTo(getScrollX(),mvMaxheight -screenheight);
                    }
                    if (getScrollX()>=mvMaxwidth-screenwidth){
                        scrollTo(mvMaxwidth-screenwidth,getScrollY());
                    }
                    startX =positionX;
                    startY =positionY;
                }else if (touchType==TYPE_ZOOM){
                    positionDis = getDistance(event);
                    //计算缩放比例
                    zoomTimes = positionDis/startDis;
                    startDis = positionDis;
                    zoomwidth *= zoomTimes;
                    zoomheight *= zoomTimes;
                    //控制最大最小值
                    if (zoomheight>MAXHEIGHT||zoomwidth>MAXWIDTH){
                        zoomheight = MAXHEIGHT;
                        zoomwidth = MAXWIDTH;
                    }
                    if (zoomheight<MINHEIGHT||zoomwidth<MINWIDTH){
                        zoomheight = MINHEIGHT;
                        zoomwidth = MINWIDTH;
                    }
                    //重绘
                    requestLayout();

                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                touchType = TYPE_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                touchType = TYPE_NONE;
                break;

            default:
                return false;
        }
        return true;
    }

    private double getDistance(MotionEvent event){
        float x = event.getX(0)-event.getX(1);
        float y = event.getY(0)-event.getY(1);
        return Math.sqrt(x*x+y*y);
    }
}
