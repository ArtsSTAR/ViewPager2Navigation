import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CircleNavigationWidget extends View {

    private int mSelectColor = Color.WHITE;
    private int mNormalColor = Color.GRAY;
    private int mCirCleRadius = 20; // 默认圆半径
    private Paint mPaint; // 绘画选择部分的画笔
    private Path mPath; // 动画路径

    private int mCount = 0; // 定圆个数/页面数目 外部使用时，必须设定
    private ArrayList<Circle> mFixedCirclesList; // 定圆集合

    private int mCurrentPosition = 0; // VP2当前滑动到的页面, 默认为第一页
    private float mPercent = 0; // 页面当前滑动的百分比, 默认不滑动，即滑动比例为0

    private Point mCenterPoint; // 一个圆的中心圆点
    private Point[] mPointsArray; // 控制一个圆的四个数据点和八个控制点，解释图可看drawable的圆点图,P0,P3,P6,P9为数据点
    /**
     * 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置，解释图可看drawable的圆点图
     */
//    private static final float M = 0.551915024494f;
    private static float M = 0.551915024494f;

    // 以下几个变量主要是为了使圆点居中显示在整个控件里，该方案可以使定长的该控件也实现居中显示
    private int mWidgetViewWidth;  //整个控件的宽
    private int mCircleItemWidth; // 整个控件宽除掉圆点数目，Item宽的大小


    public CircleNavigationWidget(Context context) {
        super(context);
        init();

    }

    public CircleNavigationWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleNavigationWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initPaint();
        mPath = new Path();
        mPointsArray = new Point[]{new Point(), new Point(), new Point(), new Point(), new Point(), new Point(),
                new Point(), new Point(), new Point(), new Point(), new Point(), new Point()};
    }

    private void initData() {
        // 初始化恒定中心点纵坐标
        mCenterPoint = new Point();
        mCenterPoint.Y = 2 * mCirCleRadius;
        if (mCount > 0) {
            mFixedCirclesList = new ArrayList<>();
            mCircleItemWidth = mWidgetViewWidth / mCount;
            // 初始化定圆数据
            for (int i = 0; i < mCount; i++) {
                Point p = new Point();
                p.X = 0.5F * mCircleItemWidth + (mCircleItemWidth * i);
                p.Y = mCenterPoint.Y;
                p.radius = mCirCleRadius;
                mFixedCirclesList.add(new Circle(p));
            }
            mCenterPoint.X = mFixedCirclesList.get(mCurrentPosition).p.X;
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mSelectColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidgetViewWidth = getMeasuredWidth();
        initData(); // 走构造函数时（初始化View），不测量View
        setMeasuredDimension(widthMeasureSpec, mCirCleRadius * 4); // 高度为半径4倍，使圆刚好居中
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制定圆
        for (int i = 0; i < mCount; i++) {
            Log.e(TAG, "onDraw: FiexedPos " + i + " mCurrPos " + mCurrentPosition);
            mFixedCirclesList.get(i).onDraw(canvas, mNormalColor);
//            if (mCurrentPosition == i) {
//                mFixedCirclesList.get(i).onDraw(canvas, mSelectColor);
//            } else {
//
//            }
        }
        // 初始化中心点横坐标
        mCenterPoint.X = mFixedCirclesList.get(mCurrentPosition).p.X;
        // 计算与绘制滑动圆（贝塞尔曲线）
        calculateBezierPathCircle();
        drawBezierPathCircle(canvas);

    }

    /**
     * 根据页面滑动的百分比，确定动态圆（贝塞尔曲线圆）中心点坐标
     *
     * @param x         页面滑动的百分比
     * @param mPosition 当前选中页位置
     */
    public void setAnimCircleBezierPathCenterPointCoordinate(Float x, int mPosition) {
        mPercent = x;
        mCurrentPosition = mPosition;
        if (mCenterPoint != null) {
            // 此处计算方式为itemWidth*数量+中心点距即让圆心处于Item的中点+偏移百分比*间距即ItemWidth
            mCenterPoint.X = mCircleItemWidth * mPosition + (mCircleItemWidth * x) + (0.5F * mCircleItemWidth);
        }
        invalidate(); // 绘制/刷新该控件
    }

    private void calculateFixedCirCleCoordinatesData() {
        // 控制点与数据点坐标计算，解释可见图R.drawable.圆点图
        mPointsArray[11].X = mCenterPoint.X - (M * mCirCleRadius);
        mPointsArray[11].Y = mCenterPoint.Y + mCirCleRadius;
        mPointsArray[0].X = mCenterPoint.X;
        mPointsArray[0].Y = mCenterPoint.Y + mCirCleRadius;
        mPointsArray[1].X = mCenterPoint.X + (M * mCirCleRadius);
        mPointsArray[1].Y = mCenterPoint.Y + mCirCleRadius;

        mPointsArray[2].X = mCenterPoint.X + mCirCleRadius;
        mPointsArray[2].Y = mCenterPoint.Y + (M * mCirCleRadius);
        mPointsArray[3].X = mCenterPoint.X + mCirCleRadius;
        mPointsArray[3].Y = mCenterPoint.Y;
        mPointsArray[4].X = mCenterPoint.X + mCirCleRadius;
        mPointsArray[4].Y = mCenterPoint.Y - (M * mCirCleRadius);

        mPointsArray[5].X = mCenterPoint.X + (M * mCirCleRadius);
        mPointsArray[5].Y = mCenterPoint.Y - mCirCleRadius;
        mPointsArray[6].X = mCenterPoint.X;
        mPointsArray[6].Y = mCenterPoint.Y - mCirCleRadius;
        mPointsArray[7].X = mCenterPoint.X - (M * mCirCleRadius);
        mPointsArray[7].Y = mCenterPoint.Y - mCirCleRadius;

        mPointsArray[8].X = mCenterPoint.X - mCirCleRadius;
        mPointsArray[8].Y = mCenterPoint.Y - (M * mCirCleRadius);
        mPointsArray[9].X = mCenterPoint.X - mCirCleRadius;
        mPointsArray[9].Y = mCenterPoint.Y;
        mPointsArray[10].X = mCenterPoint.X - mCirCleRadius;
        mPointsArray[10].Y = mCenterPoint.Y + (M * mCirCleRadius);
    }

    private void calculateBezierPathCircle() {
        Log.e(TAG, "centerPoint.X " + mCenterPoint.X + " A");
        // 计算中心点坐标及控制点和数据点坐标
        mCenterPoint.X = mFixedCirclesList.get(mCurrentPosition).p.X;
        calculateFixedCirCleCoordinatesData();
        // 当页面滑动百分比小于0.4时，只更新右边组(2 -> 4)控制圆的点坐标
        if (mPercent < 0.4) {
            M = 0.551915024494f;
            Log.e(TAG, "centerPoint.X " + mCenterPoint.X + " B");
            // 如果圆滑动比例长度，没有超过半径，不绘制贝塞尔曲线
            if (mCircleItemWidth * mPercent > mCirCleRadius) {
                mPointsArray[2].X = mCenterPoint.X + mCircleItemWidth * mPercent;
                mPointsArray[3].X = mCenterPoint.X + mCircleItemWidth * mPercent;
                mPointsArray[4].X = mCenterPoint.X + mCircleItemWidth * mPercent;
            }
        } else if (mPercent < 0.6) { // 当滑动百分比大于0.4，小于0.6时，更新贝塞尔圆状态为椭圆
            M = 0.751915024494f;
            // 更新中心点坐标及控制点和数据点坐标
            mCenterPoint.X = mFixedCirclesList.get(mCurrentPosition).p.X + (mPercent * mCircleItemWidth);
            calculateFixedCirCleCoordinatesData();
//            // 绘制椭圆 8->10
//            mPointsArray[8].X = mCenterPoint.X - (mCircleItemWidth * 0.25F);
//            mPointsArray[9].X = mCenterPoint.X - (mCircleItemWidth * 0.25F);
//            mPointsArray[10].X = mCenterPoint.X - (mCircleItemWidth * 0.25F);
//            // 绘制椭圆 2->4
//            mPointsArray[2].X = mCenterPoint.X + (mCircleItemWidth * 0.25F);
//            mPointsArray[3].X = mCenterPoint.X + (mCircleItemWidth * 0.25F);
//            mPointsArray[4].X = mCenterPoint.X + (mCircleItemWidth * 0.25F);
            mPointsArray[8].X = mFixedCirclesList.get(mCurrentPosition).p.X - mCirCleRadius + (mCircleItemWidth * (mPercent - 0.4F) * (0.35F / 0.2F));
            mPointsArray[9].X = mFixedCirclesList.get(mCurrentPosition).p.X - mCirCleRadius + (mCircleItemWidth * (mPercent - 0.4F) * (0.35F / 0.2F));
            mPointsArray[10].X = mFixedCirclesList.get(mCurrentPosition).p.X - mCirCleRadius + (mCircleItemWidth * (mPercent - 0.4F) * (0.35F / 0.2F));

            mPointsArray[2].X = (float) (mFixedCirclesList.get(mCurrentPosition).p.X + 0.75F * mCircleItemWidth + ((0.25F * mCircleItemWidth) * ((mPercent - 0.4F) * 5.0F))) + mCirCleRadius;
            mPointsArray[3].X = (float)  (mFixedCirclesList.get(mCurrentPosition).p.X + 0.75F * mCircleItemWidth + ((0.25F * mCircleItemWidth) * ((mPercent - 0.4F) * 5.0F))) + mCirCleRadius;
            mPointsArray[4].X = (float)  (mFixedCirclesList.get(mCurrentPosition).p.X + 0.75F * mCircleItemWidth + ((0.25F * mCircleItemWidth) * ((mPercent - 0.4F) * 5.0F))) + mCirCleRadius;
//            mPointsArray[4].X = (float) (mCenterPoint.X  + mCirCleRadius + ((mCircleItemWidth - mCirCleRadius) - (0.35F * mCircleItemWidth)) * ((mPercent - 0.4F) * 5.0F));

        } else if (mPercent < 1) {
            M = 0.551915024494f;
            // 更新中心点坐标及控制点和数据点坐标
            mCenterPoint.X = mFixedCirclesList.get(mCurrentPosition).p.X + (1.0F * mCircleItemWidth);
            calculateFixedCirCleCoordinatesData();
            // 当滑动百分比大于0.8时，只更新左边组(8 -> 10)控制圆的点坐标
            // 左边组横坐标从0.6 * itemWidth 开始滑动，如果左边组横坐标小于定圆坐标不再绘制贝塞尔曲线圆
            if ((1 - mPercent) * mCircleItemWidth > mCirCleRadius) {
                mPointsArray[8].X = mCenterPoint.X - (mCircleItemWidth * (1 - mPercent));
                mPointsArray[9].X = mCenterPoint.X - (mCircleItemWidth * (1 - mPercent));
                mPointsArray[10].X = mCenterPoint.X - (mCircleItemWidth * (1 - mPercent));
            }
        }
    }

    /**
     * 绘制贝塞尔曲线
     *
     * @param canvas
     */
    private void drawBezierPathCircle(Canvas canvas) {
        // 路径重置
        mPath.reset();
        //0
        mPath.moveTo(mPointsArray[0].X, mPointsArray[0].Y);
        //0-3
        mPath.cubicTo(mPointsArray[1].X, mPointsArray[1].Y, mPointsArray[2].X, mPointsArray[2].Y, mPointsArray[3].X, mPointsArray[3].Y);
        //3-6
        mPath.cubicTo(mPointsArray[4].X, mPointsArray[4].Y, mPointsArray[5].X, mPointsArray[5].Y, mPointsArray[6].X, mPointsArray[6].Y);
        //6-9
        mPath.cubicTo(mPointsArray[7].X, mPointsArray[7].Y, mPointsArray[8].X, mPointsArray[8].Y, mPointsArray[9].X, mPointsArray[9].Y);
        //9-0
        mPath.cubicTo(mPointsArray[10].X, mPointsArray[10].Y, mPointsArray[11].X, mPointsArray[11].Y, mPointsArray[0].X, mPointsArray[0].Y);
        // 绘制曲线
        canvas.drawPath(mPath, mPaint);
    }


    public int getSelectColor() {
        return mSelectColor;
    }

    public void setSelectColor(int mSelectColor) {
        this.mSelectColor = mSelectColor;
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int mNormalColor) {
        this.mNormalColor = mNormalColor;
    }

    public int getCirCleRadius() {
        return mCirCleRadius;
    }

    public void setCirCleRadius(int mCirCleRadius) {
        this.mCirCleRadius = mCirCleRadius;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float mPercent) {
        this.mPercent = mPercent;
    }

    public Point getCenterPoint() {
        return mCenterPoint;
    }

    public void setCenterPoint(Point mCenterPoint) {
        this.mCenterPoint = mCenterPoint;
    }

    public int getWidgetViewWidth() {
        return mWidgetViewWidth;
    }

    public void setWidgetViewWidth(int mWidgetViewWidth) {
        this.mWidgetViewWidth = mWidgetViewWidth;
    }
}
