package aero.xpand;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.shapes.OvalShape;

public class ComponentILS {

    private final int CANVAS_SAVE_LOC = 1;
    private final int CANVAS_SAVE_GLIDE = 2;
    private Paint paint;

    private float mWidth; // width of the canvas
    private float mHeight; // height of the canvas
    private float mPosX;
    private float mPosY;
    private float centerX, centerY;
    private Context context;

    private Path reticleGlide, reticleLoc;
    private Path diamondGlide, diamondLoc;
    private PointF[] dots;
    private float maxDevRatio = 0.77f;   // deviation maximum on given width or height
    public float devLoc, devGlide;  // deviation in degrees, between -2.5f and +2.5f
    private float devX, devY;       // actual deviation of diamonds, expressed in % of width
    private boolean locModeFill;    // when -1.25° < diamond < 1.25°
    private boolean gsModeFill;     // when -1.25° < diamond < 1.25°
    private boolean init_ok = false;

    public ComponentILS(Context context, float x, float y, float width, float height) {
        // values passed are x, y, width and height of the ADI
        // vertical ILS is displayed right of the ADI, horizonal ILS is below the ADI
        this.context = context;
        this.mPosX = x;
        this.mPosY = y;
        this.mWidth = width;
        this.mHeight = height;
        centerX = width / 2f;
        centerY = mHeight / 2f;
        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        initGraphics();

    }

    private void initGraphics() {
        // local coordinates
        reticleGlide = new Path();
        reticleGlide.moveTo(mWidth, centerY);
        reticleGlide.lineTo(mWidth * 1.08f, centerY);
        reticleGlide.close();

        reticleLoc = new Path();
        reticleLoc.moveTo(centerX, mHeight);
        reticleLoc.lineTo(centerX, mHeight * 1.08f);
        reticleLoc.close();
        dots = new PointF[8];
        // Loc
        dots[0] = new PointF(centerX - mWidth * maxDevRatio * 0.4f, mHeight * 1.04f);
        dots[1] = new PointF(centerX - mWidth * maxDevRatio * 0.2f, mHeight * 1.04f);
        dots[2] = new PointF(centerX + mWidth * maxDevRatio * 0.2f, mHeight * 1.04f);
        dots[3] = new PointF(centerX + mWidth * maxDevRatio * 0.4f, mHeight * 1.04f);
        // glide
        dots[4] = new PointF(mWidth * 1.04f, centerY - mHeight * maxDevRatio * 0.4f);
        dots[5] = new PointF(mWidth * 1.04f, centerY - mHeight * maxDevRatio * 0.2f);
        dots[6] = new PointF(mWidth * 1.04f, centerY + mHeight * maxDevRatio * 0.2f);
        dots[7] = new PointF(mWidth * 1.04f, centerY + mHeight * maxDevRatio * 0.4f);

        //diamondLoc
        diamondLoc = new Path();
        diamondLoc.moveTo(centerX, mHeight * 1.01f);
        diamondLoc.lineTo(centerX * 1.1f, mHeight * 1.04f);
        diamondLoc.lineTo(centerX, mHeight * 1.07f);
        diamondLoc.lineTo(centerX * 0.9f, mHeight * 1.04f);
        diamondLoc.close();

        //diamondGlide
        diamondGlide = new Path();
        diamondGlide.moveTo(mWidth * 1.01f, centerY);
        diamondGlide.lineTo(mWidth * 1.04f, centerY * 0.9f);
        diamondGlide.lineTo(mWidth * 1.07f, centerY);
        diamondGlide.lineTo(mWidth * 1.04f, centerY * 1.1f);
        diamondGlide.close();

        init_ok = true;
    }

    public void updateComponent(Canvas canvas) {
        // compute deviation on X and Y scales (dots)
        locModeFill = (devLoc >= -2.4f && devLoc <= 2.4f) ? true : false;
        if (devLoc <= -2.4999f) {
            devX = (-mWidth / 2f) * maxDevRatio;
        } else if (devLoc > -2.4999f && devLoc < 2.4999f) {
            devX = (mWidth / 2f) * maxDevRatio * devLoc / 2.5f;
        }
        if (devLoc >= 2.4999f) {
            devX = (mWidth / 2f) * maxDevRatio;
        }

        gsModeFill = (devGlide >= -2.4f && devGlide <= 2.4f) ? true : false;
        if (devGlide <= -2.4999f) {
            devY = (-mHeight / 2f) * maxDevRatio;
        } else if (devGlide > -2.4999f && devGlide < 2.4999f) {
            devY = (mHeight / 2f) * maxDevRatio * devGlide / 2.5f;
        }
        if (devGlide >= 2.4999f) {
            devY = (mHeight / 2f) * maxDevRatio;
        }
        draw(canvas);
    }

    /**
     * Draws the elements
     */
    private void draw(Canvas canvas) {
        if (!init_ok) {
            return;
        }
        canvas.save();
        canvas.translate(mPosX, mPosY);

        // draw diamonds
        paint.setColor(Color.MAGENTA);
        paint.setStrokeWidth(mHeight * 0.01f);

        canvas.translate(devX, 0f);
        paint.setStyle(locModeFill ? Style.FILL : Style.STROKE);

        canvas.drawPath(diamondLoc, paint);
        canvas.translate(-devX, 0f);

        canvas.translate(0f, devY);
        paint.setStyle(gsModeFill ? Style.FILL : Style.STROKE);
        canvas.drawPath(diamondGlide, paint);
        canvas.translate(0f, -devY);

        // draw reticles
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.STROKE);
        canvas.drawPath(reticleGlide, paint);
        canvas.drawPath(reticleLoc, paint);

        // draw dots
        paint.setStrokeWidth(mHeight * 0.007f);
        for (int i = 0; i < dots.length; i++) {
            canvas.drawCircle(dots[i].x, dots[i].y, mWidth * 0.015f, paint);
        }
        canvas.restore();
    }
}
