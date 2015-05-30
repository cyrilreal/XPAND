package aero.xpand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

public class ComponentAdi {

	private Paint paint;

	private float mWidth; // width of the canvas
	private float mHeight; // height of the canvas
	private float mPosX;
	private float mPosY;
	private float centerX, centerY;
	private Context context;

	private RectF rectFrame;
	private static final float RETICLE_THICKNESS_RATIO = 72;
	private static final float RETICLE_LENGHT_RATIO = 5;
	private static final float BANK_ANGLE_RETICLE_RATIO = 30;
	private static final float BANK_ANGLE_INDICATOR_RATIO = 20;
	private static final float SLIP_INDICATOR_RATIO = 40;
	private static final float LINE_THICKNESS_RATIO = 160f;
	private static final float LINE_10_LENGTH_RATIO = 2.2f;
	private static final float LINE_5_LENGTH_RATIO = 3.5f;
	private static final float LINE_2_5_LENGTH_RATIO = 7f;

	// smaller than frame's height (so frame's width has no influence)
	private RectF rectReticle;
	private Path pathReticleLeft, pathReticleRight;
	private RectF rectGround;
	private RectF rectSlip;

	private Path clipRoundRect;
	private Path clipHorizonUp; // prevent lines to be drawn on bank angle scale
	private Path clipHorizonDn;
	private Path lineHorizon;
	private Path[] linesUp10, linesUp5, linesUp2_5;
	private Path[] linesDn10, linesDn5, linesDn2_5;
	private PointF[] pointStringUpLeft, pointStringUpRight;
	private PointF[] pointStringDnLeft, pointStringDnRight;

	private float lineThickness;
	private float reticleThickness;
	private float reticleLength;
	private float bankAngleReticleHeight;
	private float bankAngleIndicatorHeight;
	private float slipIndicatorThickness;
	private float line10length;
	private float line5length;
	private float line2_5length;

	private Path lineRollShort, lineRollLong;
	private Path pathBankAngleReticle;
	private Path pathBankAngleIndicator;

	private int[] pitchValues; // resources ids for pitch(10, 20, 30 etc)
	private Bitmap[] bitmaps = new Bitmap[12];
	private Bitmap[] scaledBitmaps = new Bitmap[12];

	public float pitch = 0f;
	public float roll = 0f;
	public float slip = 0f;

	private boolean init_ok = false;

	public ComponentAdi(Context context, float x, float y, float width,
			float height) {

		this.context = context;
		this.mPosX = x;
		this.mPosY = y;
		this.mWidth = width;
		this.mHeight = height;
		this.centerX = width / 2f;
		this.centerY = height / 2f;

		pitchValues = new int[] { R.drawable.aa_num_10, R.drawable.aa_num_20,
				R.drawable.aa_num_30, R.drawable.aa_num_40,
				R.drawable.aa_num_50, R.drawable.aa_num_60,
				R.drawable.aa_num_70, R.drawable.aa_num_80,
				R.drawable.aa_num_90, R.drawable.aa_num_80,
				R.drawable.aa_num_70, R.drawable.aa_num_60 };

		paint = new Paint();
		paint.setFilterBitmap(true);
		paint.setAntiAlias(true);

		initGraphics();
	}

