// to do package path

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle {
    Point p;
    Paint paint;

    public Circle() {
        initPaint();
    }

    public Circle(Point p) {
        this.p = p;
        initPaint();
    }


    public Point getP() {
        return p;
    }

    public void setP(Point p) {
        this.p = p;
    }

    public void onDraw(Canvas canvas, int color){
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
