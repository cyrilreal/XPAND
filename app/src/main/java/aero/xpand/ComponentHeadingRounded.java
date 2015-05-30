package aero.xpand;

import aero.xpand.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ComponentHeadingRounded extends SurfaceView implements SurfaceHolder.Callback {

	// State-tracking constants
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;

	private boolean init_ok;
	private Paint paint, paintText;

	private int mCnvsWidth = 1; // width of the canvas
	private int mCnvsHeight = 1; // height of the canvas

	private int mMode; // state of the game

	private boolean mRun = false; // surface is created & ready to draw
	private SurfaceHolder mSurfaceHolder; // Handle to the surface manager

	private Context mContext;
	private AnimLoop gameLoop; // thread that draws the animation

	private float headingAmplitude; // amplitude of speed scale
	private float gradLineLength;
	private int[] values; // values of hdg to display
	private Path lineShort, lineLong;
	private Path reticle;

	private int[] singleDigits; // resources ids for single digits(0123456789)
	private Bitmap[] bitmaps = new Bitmap[10];
	private Bitmap[] scaledBitmaps = new Bitmap[10];
	private Bitmap[] scaledBitmapsBig = new Bitmap[10]; // bitmaps to display
														// 30,
														// 60, 90, 120 etc.

	public ComponentHeadingRounded(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);

		paintText = new Paint();
		paintText.setColor(Color.WHITE);
		paintText.setAntiAlias(true);
		paintText.setTypeface(Typeface.MONOSPACE);
		paintText.setTextSize(mCnvsHeight / 16f);

		headingAmplitude = 100f; //
		values = new int[(int) (headingAmplitude / 10) + 1];

		singleDigits = new int[] { R.drawable.aa_fig_0, R.drawable.aa_fig_1,
				R.drawable.aa_fig_2, R.drawable.aa_fig_3, R.drawable.aa_fig_4,
				R.drawable.aa_fig_5, R.drawable.aa_fig_6, R.drawable.aa_fig_7,
				R.drawable.aa_fig_8, R.drawable.aa_fig_9 };

		// create thread only; it's started in surfaceCreated()
		gameLoop = new AnimLoop(holder, context);
		gameLoop.doStart();
		setFocusable(true); // make sure we get key events
	}

	private void initGraphics() {
		gradLineLength = mCnvsWidth / 24f;

		lineLong = new Path();
		lineLong.moveTo(mCnvsWidth / 2f, mCnvsWidth / 20f);
		lineLong.lineTo(mCnvsWidth / 2f, mCnvsWidth / 20f + gradLineLength);
		lineLong.close();

		lineShort = new Path();
		lineShort.moveTo(mCnvsWidth / 2f, mCnvsWidth / 20f);
		lineShort.lineTo(mCnvsWidth / 2f, mCnvsWidth / 20f + gradLineLength
				* 2f / 3f);
		lineShort.close();

		paint.setStrokeWidth(mCnvsWidth / 120f);
		paint.setFilterBitmap(true);

		paintText.setTextSize(mCnvsWidth / 20f);

		// init reticle
		reticle = new Path();
		reticle.moveTo(mCnvsWidth / 2f + mCnvsWidth / 32f, 0);
		reticle.lineTo(mCnvsWidth / 2f - mCnvsWidth / 32f, 0);
		reticle.lineTo(mCnvsWidth / 2f, mCnvsWidth / 20f);
		reticle.close();

		// init bitmaps arrays
		for (int i = 0; i < bitmaps.length; i++) {
			bitmaps[i] = BitmapFactory.decodeResource(getResources(),
					singleDigits[i]);
		}

		for (int i = 0; i < scaledBitmaps.length; i++) {
			scaledBitmaps[i] = Bitmap.createScaledBitmap(
					bitmaps[i],
					mCnvsWidth / 36,
					mCnvsWidth / 36 + (mCnvsWidth / 36) / 3,
					true);
		}

		for (int i = 0; i < scaledBitmapsBig.length; i++) {
			scaledBitmapsBig[i] = Bitmap.createScaledBitmap(
					bitmaps[i],
					mCnvsWidth / 24,
					mCnvsWidth / 24 + (mCnvsWidth / 24) / 3,
					true);
		}

		init_ok = true;
	}

	/**
	 * Fetches the animation thread corresponding to this LunarView.
	 * 
	 * @return the animation thread
	 */
	public AnimLoop getThread() {
		return gameLoop;
	}

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus)
			gameLoop.pause();
	}

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		gameLoop.setSurfaceSize(width, height);
		initGraphics();
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		if (gameLoop.getState() == Thread.State.TERMINATED) {
			gameLoop = new AnimLoop(holder, mContext);
			gameLoop.setRunning(true);
			gameLoop.start();
		} else {
			gameLoop.setRunning(true);
			gameLoop.start();
		}
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		gameLoop.setRunning(false);
		while (retry) {
			try {
				gameLoop.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public int getMode() {
		return mMode;
	}

	class AnimLoop extends Thread {

		float heading;
		int headingTenRef;

		public AnimLoop(SurfaceHolder surfaceHolder, Context context) {

			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
		}

		/**
		 * Starts the game, setting parameters for the current difficulty.
		 */
		public void doStart() {
			synchronized (mSurfaceHolder) {
				mMode = STATE_RUNNING;
			}
		}

		/**
		 * Pauses the physics update & animation.
		 */
		public void pause() {
			synchronized (mSurfaceHolder) {
				if (mMode == STATE_RUNNING)
					mMode = STATE_PAUSE;
			}
		}

		/**
		 * Restores game state from the indicated Bundle. Typically called when
		 * the Activity is being restored after having been previously
		 * destroyed.
		 * 
		 * @param savedState
		 *            Bundle containing the game state
		 */
		public synchronized void restoreState(Bundle savedState) {
			synchronized (mSurfaceHolder) {
				mMode = STATE_READY;

			}
		}

		/**
		 * Sets the game mode. That is, whether we are running, paused, in the
		 * failure state, in the victory state, etc.
		 * 
		 * @see #setState(int, CharSequence)
		 * @param mode
		 *            one of the STATE_* constants
		 */
		public void setState(int mode) {
			synchronized (mSurfaceHolder) {
				mMode = mode;
			}
		}

		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						if (mMode == STATE_RUNNING && init_ok) {

							// first clear the screen
							c.drawColor(Color.BLACK);
							updateHeadingScale();
							draw(c);
						}
						if (mMode == STATE_READY) {

						}

					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 * 
		 * @param b
		 *            true to run, false to shut down
		 */
		public void setRunning(boolean b) {
			mRun = b;
		}

		/* Callback invoked when the surface dimensions change. */
		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				mCnvsWidth = width;
				mCnvsHeight = height;

				// don't forget to resize the background image

			}
		}

		/**
		 * Resumes from a pause.
		 */
		public void unpause() {
			// Move the real time clock up to now
			synchronized (mSurfaceHolder) {
			}
			mMode = STATE_RUNNING;
		}

		/**
		 * Draws the elements
		 */
		private void draw(Canvas canvas) {

			// draw grey disc
			paint.setStyle(Style.FILL);
			paint.setColor(Color.DKGRAY);
			canvas.drawCircle(
					mCnvsWidth / 2f,
					mCnvsHeight * 2f,
					mCnvsHeight * 2f - mCnvsWidth / 20f,
					paint);
			// draw reticle
			paint.setStyle(Style.STROKE);
			paint.setColor(Color.WHITE);
			canvas.drawPath(reticle, paint);

			// draw graphics
			// add a 1.7f factor for rotation cause heading rose is not a true
			// circle
			canvas.rotate(
					(-headingAmplitude / 2f) * 1.6f
							- (heading - headingTenRef) * 1.6f,
					mCnvsWidth / 2f,
					mCnvsHeight * 2f);

			// now draw all lines and numbers
			for (int i = 0; i < values.length; i++) {
				canvas.drawPath(lineLong, paint);
				if (values[i] < 100) {
					if (values[i] % 30f == 0) {
						drawBmpFromArray(canvas, i, scaledBitmapsBig);
					} else {
						drawBmpFromArray(canvas, i, scaledBitmaps);
					}
				}
				else {
					// first bitmap
					if (values[i] % 30f == 0) {
						drawLeftBmpFromArray(canvas, i, scaledBitmapsBig);
					} else {
						drawLeftBmpFromArray(canvas, i, scaledBitmaps);
					}

					// second bitmap
					if (values[i] % 30f == 0) {
						drawRightBmpFromArray(canvas, i, scaledBitmapsBig);
					} else {
						drawRightBmpFromArray(canvas, i, scaledBitmaps);
					}
				}
				// draw short line between two long
				canvas.rotate(5f * 1.6f, mCnvsWidth / 2f, mCnvsHeight * 2f);
				canvas.drawPath(lineShort, paint);
				canvas.rotate(5f * 1.6f, mCnvsWidth / 2f, mCnvsHeight * 2f);
			}
		}

		private void updateHeadingScale() {
			// round speed to the lowest ten
			headingTenRef = (((int) (heading / 10)) * 10);

			for (int i = 0; i < values.length; i++) {
				values[i] = (int) (headingTenRef - headingAmplitude / 2f + i * 10);
				if (values[i] <= 0) {
					values[i] += 360;
				}
				if (values[i] > 360) {
					values[i] -= 360;
				}
			}
		}

		private void drawBmpFromArray(Canvas c, int i, Bitmap[] array) {
			c.drawBitmap(
					array[values[i] / 10],
					mCnvsWidth / 2f - array[0].getWidth() / 2f,
					mCnvsWidth / 20f + gradLineLength + array[0].getHeight()
							/ 4f,
					paint);
		}

		private void drawLeftBmpFromArray(Canvas c, int i, Bitmap[] array) {
			c.drawBitmap(
					array[values[i] / 100],
					mCnvsWidth / 2 - array[0].getWidth(),
					mCnvsWidth / 20f + gradLineLength + array[0].getHeight()
							/ 4f,
					paint);
		}

		private void drawRightBmpFromArray(Canvas c, int i, Bitmap[] array) {
			c.drawBitmap(
					array[(values[i] % 100) / 10],
					mCnvsWidth / 2,
					mCnvsWidth / 20f + gradLineLength + array[0].getHeight()
							/ 4f,
					paint);
		}
	}

	public AnimLoop getGameLoop() {
		return gameLoop;
	}
}