	private void initGraphics() {
		lineThickness = mHeight / LINE_THICKNESS_RATIO;
		if (lineThickness < 1) {
			lineThickness = 1;
		}
		reticleThickness = mHeight / RETICLE_THICKNESS_RATIO;
		reticleLength = mHeight / RETICLE_LENGHT_RATIO;
		bankAngleReticleHeight = mHeight / BANK_ANGLE_RETICLE_RATIO;
		bankAngleIndicatorHeight = mHeight / BANK_ANGLE_INDICATOR_RATIO;
		slipIndicatorThickness = mHeight / SLIP_INDICATOR_RATIO;
		line10length = mHeight / LINE_10_LENGTH_RATIO;
		line5length = mHeight / LINE_5_LENGTH_RATIO;
		line2_5length = mHeight / LINE_2_5_LENGTH_RATIO;

		// init bitmaps arrays
		for (int i = 0; i < bitmaps.length; i++) {
			bitmaps[i] = BitmapFactory.decodeResource(context.getResources(),
					pitchValues[i]);
		}

		for (int i = 0; i < scaledBitmaps.length; i++) {
			scaledBitmaps[i] = Bitmap.createScaledBitmap(
					bitmaps[i],
					(int) (mHeight / 20 + (mHeight / 20) / 2),
					(int) (mHeight / 20),
					true);
		}

		pointStringUpLeft = new PointF[12];
		pointStringUpRight = new PointF[12];
		pointStringDnLeft = new PointF[12];
		pointStringDnRight = new PointF[12];

		rectFrame = new RectF(0, 0, mWidth, mHeight);
		clipRoundRect = new Path();
		clipRoundRect.addRoundRect(
				rectFrame,
				mHeight / 30f,
				mHeight / 30f,
				Path.Direction.CCW);

		clipHorizonUp = new Path();
		clipHorizonUp
				.addCircle(
						centerX,
						centerY,
						(mHeight / 2) - bankAngleReticleHeight
								- bankAngleIndicatorHeight
								- slipIndicatorThickness * 2,
						Path.Direction.CCW);

		clipHorizonDn = new Path();
		clipHorizonDn.addRect(
				0,
				centerY,
				mWidth,
				mHeight,
				Path.Direction.CCW);

		// sizes based on height so that central reticle is always square even
		// if display is rectangle
		rectReticle = new RectF(
				centerX - reticleThickness,
				centerY - reticleThickness,
				centerX + reticleThickness,
				centerY + reticleThickness);

		pathReticleLeft = new Path();
		pathReticleLeft.moveTo(
				centerX - reticleLength * 2,
				centerY - reticleThickness);
		pathReticleLeft.lineTo(
				centerX - reticleLength,
				centerY - reticleThickness);
		pathReticleLeft.lineTo(
				centerX - reticleLength,
				centerY + 4 * reticleThickness);
		pathReticleLeft.lineTo(
				centerX - reticleLength - 2 * reticleThickness,
				centerY + 4 * reticleThickness);
		pathReticleLeft.lineTo(
				centerX - reticleLength - 2 * reticleThickness,
				centerY + reticleThickness);
		pathReticleLeft.lineTo(
				centerX - reticleLength * 2,
				centerY + reticleThickness);
		pathReticleLeft.close();

		pathReticleRight = new Path();
		pathReticleRight.moveTo(
				centerX + reticleLength * 2,
				centerY - reticleThickness);
		pathReticleRight.lineTo(
				centerX + reticleLength,
				centerY - reticleThickness);
		pathReticleRight.lineTo(
				centerX + reticleLength,
				centerY + 4 * reticleThickness);
		pathReticleRight.lineTo(
				centerX + reticleLength + 2 * reticleThickness,
				centerY + 4 * reticleThickness);
		pathReticleRight.lineTo(
				centerX + reticleLength + 2 * reticleThickness,
				centerY + reticleThickness);
		pathReticleRight.lineTo(
				centerX + reticleLength * 2,
				centerY + reticleThickness);
		pathReticleRight.close();

		// Ground rectangle is mCanvasHeight * 3.6, cause it displays 25? up and
		// down). It is referenced to zero
		rectGround = new RectF(
				-mWidth,
				0,
				mWidth * 2f,
				mHeight * 3.6f);

		// line of horizon (reference is zero)
		lineHorizon = new Path();
		lineHorizon.moveTo(-mWidth, 0);
		lineHorizon.lineTo(mWidth * 2f, 0);
		lineHorizon.close();

		// horizontal lines Up
		linesUp10 = new Path[12];
		for (int i = 0; i < linesUp10.length; i++) {
			linesUp10[i] = new Path();
			linesUp10[i].moveTo(
					centerX - line10length / 2,
					-rectGround.height() / 180f * (i * 10f + 10f));
			linesUp10[i].lineTo(
					centerX + line10length / 2,
					-rectGround.height() / 180f * (i * 10f + 10f));

			pointStringUpLeft[i] = new PointF(
					centerX - line10length / 2 - scaledBitmaps[i].getWidth()
							* 1.2f,
					-rectGround.height() / 180f * (i * 10f + 10f)
							- scaledBitmaps[i].getHeight() / 2f);

			pointStringUpRight[i] = new PointF(
					centerX + line10length / 2
							+ scaledBitmaps[i].getWidth() / 5f,
					-rectGround.height() / 180f * (i * 10f + 10f)
							- scaledBitmaps[i].getHeight() / 2f);
		}

		linesUp5 = new Path[12];
		for (int i = 0; i < linesUp5.length; i++) {
			linesUp5[i] = new Path();
			linesUp5[i].moveTo(
					centerX - line5length / 2,
					-rectGround.height() / 180f * (i * 10f + 5f));
			linesUp5[i].lineTo(
					centerX + line5length / 2,
					-rectGround.height() / 180f * (i * 10f + 5f));

		}

		linesUp2_5 = new Path[24];
		for (int i = 0; i < linesUp2_5.length; i++) {
			linesUp2_5[i] = new Path();
			linesUp2_5[i].moveTo(
					centerX - line2_5length / 2,
					-rectGround.height() / 180f * (i * 5f + 2.5f));
			linesUp2_5[i].lineTo(
					centerX + line2_5length / 2,
					-rectGround.height() / 180f * (i * 5f + 2.5f));
		}

		// horizontal lines Dn
		linesDn10 = new Path[12];
		for (int i = 0; i < linesDn10.length; i++) {
			linesDn10[i] = new Path();
			linesDn10[i].moveTo(
					centerX - line10length / 2,
					rectGround.height() / 180f * (i * 10f + 10f));
			linesDn10[i].lineTo(
					centerX + line10length / 2,
					rectGround.height() / 180f * (i * 10f + 10f));

			pointStringDnLeft[i] = new PointF(
					centerX - line10length / 2 - scaledBitmaps[i].getWidth()
							* 1.2f,
					rectGround.height() / 180f * (i * 10f + 10f)
							- scaledBitmaps[i].getHeight() / 2f);

			pointStringDnRight[i] = new PointF(
					centerX + line10length / 2
							+ scaledBitmaps[i].getWidth() / 5f,
					rectGround.height() / 180f * (i * 10f + 10f)
							- scaledBitmaps[i].getHeight() / 2f);
		}

		linesDn5 = new Path[12];
		for (int i = 0; i < linesDn5.length; i++) {
			linesDn5[i] = new Path();
			linesDn5[i].moveTo(
					centerX - line5length / 2,
					rectGround.height() / 180f * (i * 10f + 5f));
			linesDn5[i].lineTo(
					centerX + line5length / 2,
					rectGround.height() / 180f * (i * 10f + 5f));
		}

		linesDn2_5 = new Path[24];
		for (int i = 0; i < linesDn2_5.length; i++) {
			linesDn2_5[i] = new Path();
			linesDn2_5[i].moveTo(
					centerX - line2_5length / 2,
					rectGround.height() / 180f * (i * 5f + 2.5f));
			linesDn2_5[i].lineTo(
					centerX + line2_5length / 2,
					rectGround.height() / 180f * (i * 5f + 2.5f));
		}

		// path bankAngleReticle (based on height)
		pathBankAngleReticle = new Path();
		pathBankAngleReticle.moveTo(centerX + bankAngleReticleHeight, 0);
		pathBankAngleReticle.lineTo(centerX - bankAngleReticleHeight, 0);
		pathBankAngleReticle.lineTo(centerX, bankAngleReticleHeight);
		pathBankAngleReticle.close();

		// roll indicator lines
		lineRollShort = new Path();
		lineRollShort.moveTo(centerX, 0f);
		lineRollShort.lineTo(centerX, bankAngleReticleHeight);
		lineRollShort.close();

		lineRollLong = new Path();
		lineRollLong.moveTo(centerX, -bankAngleReticleHeight);
		lineRollLong.lineTo(centerX, bankAngleReticleHeight);
		lineRollLong.close();

		// path bankAngleIndicator
		pathBankAngleIndicator = new Path();
		pathBankAngleIndicator.moveTo(centerX, bankAngleReticleHeight
				+ lineThickness);
		pathBankAngleIndicator.lineTo(
				centerX + bankAngleIndicatorHeight,
				bankAngleReticleHeight + bankAngleIndicatorHeight
						+ lineThickness);
		pathBankAngleIndicator.lineTo(
				centerX - bankAngleIndicatorHeight,
				bankAngleReticleHeight + bankAngleIndicatorHeight
						+ lineThickness);
		pathBankAngleIndicator.close();

		// slip indicator
		rectSlip = new RectF(
				centerX - bankAngleIndicatorHeight,
				bankAngleReticleHeight + bankAngleIndicatorHeight
						+ lineThickness,
				centerX + bankAngleIndicatorHeight,
				bankAngleReticleHeight + bankAngleIndicatorHeight
						+ slipIndicatorThickness + lineThickness);

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
		canvas.translate(mPosX, mPosY);
		
		// draw a clipping rounded frame
		canvas.clipPath(clipRoundRect);
		// fill the inside with sky color
		canvas.drawColor(context.getResources()
				.getColor(R.color.colorSteelBlue));

		// rotate/translate canvas
		canvas.save();

		canvas.rotate(-roll, mWidth / 2f, mHeight / 2f);
		canvas.translate(
				0,
				mHeight / 2f + rectGround.height() / 180f * pitch);

		// draw ground rectangle
		paint.setColor(context.getResources().getColor(R.color.colorSienna));
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(rectGround, paint);

		// draw horizon
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(lineThickness);
		paint.setColor(Color.WHITE);
		canvas.drawPath(lineHorizon, paint);

		canvas.restore();

		// draw roll scale
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(pathBankAngleReticle, paint);

		paint.setStyle(Style.STROKE);
		// left part
		canvas.save();
		for (int i = 0; i < 6; i++) {
			canvas.rotate(-10f, mWidth / 2f, mHeight / 2f);
			// do not draw 40? and 50? lines
			if (i == 3 || i == 4) {
				continue;
			}

			if (i == 2 || i == 5) {
				canvas.drawPath(lineRollLong, paint);
			} else {
				canvas.drawPath(lineRollShort, paint);
			}
		}
		canvas.rotate(15f, mWidth / 2f, mHeight / 2f);
		canvas.drawPath(lineRollShort, paint);
		canvas.restore();

		// right part
		canvas.save();
		for (int i = 0; i < 6; i++) {
			canvas.rotate(10f, mWidth / 2f, mHeight / 2f);
			// do not draw 40? and 50? lines
			if (i == 3 || i == 4) {
				continue;
			}
			if (i == 2 || i == 5) {
				canvas.drawPath(lineRollLong, paint);
			} else {
				canvas.drawPath(lineRollShort, paint);
			}
		}
		canvas.rotate(-15f, mWidth / 2f, mHeight / 2f);
		canvas.drawPath(lineRollShort, paint);
		canvas.restore();

		// draw bank angle indicator
		canvas.save();

		paint.setStrokeJoin(Join.ROUND);
		canvas.rotate(-roll, mWidth / 2f, mHeight / 2f);
		canvas.drawPath(pathBankAngleIndicator, paint);
		canvas.restore();

		// draw slip indicator
		canvas.save();
		canvas.rotate(-roll, mWidth / 2f, mHeight / 2f);
		canvas.translate(slip * mWidth / 32f, 0f);
		canvas.drawRect(rectSlip, paint);
		canvas.restore();

		canvas.clipPath(clipHorizonUp);
		canvas.clipPath(clipHorizonDn, Region.Op.UNION);
		// draw lines Up
		canvas.save();
		canvas.rotate(-roll, mWidth / 2f, mHeight / 2f);
		canvas.translate(
				0,
				mHeight / 2f + rectGround.height() / 180f * +pitch);
		for (int i = 0; i < linesUp10.length; i++) {
			if (i * 10 + 10 > 30.0f + pitch) {
				break;
			}
			canvas.drawPath(linesUp10[i], paint);
			canvas.drawBitmap(
					scaledBitmaps[i],
					pointStringUpLeft[i].x,
					pointStringUpLeft[i].y,
					paint);
			canvas.drawBitmap(
					scaledBitmaps[i],
					pointStringUpRight[i].x,
					pointStringUpRight[i].y,
					paint);
		}

		for (int i = 0; i < linesUp5.length; i++) {
			if (i * 10 + 5 > 15.0f + pitch) {
				break;
			}
			canvas.drawPath(linesUp5[i], paint);

		}

		for (int i = 0; i < linesUp2_5.length; i++) {
			if (i * 5 + 2.5f > 15.0f + pitch) {
				break;
			}
			canvas.drawPath(linesUp2_5[i], paint);

		}
		// draw lines Dn
		for (int i = 0; i < linesDn10.length; i++) {
			if (i * 10 + 10 > 30.0f + pitch) {
				break;
			}
			canvas.drawPath(linesDn10[i], paint);
			canvas.drawBitmap(
					scaledBitmaps[i],
					pointStringDnLeft[i].x,
					pointStringDnLeft[i].y,
					paint);
			canvas.drawBitmap(
					scaledBitmaps[i],
					pointStringDnRight[i].x,
					pointStringDnRight[i].y,
					paint);
		}

		for (int i = 0; i < linesDn5.length; i++) {
			canvas.drawPath(linesDn5[i], paint);
		}

		for (int i = 0; i < linesDn2_5.length; i++) {
			canvas.drawPath(linesDn2_5[i], paint);
		}
		canvas.restore();
		// draw reticle
		canvas.clipRect(0, 0, mWidth, mHeight, Region.Op.REPLACE);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawRect(rectReticle, paint);
		canvas.drawPath(pathReticleLeft, paint);
		canvas.drawPath(pathReticleRight, paint);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2f);
		paint.setColor(Color.WHITE);
		canvas.drawRect(rectReticle, paint);
		canvas.drawPath(pathReticleLeft, paint);
		canvas.drawPath(pathReticleRight, paint);

	}

	public float getmWidth() {
		return mWidth;
	}

	public float getmHeight() {
		return mHeight;
	}

	public float getmPosX() {
		return mPosX;
	}

	public float getmPosY() {
		return mPosY;
	}
}
