package aero.xpand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.Typeface;

public class ComponentHeadingStrip {

	private String strHeading; // for testing only
	public static final float RETICLE_LENGHT_RATIO = 0.15f;
	private boolean init_ok;
	private Paint paint, paintText;

	private float mWidth = 1; // width of the canvas
	private float mHeight = 1; // height of the canvas
	private float mPosX, mPosY;
	private float mCenterX, mCenterY;

	private Rect textBounds; // to measure the size of text
	private Rect background;

	private Context mContext;

	private float headingAmplitude; // amplitude of speed scale
	private float pixelPerUnit; // number of pixel per speed unit
	private float gradLineLength;
	private int[] values; // values of hdg to display
	private Path lineShort, lineLong;
	private Path reticle;

	private PointF[] positions; // positions of strings to display
	private Path[] paths; // paths to draw graduations lines

	private int[] singleDigits; // resources ids for single digits(0123456789)
	private Bitmap[] bitmaps = new Bitmap[10];
	private Bitmap[] scaledBitmaps = new Bitmap[10];
	private Bitmap[] scaledBitmapsBig = new Bitmap[10]; // bitmaps to display
														// 30,
														// 60, 90, 120 etc.

	public float heading;
	private int headingTenRef;

	private float reticleLength;

	public ComponentHeadingStrip(Context context, float x, float y,
			float width, float height) {

		mContext = context;
		this.mPosX = x;
		this.mPosY = y;
		this.mWidth = width;
		this.mHeight = height;
		this.mCenterX = width / 2;
		this.mCenterY = height / 2;

		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);

		paintText = new Paint();
		paintText.setColor(Color.WHITE);
		paintText.setAntiAlias(true);
		paintText.setTypeface(Typeface.MONOSPACE);
		paintText.setTextSize(mHeight / 2.5f);

		textBounds = new Rect();
		headingAmplitude = 100f; //

		values = new int[(int) (headingAmplitude / 10) + 1];
		positions = new PointF[(int) (headingAmplitude / 10) + 1];
		paths = new Path[(int) (headingAmplitude / 10) + 1];

		singleDigits = new int[] { R.drawable.aa_fig_0, R.drawable.aa_fig_1,
				R.drawable.aa_fig_2, R.drawable.aa_fig_3, R.drawable.aa_fig_4,
				R.drawable.aa_fig_5, R.drawable.aa_fig_6, R.drawable.aa_fig_7,
				R.drawable.aa_fig_8, R.drawable.aa_fig_9 };

		initGraphics();
	}

	private void initGraphics() {
		// init background
		background = new Rect(0, 0, (int) mWidth, (int) mHeight);

		paintText.getTextBounds("000", 0, 3, textBounds);

		gradLineLength = mHeight / 4f;
		reticleLength = mHeight / 4f;

		paint.setStrokeWidth(mHeight / 160f);

		// init point array
		for (int i = 0; i < positions.length; i++) {
			positions[i] = new PointF();
		}

		// init path array
		for (int i = 0; i < paths.length; i++) {
			paths[i] = new Path();
		}

		pixelPerUnit = mWidth / headingAmplitude;

		// init reticle
		reticle = new Path();
		reticle.moveTo(mCenterX + reticleLength / 2, 0);
		reticle.lineTo(mCenterX - reticleLength / 2, 0);
		reticle.lineTo(mCenterX, reticleLength);
		reticle.close();

		// init bitmaps arrays
		for (int i = 0; i < bitmaps.length; i++) {
			bitmaps[i] = BitmapFactory.decodeResource(mContext.getResources(),
					singleDigits[i]);
		}

		for (int i = 0; i < scaledBitmaps.length; i++) {
			scaledBitmaps[i] = Bitmap.createScaledBitmap(bitmaps[i],
					(int) (mHeight / 36),
					(int) (mHeight / 36 + (mHeight / 36) / 3), true);
		}

		for (int i = 0; i < scaledBitmapsBig.length; i++) {
			scaledBitmapsBig[i] = Bitmap.createScaledBitmap(bitmaps[i],
					(int) (mHeight / 24),
					(int) (mHeight / 24 + (mHeight / 24) / 3), true);
		}

		init_ok = true;
	}

	public void updateComponent(Canvas canvas) {
		updateHeadingScale();
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

		// clip the canvas
		canvas.clipRect(0, 0, mWidth, mHeight);

		// draw grey rectangle
		paint.setStyle(Style.FILL);
		paint.setColor(Color.DKGRAY);
		canvas.drawRect(background, paint);

		// draw reticle
		paint.setStyle(Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawPath(reticle, paint);

		// draw graphics

		// draw lines
		paint.setStyle(Style.STROKE);
		canvas.drawLine(0, reticleLength, mWidth, reticleLength, paint);

		for (int i = 0; i < paths.length; i++) {
			if (values[i] < 0) {
				continue;
			}
			canvas.drawPath(paths[i], paint);
		}

		// draw strings
		for (int i = 0; i < values.length; i++) {
			if (values[i] < 0 || (values[i] % 20 != 0 && values[i] != 0)) {
				continue;
			}
			paintText.getTextBounds(String.valueOf(values[i]), 0, String
					.valueOf(values[i]).length(), textBounds);
			canvas.drawText(String.valueOf(values[i]), positions[i].x
					- textBounds.width() / 2f, positions[i].y + reticleLength
					+ gradLineLength + gradLineLength / 4f, paintText);
		}

		canvas.restore();
		// "de-clip" the canvas
		canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight(),
				Region.Op.REPLACE);
	}

	private void updateHeadingScale() {
		// round speed to the lowest ten
		headingTenRef = (((int) (heading / 10)) * 10);

		for (int i = 0; i < values.length; i++) {
			values[i] = (int) (headingTenRef - headingAmplitude / 2f + i * 10);
			if (values[i] <= 0) {
				values[i] += 360;
			}
			if (values[i] >= 360) {
				values[i] -= 360;
			}
		}

		for (int i = 0; i < positions.length; i++) {
			float delta = heading - values[i];
			if (delta < -180) {
				delta += 360;
			}
			if (delta > 180) {
				delta -= 360;
			}
			positions[i].x = mCenterX - delta * pixelPerUnit;
			positions[i].y = reticleLength;
		}

		for (int i = 0; i < paths.length; i++) {
			paths[i].reset();
			paths[i].moveTo(positions[i].x, positions[i].y);
			paths[i].lineTo(positions[i].x, positions[i].y + gradLineLength);
			paths[i].close();
		}
	}

	private void drawBmpFromArray(Canvas c, int i, Bitmap[] array) {
		c.drawBitmap(array[values[i] / 10], mWidth / 2f - array[0].getWidth()
				/ 2f,
				mWidth / 20f + gradLineLength + array[0].getHeight() / 4f,
				paint);
	}

	private void drawLeftBmpFromArray(Canvas c, int i, Bitmap[] array) {
		c.drawBitmap(array[values[i] / 100], mWidth / 2 - array[0].getWidth(),
				mWidth / 20f + gradLineLength + array[0].getHeight() / 4f,
				paint);
	}

	private void drawRightBmpFromArray(Canvas c, int i, Bitmap[] array) {
		c.drawBitmap(array[(values[i] % 100) / 10], mWidth / 2, mWidth / 20f
				+ gradLineLength + array[0].getHeight() / 4f, paint);
	}
}
