package aero.xpand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

public class ComponentVsi {

    private Context context;
    private Paint paint, paintText, paintTextReticle;

    private float mWidth; // width of the component
    private float mHeight; // height of the component
    private float mPosX;
    private float mPosY;

    private int[] values; // values of vertical speed do display (path)
    private PointF[] positions; // positions of strings to display

    private Path[] paths; // paths to draw graduations lines
    private RectF background;
    private Rect textBounds; // to measure the size of text
    private Rect textBoundsRounded3, textBoundsRounded4;

    private float halfScaleHeight;
    private float needleEndPosY;
    private float graduationEnd;
    private boolean init_ok = false;

    public float mVerticalSpeed;
    private int mRoundedVs;     // vertical speed rounded to the nearest 50ft

    public ComponentVsi(Context context, float x, float y, float w, float h) {
        this.context = context;
        this.mPosX = x;
        this.mPosY = y;
        this.mWidth = w;
        this.mHeight = h;

        paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);

        paintText = new Paint();
        paintText.setColor(Color.WHITE);
        paintText.setAntiAlias(true);
        paintText.setTypeface(Typeface.MONOSPACE);

        paintTextReticle = new Paint();
        paintTextReticle.setColor(Color.WHITE);
        paintTextReticle.setAntiAlias(true);
        paintTextReticle.setTypeface(Typeface.MONOSPACE);

        values = new int[]{-6000, -4000, -2000, -1500, -1000, -500, 0, 500, 1000, 1500, 2000,
                4000, 6000};
        positions = new PointF[values.length];
        paths = new Path[values.length];

        textBounds = new Rect();
        textBoundsRounded3 = new Rect();
        textBoundsRounded4 = new Rect();

