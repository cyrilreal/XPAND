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
import android.graphics.Region;
import android.graphics.Typeface;

public class ComponentSpeed {

	private Context context;
	private Paint paint, paintText, paintTextReticle;

	private int mWidth; // width of the component
	private int mHeight; // height of the component
	private int mPosX;
	private int mPosY;

	private float speedAmplitude; // amplitude of speed scale
	private float pixelPerUnit; // number of pixel per speed unit
	private float gradLineLength;
	private int[] values; // values of speeds to display
	private PointF[] positions; // positions of strings to display

	private Path[] paths; // paths to draw graduations lines
	private Path reticle;
	private float reticleRatio; // scale of the reticle based on canvas height
	private Rect background;
	private Rect textBounds; // to measure the size of text
	private Rect textBoundsReticle;

	private int[] singleDigits; // resources ids for single digits(0123456789)

	private Bitmap[] singleBmp = new Bitmap[10];
	private Bitmap[] scaledSingleBmp = new Bitmap[10];
	private Bitmap[] scaledSingleBmpBig = new Bitmap[10];

	private int lowestOne; // lowest unit value for reticle
	private int lowestTen; // lowest ten value for reticle
	private int lowestHundred;

	private int[] reticleBmpIndices1 = new int[5];
	private int[] reticleBmpIndices10 = new int[3];
	private int[] reticleBmpIndices100 = new int[3];

	private boolean init_ok = false;

	public float mSpeed;
	private int speedTenRef;

	public ComponentSpeed(Context context, int x, int y, int w, int h) {
		this.context = context;
		this.mPosX = x;
		this.mPosY = y;
		this.mWidth = w;
		this.mHeight = h;

		singleDigits = new int[] { R.drawable.aa_fig_0, R.drawable.aa_fig_1,
				R.drawable.aa_fig_2, R.drawable.aa_fig_3, R.drawable.aa_fig_4,
				R.drawable.aa_fig_5, R.drawable.aa_fig_6, R.drawable.aa_fig_7,
				R.drawable.aa_fig_8, R.drawable.aa_fig_9 };

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

		speedAmplitude = 120f; //
		values = new int[(int) (speedAmplitude / 10) + 1];
		positions = new PointF[(int) (speedAmplitude / 10) + 1];
		paths = new Path[(int) (speedAmplitude / 10) + 1];

		textBounds = new Rect();
		textBoundsReticle = new Rect();

		initGraphics();
	}

	private void initGraphics() {
		// init background
		background = new Rect(mPosX, mPosY, mPosX
				+ mWidth, mPosY + mHeight);

		gradLineLength = mWidth / 8f;
		paint.setStrokeWidth(mHeight / 160f);

		paintText.setTextSize(mWidth / 4f);
		paintTextReticle.setTextSize(mWidth / 2.8f);

		paintText.getTextBounds("000", 0, 3, textBounds);
		paintTextReticle.getTextBounds("000", 0, 3, textBoundsReticle);

		// init point array
		for (int i = 0; i < positions.length; i++) {
			positions[i] = new PointF();
		}

		// init path array
		for (int i = 0; i < paths.length; i++) {
			paths[i] = new Path();
		}

		pixelPerUnit = mHeight / speedAmplitude;

		// init reticle
		reticleRatio = mHeight / 8f;
		reticle = new Path();
		reticle.moveTo(0f, mHeight / 2f - reticleRatio / 2f);
		reticle.lineTo(0f, mHeight / 2f + reticleRatio / 2f);
		reticle.lineTo(
				mWidth - gradLineLength * 2f,
				mHeight / 2f + reticleRatio / 2f);
		reticle.lineTo(
				mWidth - gradLineLength * 2f,
				mHeight / 2f + gradLineLength);
		reticle.lineTo(mWidth - gradLineLength, mHeight / 2);
		reticle.lineTo(mWidth - gradLineLength * 2f,
				mHeight / 2f - gradLineLength);
		reticle.lineTo(
				mWidth - gradLineLength * 2f,
				mHeight / 2f - reticleRatio / 2f);
		reticle.lineTo(0f, mHeight / 2f - reticleRatio / 2f);
		reticle.close();

		// init bitmaps arrays
		for (int i = 0; i < singleBmp.length; i++) {
			singleBmp[i] = BitmapFactory.decodeResource(context.getResources(),
					singleDigits[i]);
		}

		for (int i = 0; i < scaledSingleBmp.length; i++) {
			scaledSingleBmp[i] = Bitmap.createScaledBitmap(
					singleBmp[i],
					(int) (mHeight * 3 / 4f / 20f),
					mHeight / 20,
					true);
		}

		for (int i = 0; i < scaledSingleBmpBig.length; i++) {
			scaledSingleBmpBig[i] = Bitmap.createScaledBitmap(
					singleBmp[i],
					(int) (mHeight * 3 / 4f / 16f),
					mHeight / 16,
					true);
		}

		init_ok = true;
	}

