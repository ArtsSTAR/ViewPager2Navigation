package;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * 新导航圆切换效果(Bezier曲线实现)
 * @author sm2884 yuxiangliu
 * @date 2022/08/09
 */
public class NavigationCircleContainer extends View {

    private ArrayList<CirclePoint> mCirclePointList; // 定点圆集合
    private int count; // 定点圆集合大小（即个数）
    private P p1, p2, p3, p4, p5; // 控制两个大圆的五个圆点坐标实例
    private P mAnimP; // 动态大圆的圆心坐标点实例
    private CirclePoint mAnimCirclePoint; // 动态大圆实例

    private float fixedPX, fixedPY, animPX, animPY; // 滑动前固定圆与动态圆坐标的xy值
    private double mBetweenFixedAndDynamicCircleDistance; // 固定圆与动态圆之间距离
    private int currentIndex = 0; // 当前选中的圆

    private int mSelectColor = Color.WHITE;
    private int mNormalColor = Color.GRAY;
    private int mCirCleRadius = 8; // 默认圆半径
    private Paint mPaint; // 绘画选择部分的画笔
    private Path mPath; // 动画路径

    private Context mContext;
    private int ScreenWidth;
    private int mKeepFixedCircleStateOffset = 0; // 保持固定圆状态的偏移量
    private int pointY = mCirCleRadius * 2; // 圆点坐标的Y轴值不变，为半径的2倍

    // 以下几个变量主要是为了使圆点居中显示在整个控件里，该方案可以使定长的该控件也实现居中显示
    private int mContainerViewWidth;  //整个控件的宽
    private int mCircleItemWidth; // 整个控件宽除掉圆点数目，Item宽的大小
    private int mCircleItemCenterLength; // 每个小Item中心点到Item两端的距离，即（mCircleItemWidth/2）

    // 转换切换页面进度时使用
//    private float scale;//屏幕宽度与当前View宽度的比例


    public NavigationCircleContainer(Context context) {
        super(context);
        init(context);
    }

