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
import android.graphics.Typeface;

public class ComponentAltitude {

	public boolean init_ok = false;

	private Paint paint, paintText, paintTextReticle;

	private float mWidth = 1; // width of the canvas
	private float mHeight = 1; // height of the canvas
	private float mPosX, mPosY;

	private Context mContext;

	private float altAmplitude; // amplitude of alt scale
	private float pixelPerUnit; // number of pixel per altitude unit
	private float gradLineLength;
	private int[] values; // values of alt to display
	private int lowestTen; // lowest ten value for reticle
	private int lowestTwenty; // lowest twentiest value for reticle
	private int lowestHundred;
	private int lowestThousand;
	private int lowestTenThousand;

	private PointF[] positions; // positions of strings to display

	private Path[] paths; // paths to draw graduations lines
	private Path[] pathsThousand; // paths for thousand numbers (line above and
									// under)
	private Path reticle;
	private float reticleRatio; // scale of the reticle based on canvas height
	private Rect textBounds; // to measure the size of text
	private Rect textBoundsReticle;

	private int[] singleDigits; // resources ids for single digits(0123456789)
	private int[] dualDigits; // resources ids for dual digits(00, 10, 20, 30
								// etc.)

	private Bitmap[] singleBmp = new Bitmap[10];
	private Bitmap[] dualBmp = new Bitmap[10];
	private Bitmap[] scaledSingleBmp = new Bitmap[10];
	private Bitmap[] scaledSingleBmpBig = new Bitmap[10];
	private Bitmap[] scaledDualBmp = new Bitmap[10];

	private int[] reticleBmpIndices20 = new int[5]; // array holding indices of
													// bitmaps to be used
	private int[] reticleBmpIndices100 = new int[3];
	private int[] reticleBmpIndices1000 = new int[3];
	private int[] reticleBmpIndices10000 = new int[3];

	float alt = 0f;
	int altHundredRef;

	public ComponentAltitude(Context context, float x, float y, float width,
			float height) {

		mContext = context;
		mPosX = x;
		mPosY = y;
		mWidth = width;
		mHeight = height;

		singleDigits = new int[] { R.drawable.aa_fig_0, R.drawable.aa_fig_1,
				R.drawable.aa_fig_2, R.drawable.aa_fig_3, R.drawable.aa_fig_4,
				R.drawable.aa_fig_5, R.drawable.aa_fig_6, R.drawable.aa_fig_7,
				R.drawable.aa_fig_8, R.drawable.aa_fig_9 };

		dualDigits = new int[] { R.drawable.aa_num_00, R.drawable.aa_num_10,
				R.drawable.aa_num_20, R.drawable.aa_num_30,
				R.drawable.aa_num_40,
				R.drawable.aa_num_50, R.drawable.aa_num_60,
				R.drawable.aa_num_70,
				R.drawable.aa_num_80, R.drawable.aa_num_90 };

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

		altAmplitude = 800f; //
		values = new int[(int) (altAmplitude / 100) + 1];
		positions = new PointF[(int) (altAmplitude / 100) + 1];
		paths = new Path[(int) (altAmplitude / 100) + 1];
		pathsThousand = new Path[2]; // only two line necessary
		textBounds = new Rect();
		textBoundsReticle = new Rect();

		initGraphics();
	}