	public void updateComponent(Canvas canvas) {
		updateSpeedScale();
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
		canvas.drawRect(0, 0, mWidth, mHeight, paint);

		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);

		// draw strings
		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0 || (values[i] % 20 != 0 && values[i] != 0)) {
				continue;
			}
			canvas.drawText(
					buildSpeedValue(values[i]),
					mWidth - gradLineLength * 2f - textBounds.width(),
					positions[i].y + textBounds.height() / 2,
					paintText);
		}

		// draw lines
		for (int i = 0; i < paths.length; i++) {
			if (values[i] < 0) {
				continue;
			}
			canvas.drawPath(paths[i], paint);
		}

		// draw reticle
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawPath(reticle, paint);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		canvas.drawPath(reticle, paint);

		canvas.clipPath(reticle);

		for (int i = 0; i < reticleBmpIndices100.length; i++) {
			// dot not draw zero if speed < 100
			if (mSpeed < 100 && reticleBmpIndices100[i] == 0) {
				continue;
			}
			canvas.drawBitmap(
					scaledSingleBmpBig[reticleBmpIndices100[i]],
					0,
					mHeight / 2f - (i - 1)
							* scaledSingleBmpBig[0].getHeight()
							* (1 + 1 / 2f)
							- scaledSingleBmpBig[0].getHeight() / 2f
							+ getDeltaUnit(100),
					paint);
		}

		for (int i = 0; i < reticleBmpIndices10.length; i++) {
			// dot not draw zero if speed < 10
			if (mSpeed < 10 && reticleBmpIndices10[i] == 0) {
				continue;
			}
			canvas.drawBitmap(
					scaledSingleBmpBig[reticleBmpIndices10[i]],
					scaledSingleBmpBig[0].getWidth(),
					mHeight / 2f - (i - 1)
							* scaledSingleBmpBig[0].getHeight()
							* (1 + 1 / 2f)
							- scaledSingleBmpBig[0].getHeight() / 2f
							+ getDeltaUnit(10),
					paint);
		}

		for (int i = 0; i < reticleBmpIndices1.length; i++) {
			canvas.drawBitmap(
					scaledSingleBmpBig[reticleBmpIndices1[i]],
					scaledSingleBmpBig[0].getWidth() * 2,
					mHeight / 2f + scaledSingleBmpBig[0].getHeight()
							/ 2f
							- (i - 1) * scaledSingleBmpBig[0].getHeight()
							+ getDeltaUnit(1),
					paint);
		}

		canvas.restore();
		
		// "de-clip" the canvas
		canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight(),
				Region.Op.REPLACE);
	}

	private void updateSpeedScale() {
		// speed -= 0.1f;
		// round speed to the lowest tens
		speedTenRef = (((int) (mSpeed / 10)) * 10);

		for (int i = 0; i < values.length; i++) {
			values[i] = speedTenRef - 60 + i * 10;
		}

		for (int i = 0; i < positions.length; i++) {
			positions[i].x = mWidth - gradLineLength;
			positions[i].y = (mSpeed - values[i]) * pixelPerUnit
					+ mHeight / 2;
		}

		for (int i = 0; i < paths.length; i++) {
			paths[i].reset();
			paths[i].moveTo(positions[i].x, positions[i].y);
			paths[i].lineTo(mWidth, positions[i].y);
			paths[i].close();
		}

		// reticle bitmap array
		buildBmpArray1(reticleBmpIndices1, mSpeed);
		buildBmpArray10(reticleBmpIndices10, mSpeed);
		buildBmpArray100(reticleBmpIndices100, mSpeed);
	}

	private float getDeltaUnit(int step) {
		switch (step) {
		case 1:
			return scaledSingleBmpBig[0].getHeight()
					* (mSpeed - ((int) mSpeed));

		case 10:
			// scroll only when 1 to go
			if (((int) mSpeed) % 10 >= 9) {
				return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
						* (mSpeed - (int) mSpeed);
			}
			return 0;

		case 100:
			// scroll only when 1 to go
			if (((int) mSpeed) % 100 >= 99) {
				return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
						* (mSpeed - (int) mSpeed);
			}
			return 0;

		default:
			return 0;
		}
	}

	private void buildBmpArray1(int[] array, float value) {
		// compute lowest unit with integer part of alt only
		if (value < 10)
			lowestOne = (int) (value);
		else
			lowestOne = ((int) (value)) % 10;

		// fill array
		if (lowestOne - 2 < 0) {
			array[0] = scaledSingleBmpBig.length - lowestOne - 2;
		} else {
			array[0] = lowestOne - 2;
		}
		if (lowestOne - 1 < 0) {
			array[1] = scaledSingleBmpBig.length - lowestOne - 1;
		} else {
			array[1] = lowestOne - 1;
		}
		array[2] = lowestOne;
		if (lowestOne + 1 > scaledSingleBmpBig.length - 1) {
			array[3] = lowestOne + 1 - scaledSingleBmpBig.length;
		} else {
			array[3] = lowestOne + 1;
		}
		if (lowestOne + 2 > scaledSingleBmpBig.length - 1) {
			array[4] = lowestOne + 2 - scaledSingleBmpBig.length;
		} else {
			array[4] = lowestOne + 2;
		}
	}

	private void buildBmpArray10(int[] array, float value) {
		// compute lowest ten with integer part of alt only
		if (value < 100)
			lowestTen = (((int) (value) / 10) * 10);
		else
			lowestTen = (((int) (value % 100) / 10) * 10);

		// fill array
		if (lowestTen / 10 - 1 < 0) {
			array[0] = scaledSingleBmpBig.length - lowestTen / 10 - 1;
		} else {
			array[0] = lowestTen / 10 - 1;
		}
		array[1] = lowestTen / 10;
		if (lowestTen / 10 + 1 > scaledSingleBmpBig.length - 1) {
			array[2] = lowestTen / 10 + 1 - scaledSingleBmpBig.length;
		} else {
			array[2] = lowestTen / 10 + 1;
		}
	}

	private void buildBmpArray100(int[] array, float value) {
		// compute lowest hundred with integer part of alt only
		if (value < 1000)
			lowestHundred = (((int) (value) / 100) * 100);
		else
			lowestHundred = (((int) (value % 1000) / 100) * 100);

		// fill array
		if (lowestHundred / 100 - 1 < 0) {
			array[0] = scaledSingleBmpBig.length - lowestHundred / 100
					- 1;
		} else {
			array[0] = lowestHundred / 100 - 1;
		}
		array[1] = lowestHundred / 100;
		if (lowestHundred / 100 + 1 > scaledSingleBmpBig.length - 1) {
			array[2] = lowestHundred / 100 + 1
					- scaledSingleBmpBig.length;
		} else {
			array[2] = lowestHundred / 100 + 1;
		}
	}

	private String buildSpeedValue(int value) {
		if (value < 10) {
			return "  " + String.valueOf(value);
		}

		if (value < 100) {
			return " " + String.valueOf(value);
		}

		return String.valueOf(value);
	}

}
