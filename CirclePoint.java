// to do package
import android.graphics.Canvas;
import android.graphics.Paint;

// 定点圆抽象化
public class CirclePoint {
    private P p;
    private Paint paint;

    public CirclePoint() {
        initPaint();
    }

    public P getP() {
        return p;
    }

    public void setP(P p) {
        this.p = p;
    }

    public void onDraw(Canvas canvas,int color){
        if(p == null) return;
        paint.setColor(color);
        canvas.drawCircle(p.X,p.Y,p.radius,paint);

    }

    private void initPaint(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(2);
    }
}