    public NavigationCircleContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NavigationCircleContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        initPaint();
        initP();
        // 计算屏幕宽度
        calculationScreen();
        // 动画路径
        mPath = new Path();
        // 定圆集合
        mCirclePointList = new ArrayList<>();
        // 动画圆实例
        mAnimCirclePoint = new CirclePoint();
        // 动画圆的坐标实例
        mAnimP = new P();
    }


    //初始化画笔
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mSelectColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(2);
    }

    //初始化点实例
    private void initP() {
        p1 = new P();//定圆上的点
        p2 = new P();//定圆上的点
        p3 = new P();//动圆上的点
        p4 = new P();//动圆上的点
        p5 = new P();//两圆圆心上的中心点
    }

    //计算屏幕的尺寸
    private void calculationScreen() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        ScreenWidth = displayMetrics.widthPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mContainerViewWidth = getMeasuredWidth();
        initData();
        setMeasuredDimension(widthMeasureSpec, pointY * 2); // 高度为圆心点2倍，使其刚好居中
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCirclePointList == null || mCirclePointList.size() <= 0)
            return;
        // 绘制定圆
        for (int i = 0; i < mCirclePointList.size(); i++) {
            if (currentIndex == i) {
                mCirclePointList.get(i).onDraw(canvas, mSelectColor);
            } else {
                mCirclePointList.get(i).onDraw(canvas, mNormalColor);
            }
        }
        // 绘制动圆
        mAnimCirclePoint.onDraw(canvas, mSelectColor);
        // 计算Path路径
        calculationSelectPath();
        // 绘制Path路径
        canvas.drawPath(mPath, mPaint);
    }

    private void calculationSelectPath() {
        fixedPX = mCirclePointList.get(currentIndex).getP().X;
        fixedPY = mCirclePointList.get(currentIndex).getP().Y;
        animPX = mAnimCirclePoint.getP().X;
        animPY = mAnimCirclePoint.getP().Y;

        // 通过两个圆的圆心点坐标，计算出Bezier曲线（二阶）的五个点坐标
        calculationBezierControlCirclePoint(fixedPX, fixedPY, animPX, animPY);

        // 计算固定圆和动态圆之间距离
        mBetweenFixedAndDynamicCircleDistance = Math.abs(Math.sqrt(Math.pow(fixedPX - animPX, 2) + Math.pow(fixedPY - animPY, 2)));

        // 路径重置
        mPath.reset();

        if (mBetweenFixedAndDynamicCircleDistance <= mCirCleRadius * 2) {
            // 绘制矩形方框路径
            calculationRectanglePath();
        } else if (mBetweenFixedAndDynamicCircleDistance > mCirCleRadius * 2
                && mBetweenFixedAndDynamicCircleDistance < mCircleItemCenterLength - mKeepFixedCircleStateOffset) {
            //绘制贝塞尔曲线
            calculationBezierPath();
        } else if (mBetweenFixedAndDynamicCircleDistance > mCircleItemCenterLength - mKeepFixedCircleStateOffset
                && mBetweenFixedAndDynamicCircleDistance < mCircleItemCenterLength + mKeepFixedCircleStateOffset) {
            //取消绘制贝塞尔曲线
        } else if (mBetweenFixedAndDynamicCircleDistance >= mCircleItemCenterLength + mKeepFixedCircleStateOffset) {
            //切换下一个圆，以此圆为基础计算Path路径，然后绘制
            if (currentIndex <= mCirclePointList.size() - 1) {
                if (fixedPX > animPX) {//动圆位于当前圆的左侧
                    currentIndex = currentIndex - 1;
                } else {//动圆位于当前圆的右侧
                    currentIndex = currentIndex + 1;
                }
            }
        }

    }


    // 计算两圆心连线且过两圆心的垂线与园的交点的坐标
    // 通过两个圆的圆心点坐标，计算出Bezier曲线的控制点与数据点坐标（共五个，p5为控制点）
    private void calculationBezierControlCirclePoint(float pax, float pay, float pbx, float pby) {
        double a = Math.atan((pbx - pax) / (pby - pay));
        double sin = Math.sin(a);
        double cos = Math.cos(a);
        p1.Y = (float) (pay + (sin * mCirCleRadius));
        p1.X = (float) (pax - (cos * mCirCleRadius));

        p2.X = (float) (pax + cos * mCirCleRadius);
        p2.Y = (float) (pay - sin * mCirCleRadius);

        p3.X = (float) (pbx - cos * mCirCleRadius);
        p3.Y = (float) (pby + sin * mCirCleRadius);

        p4.X = (float) (pbx + cos * mCirCleRadius);
        p4.Y = (float) (pby - sin * mCirCleRadius);

        p5.X = (pax + pbx) / 2;
        p5.Y = (pay + pby) / 2;
    }

    //计算贝塞尔曲线Path
    private void calculationBezierPath() {
        mPath.moveTo(p1.X, p1.Y);
        mPath.quadTo(p5.X, p5.Y, p3.X, p3.Y);
        mPath.lineTo(p4.X, p4.Y);
        mPath.quadTo(p5.X, p5.Y, p2.X, p2.Y);
        mPath.lineTo(p1.X, p1.Y);
    }

    //计算矩形Path（用于动态圆和定点圆未完全脱离时绘制）
    private void calculationRectanglePath() {
        mPath.moveTo(p1.X, p1.Y);
        mPath.lineTo(p3.X, p3.Y);
        mPath.lineTo(p4.X, p4.Y);
        mPath.lineTo(p2.X, p2.Y);
        mPath.close();
    }

    public void setTranslateX(Float x,int mPosition) {
        if (mAnimCirclePoint != null && mAnimCirclePoint.getP() != null) {
            // 此处计算方式为itemWidth*数量+中心点距即让圆心处于Item的中点+偏移百分比*间距即ItemWidth
            mAnimCirclePoint.getP().X = mCircleItemWidth * mPosition + (mCircleItemWidth * x) + mCircleItemCenterLength;
//            mAnimCirclePoint.getP().X = x / scale + mCircleItemCenterLength;
        }
        invalidate();
    }

    // 相关数据初始化
    private void initData() {
        if (count > 0) {
            mCircleItemWidth = mContainerViewWidth / count;
//            scale = (float) ScreenWidth / (float) mCircleItemWidth;
            mCircleItemCenterLength = mCircleItemWidth / 2;
            mCirclePointList.clear();
            // 初始化定圆具体数据
            for (int i = 0; i < count; i++) {
                CirclePoint circlePoint = new CirclePoint();
                P p = new P();
                // item距离+二分之一item距离
                p.X = mCircleItemWidth * i + mCircleItemCenterLength;
                p.Y = pointY;
                p.radius = mCirCleRadius;
                circlePoint.setP(p);
                mCirclePointList.add(circlePoint);
            }
//            // 初始化动态圆具体数据
            mAnimP.X = mCircleItemCenterLength + mCircleItemWidth * currentIndex;
            mAnimP.Y = pointY;
            mAnimP.radius = mCirCleRadius;
            mAnimCirclePoint.setP(mAnimP);

        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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
        pointY = mCirCleRadius * 2;//设置所有圆形的Y坐标为半径的两倍
    }

    public int getKeepFixedCircleStateOffset() {
        return mKeepFixedCircleStateOffset;
    }

    public void setKeepFixedCircleStateOffset(int mKeepFixedCircleStateOffset) {
        this.mKeepFixedCircleStateOffset = mKeepFixedCircleStateOffset;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
}