	private void initGraphics() {

		gradLineLength = mWidth / 10f;
		paint.setStrokeWidth(mHeight / 60f);

		paintText.setTextSize(mWidth / 5f);
		paintTextReticle.setTextSize(mWidth / 4f);

		paintText.getTextBounds("00000", 0, 3, textBounds);
		paintTextReticle.getTextBounds("000", 0, 3, textBoundsReticle);

		// init point array
		for (int i = 0; i < positions.length; i++) {
			positions[i] = new PointF();
		}

		// init path arrays
		for (int i = 0; i < paths.length; i++) {
			paths[i] = new Path();
		}

		for (int i = 0; i < 2; i++) {
			pathsThousand[i] = new Path();
		}

		pixelPerUnit = mHeight / altAmplitude;

		// init reticle
		reticleRatio = mHeight / 8f;
		reticle = new Path();
		reticle.moveTo(mWidth, mHeight / 2f - reticleRatio / 2f);
		reticle.lineTo(mWidth, mHeight / 2f + reticleRatio / 2f);
		reticle.lineTo(
				gradLineLength * 2f,
				mHeight / 2f + reticleRatio / 2f);
		reticle.lineTo(
				gradLineLength * 2f,
				mHeight / 2f + gradLineLength);
		reticle.lineTo(gradLineLength, mHeight / 2);
		reticle.lineTo(gradLineLength * 2f,
				mHeight / 2f - gradLineLength);
		reticle.lineTo(
				gradLineLength * 2f,
				mHeight / 2f - reticleRatio / 2f);
		reticle.lineTo(mWidth, mHeight / 2f - reticleRatio / 2f);
		reticle.close();

		// init bitmaps arrays
		for (int i = 0; i < singleBmp.length; i++) {
			singleBmp[i] = BitmapFactory.decodeResource(
					mContext.getResources(),
					singleDigits[i]);
		}

		for (int i = 0; i < dualBmp.length; i++) {
			dualBmp[i] = BitmapFactory.decodeResource(mContext.getResources(),
					dualDigits[i]);
		}

		for (int i = 0; i < scaledSingleBmp.length; i++) {
			scaledSingleBmp[i] = Bitmap.createScaledBitmap(
					singleBmp[i],
					(int) (mHeight * 3 / 4f / 20f),
					(int) (mHeight / 20),
					true);
		}

		for (int i = 0; i < scaledSingleBmpBig.length; i++) {
			scaledSingleBmpBig[i] = Bitmap.createScaledBitmap(
					singleBmp[i],
					(int) (mHeight * 3 / 4f / 16f),
					(int) (mHeight / 16),
					true);
		}

		for (int i = 0; i < scaledDualBmp.length; i++) {
			scaledDualBmp[i] = Bitmap.createScaledBitmap(
					dualBmp[i],
					(int) (mHeight * 3 / 2 / 20),
					(int) (mHeight / 20),
					true);
		}

		init_ok = true;
	}

	public void updateComponent(Canvas canvas) {
		updateAltScale();
		draw(canvas);
	}

	/**
	 * Draws the elements
	 */
	private void draw(Canvas canvas) {

		canvas.save();
		canvas.translate(mPosX, mPosY);
		
		// draw background rectangle
		paint.setColor(Color.DKGRAY);
		paint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, mWidth, mHeight, paint);