        halfScaleHeight = 0.9f * mHeight / 2;   // height of the half graduation scale
        initGraphics();
    }

    private void initGraphics() {
        // init background in local coordinates
        background = new RectF(mWidth / 24, mWidth / 24, mWidth - mWidth / 24, mHeight - mWidth / 24);

        paint.setStrokeWidth(mHeight / 160f);

        paintText.setTextSize(mWidth / 3.4f);

        paintText.getTextBounds("0", 0, 1, textBounds);
        paintText.getTextBounds("000", 0, 3, textBoundsRounded3);
        paintText.getTextBounds("0000", 0, 4, textBoundsRounded4);

        // init point array
        for (int i = 0; i < positions.length; i++) {
            positions[i] = new PointF();
            positions[i].x = mWidth / 24;
        }

        positions[0].y = mHeight / 2 + halfScaleHeight;
        positions[1].y = mHeight / 2 + halfScaleHeight * 0.88f;
        positions[2].y = mHeight / 2 + halfScaleHeight * 0.76f;
        positions[3].y = mHeight / 2 + halfScaleHeight * 0.58f;
        positions[4].y = mHeight / 2 + halfScaleHeight * 0.4f;
        positions[5].y = mHeight / 2 + halfScaleHeight * 0.2f;
        positions[6].y = mHeight / 2 + halfScaleHeight * 0.0f;
        positions[7].y = mHeight / 2 - halfScaleHeight * 0.2f;
        positions[8].y = mHeight / 2 - halfScaleHeight * 0.4f;
        positions[9].y = mHeight / 2 - halfScaleHeight * 0.58f;
        positions[10].y = mHeight / 2 - halfScaleHeight * 0.76f;
        positions[11].y = mHeight / 2 - halfScaleHeight * 0.88f;
        positions[12].y = mHeight / 2 - halfScaleHeight;

        // define graduation max X from textBounds and positions x
        graduationEnd = positions[0].x + textBounds.width() * 1.8f;

        // init path array
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path();
            paths[i].reset();
            paths[i].moveTo(positions[i].x + textBounds.width() * 1.3f, positions[i].y);
            if (values[i] == 0) // zero value dash is longer
                paths[i].lineTo(mWidth / 2, positions[i].y);
            else
                paths[i].lineTo(graduationEnd, positions[i].y);

        }

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

        paint.setStyle(Style.FILL);
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(background, paint);
//        canvas.drawRect(mWidth / 16, 0, mWidth - mWidth / 8, mHeight, paint);

        paint.setStyle(Style.STROKE);
        paint.setColor(Color.WHITE);

        // draw graduation strings
        for (int i = 0; i < values.length; i++) {
            int displayedUnit = Math.abs(values[i]);

            if (displayedUnit == 1000 || displayedUnit == 2000 || displayedUnit == 6000)
                canvas.drawText(
                        String.valueOf(Math.abs(values[i] / 1000)),
                        positions[i].x,
                        positions[i].y + textBounds.height() / 2,
                        paintText);
        }

        // draw lines
        for (int i = 0; i < paths.length; i++) {
            int displayedUnit = Math.abs(values[i]);
            if (displayedUnit == 1000 || displayedUnit == 2000 || displayedUnit == 6000)
                paint.setStrokeWidth(mHeight / 64);
            else
                paint.setStrokeWidth(mHeight / 128);
            canvas.drawPath(paths[i], paint);
        }

        // draw vertical speed value rounded to neareast 50ft
        int base = (int) Math.abs(mVerticalSpeed) / 100;
        int remainder = (int) Math.abs(mVerticalSpeed) % 100;
        if (remainder < 25)
            mRoundedVs = base * 100;
        else if (remainder < 75)
            mRoundedVs = base * 100 + 50;
        else
            mRoundedVs = base * 100 + 100;

        if (mVerticalSpeed > 0 && mRoundedVs > 950)
            canvas.drawText(String.valueOf(mRoundedVs), mWidth / 2 - textBoundsRounded4.width() / 2,
                    -textBoundsRounded4.height(), paintText);
        else if (mVerticalSpeed > 0 && mRoundedVs > 100)
            canvas.drawText(String.valueOf(mRoundedVs), mWidth / 2 - textBoundsRounded3.width() / 2,
                    -textBoundsRounded4.height(), paintText);
        else if (mVerticalSpeed < 0 && mRoundedVs > 950)
            canvas.drawText(String.valueOf(mRoundedVs), mWidth / 2 - textBoundsRounded4.width() / 2,
                    mHeight + textBoundsRounded4.height() * 2, paintText);
        else if (mVerticalSpeed < 0 && mRoundedVs > 100)
            canvas.drawText(String.valueOf(mRoundedVs), mWidth / 2 - textBoundsRounded3.width() / 2,
                    mHeight + textBoundsRounded3.height() * 2, paintText);

        // draw needle
        paint.setStrokeWidth(mHeight / 128);
        // compute needle's end position
        if (mVerticalSpeed == 0)
            needleEndPosY = mHeight / 2;
        if (mVerticalSpeed > 0 && mVerticalSpeed <= 1000)
            needleEndPosY = mHeight / 2 - halfScaleHeight * 0.4f * mVerticalSpeed / 1000;
        if (mVerticalSpeed > 1000 && mVerticalSpeed <= 2000)
            needleEndPosY = mHeight / 2 - halfScaleHeight * 0.4f - halfScaleHeight * 0.36f *
                    (mVerticalSpeed - 1000) / 1000;
        if (mVerticalSpeed > 2000 && mVerticalSpeed <= 6000)
            needleEndPosY = mHeight / 2 - halfScaleHeight * 0.76f - halfScaleHeight * 0.24f *
                    (mVerticalSpeed - 2000) / 4000;
        if (mVerticalSpeed > 6000)
            needleEndPosY = mHeight / 2 - halfScaleHeight;
        if (mVerticalSpeed < 0 && mVerticalSpeed >= -1000)
            needleEndPosY = mHeight / 2 - (mHeight / 2) * 0.4f * mVerticalSpeed / 1000;
        if (mVerticalSpeed < -1000 && mVerticalSpeed >= -2000)
            needleEndPosY = mHeight / 2 + halfScaleHeight * 0.4f + halfScaleHeight * 0.36f *
                    (-mVerticalSpeed - 1000) / 1000;
        if (mVerticalSpeed < -2000 && mVerticalSpeed >= -6000)
            needleEndPosY = mHeight / 2 + halfScaleHeight * 0.76f + halfScaleHeight * 0.24f *
                    (-mVerticalSpeed - 2000) / 4000;
        if (mVerticalSpeed < -6000)
            needleEndPosY = mHeight / 2 + halfScaleHeight;

        canvas.clipRect(0, 0, mWidth - mWidth / 24, mHeight);
        canvas.drawLine(mWidth * 1.5f, mHeight / 2, graduationEnd, needleEndPosY, paint);
        canvas.restore();
    }
}
