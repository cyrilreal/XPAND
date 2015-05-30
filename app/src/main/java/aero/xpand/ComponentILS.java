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

    private Paint paint;

    private float mWidth; // width of the canvas
    private float mHeight; // height of the canvas
    private float mPosX;
    private float mPosY;
    private Context context;

    private Path reticleGlide, reticleLoc;
    private PointF[] dots;

    private boolean init_ok = false;

    public ComponentILS(Context context, float x, float y, float width, float height) {
        // values passed are x, y, width and height of the ADI
        // vertical ILS is displayed right of the ADI, horizonal ILS is below the ADI
        this.context = context;
        this.mPosX = x;
        this.mPosY = y;
        this.mWidth = width;
        this.mHeight = height;

        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        initGraphics();
    }

    private void initGraphics() {
        // local coordinates
        reticleGlide = new Path();
        reticleGlide.moveTo(mWidth, mHeight / 2);
        reticleGlide.lineTo(mWidth * 1.08f, mHeight / 2);
        reticleGlide.close();

        reticleLoc = new Path();
        reticleLoc.moveTo(mWidth / 2, mHeight);
        reticleLoc.lineTo(mWidth / 2, mHeight * 1.08f);
        reticleLoc.close();
        dots = new PointF[8];
        // Loc
        dots[0] = new PointF(mWidth * 0.20f, mHeight * 1.04f);
        dots[1] = new PointF(mWidth * 0.35f, mHeight * 1.04f);
        dots[2] = new PointF(mWidth * 0.65f, mHeight * 1.04f);
        dots[3] = new PointF(mWidth * 0.80f, mHeight * 1.04f);
        // glide
        dots[4] = new PointF(mWidth * 1.04f, mHeight * 0.20f);
        dots[5] = new PointF(mWidth * 1.04f, mHeight * 0.35f);
        dots[6] = new PointF(mWidth * 1.04f, mHeight * 0.65f);
        dots[7] = new PointF(mWidth * 1.04f, mHeight * 0.80f);

        init_ok = true;
    }

    public void updateComponent(Canvas canvas) {
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
        // draw reticles
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(mHeight * 0.01f);
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