		// draw strings
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);

		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0 || (values[i] % 200 != 0 && values[i] != 0)) {
				continue;
			}
			canvas.drawText(
					buildAltValue(values[i]),
					gradLineLength * 1.5f,
					positions[i].y + textBounds.height() / 2f,
					paintText);
		}

		// draw lines
		for (int i = 0; i < paths.length; i++) {
			if (values[i] < 0) {
				continue;
			}
			// test if x500 or x000 (thick line)
			if (values[i] % 500 == 0) {
				paint.setStrokeWidth(mHeight / 50f);
			}
			else {
				paint.setStrokeWidth(mHeight / 160f);
			}
			canvas.drawPath(paths[i], paint);

			// test if value is multiple of thousand (add line above and
			// under number)
			if (values[i] % 1000 == 0) {
				paint.setStrokeWidth(mHeight / 120f);
				canvas.drawPath(pathsThousand[0], paint);
				canvas.drawPath(pathsThousand[1], paint);
			}
		}

		// draw reticle
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLACK);
		canvas.drawPath(reticle, paint);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(mHeight / 160f);
		paint.setColor(Color.WHITE);
		canvas.drawPath(reticle, paint);

		canvas.clipPath(reticle);

		for (int i = 0; i < reticleBmpIndices20.length; i++) {
			canvas.drawBitmap(
					scaledDualBmp[reticleBmpIndices20[i]],
					mWidth - scaledDualBmp[0].getWidth(),
					mHeight / 2f + scaledDualBmp[0].getHeight() / 2f
							- (i - 1) * scaledDualBmp[0].getHeight()
							+ getDeltaUnit(20),
					paint);
		}

		for (int i = 0; i < reticleBmpIndices100.length; i++) {
			canvas.drawBitmap(
					scaledSingleBmp[reticleBmpIndices100[i]],
					mWidth - scaledDualBmp[0].getWidth()
							- scaledSingleBmp[0].getWidth(),
					mHeight / 2f - (i - 1)
							* scaledSingleBmp[0].getHeight()
							* (1 + 1 / 2f)
							- scaledSingleBmp[0].getHeight() / 2f
							+ getDeltaUnit(100),
					paint);
		}

		for (int i = 0; i < reticleBmpIndices1000.length; i++) {
			canvas.drawBitmap(
					scaledSingleBmpBig[reticleBmpIndices1000[i]],
					mWidth - scaledDualBmp[0].getWidth()
							- scaledSingleBmp[0].getWidth()
							- scaledSingleBmpBig[0].getWidth(),
					mHeight / 2f - (i - 1)
							* scaledSingleBmpBig[0].getHeight()
							* (1 + 1 / 2f)
							- scaledSingleBmpBig[0].getHeight() / 2f
							+ getDeltaUnit(1000),
					paint);
		}

		for (int i = 0; i < reticleBmpIndices10000.length; i++) {
			canvas.drawBitmap(
					scaledSingleBmpBig[reticleBmpIndices10000[i]],
					mWidth - scaledDualBmp[0].getWidth()
							- scaledSingleBmp[0].getWidth()
							- scaledSingleBmpBig[0].getWidth() * 2,
					mHeight / 2f - (i - 1)
							* scaledSingleBmpBig[0].getHeight()
							* (1 + 1 / 2f)
							- scaledSingleBmpBig[0].getHeight() / 2f
							+ getDeltaUnit(10000),
					paint);
		}
		
		canvas.restore();
	}

	private void updateAltScale() {
		//TODO : deal with negative alt

		// alt -= 1;
		// round speed to the lowest hundred
		altHundredRef = (((int) (alt / 100)) * 100);

		for (int i = 0; i < values.length; i++) {
			values[i] = altHundredRef - 400 + i * 100;
		}

		for (int i = 0; i < positions.length; i++) {
			positions[i].x = 0f;
			positions[i].y = (alt - values[i]) * pixelPerUnit
					+ mHeight / 2f;

			// case of X000 value (line under and above)
			if (values[i] % 1000 == 0) {
				pathsThousand[0].reset();
				pathsThousand[0].moveTo(
						gradLineLength,
						positions[i].y + textBounds.height());
				pathsThousand[0].lineTo(
						mWidth * 0.7f,
						positions[i].y + textBounds.height());
				pathsThousand[0].close();
				pathsThousand[1].reset();
				pathsThousand[1].moveTo(
						gradLineLength,
						positions[i].y - textBounds.height());
				pathsThousand[1].lineTo(
						mWidth * 0.7f,
						positions[i].y - textBounds.height());
				pathsThousand[1].close();
			}
		}

		for (int i = 0; i < paths.length; i++) {
			paths[i].reset();
			paths[i].moveTo(positions[i].x, positions[i].y);
			paths[i].lineTo(gradLineLength, positions[i].y);
			paths[i].close();
		}

		// reticle bitmap array
		buildBmpArray20(reticleBmpIndices20, alt);
		buildBmpArray100(reticleBmpIndices100, alt);
		buildBmpArray1000(reticleBmpIndices1000, alt);
		buildBmpArray10000(reticleBmpIndices10000, alt);
	}

	private float getDeltaUnit(int step) {
		switch (step) {
		case 10:
			if (alt < 100) {
				return scaledDualBmp[0].getHeight()
						* ((int) alt - lowestTen) / step;
			}
			return scaledDualBmp[0].getHeight()
					* ((int) alt % 100 - lowestTen) / step;

		case 20:
			if (alt < 100) {
				return scaledDualBmp[0].getHeight()
						* ((int) alt - lowestTwenty) / step;
			}
			return scaledDualBmp[0].getHeight()
					* ((int) alt % 100 - lowestTwenty) / step;

		case 100:
			// scroll only when twenty to go
			if (alt < 1000) {
				if (alt - lowestHundred > 80) {
					return scaledSingleBmp[0].getHeight() * (1 + 1 / 2f)
							* ((int) alt - lowestHundred - 80) / 20f;
				}
				return 0;
			}

			if ((int) alt % 1000 - lowestHundred > 80) {
				return scaledSingleBmp[0].getHeight() * (1 + 1 / 2f)
						* ((int) alt % 1000 - lowestHundred - 80) / 20f;
			}
			return 0;

		case 1000:
			// scroll only when twenty to go
			if (alt < 10000) {
				if (alt - lowestThousand > 980) {
					return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
							* ((int) alt - lowestThousand - 980) / 20f;
				}
				return 0;
			}

			if ((int) alt % 10000 - lowestThousand > 980) {
				return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
						* ((int) alt % 10000 - lowestThousand - 980) / 20f;
			}
			return 0;

		case 10000:
			// scroll only when twenty to go
			if (alt < 100000) {
				if (alt - lowestTenThousand > 9980) {
					return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
							* ((int) alt - lowestTenThousand - 9980) / 20f;
				}
				return 0;
			}

			if ((int) alt % 100000 - lowestThousand > 99980) {
				return scaledSingleBmpBig[0].getHeight() * (1 + 1 / 2f)
						* ((int) alt % 100000 - lowestTenThousand - 99980)
						/ 20f;
			}
			return 0;

		default:
			return 0;
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
			array[0] = scaledSingleBmp.length - lowestHundred / 100 - 1;
		} else {
			array[0] = lowestHundred / 100 - 1;
		}
		array[1] = lowestHundred / 100;
		if (lowestHundred / 100 + 1 > scaledSingleBmp.length - 1) {
			array[2] = lowestHundred / 100 + 1 - scaledSingleBmp.length;
		} else {
			array[2] = lowestHundred / 100 + 1;
		}
	}

	private void buildBmpArray1000(int[] array, float value) {
		// compute lowest hundred with integer part of alt only
		if (value < 10000)
			lowestThousand = (((int) (value) / 1000) * 1000);
		else
			lowestThousand = (((int) (value % 10000) / 1000) * 1000);

		// fill array
		if (lowestThousand / 1000 - 1 < 0) {
			array[0] = scaledSingleBmpBig.length - lowestThousand / 1000 - 1;
		} else {
			array[0] = lowestThousand / 1000 - 1;
		}
		array[1] = lowestThousand / 1000;
		if (lowestThousand / 1000 + 1 > scaledSingleBmpBig.length - 1) {
			array[2] = lowestThousand / 1000 + 1 - scaledSingleBmpBig.length;
		} else {
			array[2] = lowestThousand / 1000 + 1;
		}
	}

	private void buildBmpArray10000(int[] array, float value) {
		// compute lowest hundred with integer part of alt only
		if (value < 100000)
			lowestTenThousand = (((int) (value) / 10000) * 10000);
		else
			lowestTenThousand = (((int) (value % 100000) / 10000) * 10000);

		// fill array
		if (lowestTenThousand / 10000 - 1 < 0) {
			array[0] = scaledSingleBmpBig.length - lowestTenThousand / 10000
					- 1;
		} else {
			array[0] = lowestTenThousand / 10000 - 1;
		}
		array[1] = lowestTenThousand / 10000;
		if (lowestTenThousand / 10000 + 1 > scaledSingleBmpBig.length - 1) {
			array[2] = lowestTenThousand / 10000 + 1
					- scaledSingleBmpBig.length;
		} else {
			array[2] = lowestTenThousand / 10000 + 1;
		}
	}

	private String buildAltValue(int value) {
		if (value < 10) {
			return "    " + String.valueOf(value);
		}

		if (value < 100) {
			return "   " + String.valueOf(value);
		}

		if (value < 1000) {
			return "  " + String.valueOf(value);
		}

		if (value < 10000) {
			return " " + String.valueOf(value);
		}

		return String.valueOf(value);
	}

	private void buildBmpArray20(int[] array, float value) {
		// compute lowest twenty with integer part of alt only
		if (value < 100)
			lowestTwenty = (((int) (value) / 20) * 20);
		else
			lowestTwenty = (((int) (value % 100) / 20) * 20);

		// fill array
		if (lowestTwenty / 10 - 4 < 0) {
			array[0] = scaledDualBmp.length - lowestTwenty / 10 - 4;
		} else {
			array[0] = lowestTwenty / 10 - 4;
		}
		if (lowestTwenty / 10 - 2 < 0) {
			array[1] = scaledDualBmp.length - lowestTwenty / 10 - 2;
		} else {
			array[1] = lowestTwenty / 10 - 2;
		}
		array[2] = lowestTwenty / 10;
		if (lowestTwenty / 10 + 2 > scaledDualBmp.length - 1) {
			array[3] = lowestTwenty / 10 + 2 - scaledDualBmp.length;
		} else {
			array[3] = lowestTwenty / 10 + 2;
		}
		if (lowestTwenty / 10 + 4 > scaledDualBmp.length - 1) {
			array[4] = lowestTwenty / 10 + 4 - scaledDualBmp.length;
		} else {
			array[4] = lowestTwenty / 10 + 4;
		}
	}
}
