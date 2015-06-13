package aero.xpand;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class View2D extends SurfaceView implements SurfaceHolder.Callback {

	// State-tracking constants
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;

	private int mCnvsWidth = 1; // width of the canvas
	private int mCnvsHeight = 1; // height of the canvas

	private int mMode; // state of the game

	private boolean mRun = false; // surface is created & ready to draw
	private SurfaceHolder mSurfaceHolder; // Handle to the surface manager

	private Context mContext;
	private GameLoop gameLoop; // thread that draws the animation

	public ComponentAdi componentAdi;
	public ComponentSpeed componentSpeed;
	public ComponentAltitude componentAltitude;
	public ComponentHeadingStrip componentHeading;
	public ComponentVsi componentVsi;
	public ComponentILS componentILS;

	public boolean initGraphicsOk;

	public View2D(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		gameLoop = new GameLoop(holder, context);
		gameLoop.doStart();
		setFocusable(true); // make sure we get key events
	}

	private void initGraphics() {
		componentAdi = new ComponentAdi(mContext, mCnvsHeight * 0.3f, 0,
				mCnvsHeight * 0.8f, mCnvsHeight * 0.8f);
		componentSpeed = new ComponentSpeed(mContext, 0, 0, mCnvsHeight / 5,
				mCnvsHeight);
		componentAltitude = new ComponentAltitude(mContext, mCnvsHeight * 0.4f
				+ mCnvsHeight * 0.8f, 0, mCnvsHeight * 0.25f, mCnvsHeight);
		componentHeading = new ComponentHeadingStrip(mContext,
				mCnvsHeight * 0.3f, mCnvsHeight * 0.9f, mCnvsHeight * 0.8f,
				mCnvsHeight * 0.1f);
		componentVsi = new ComponentVsi(mContext,  mCnvsHeight * 0.4f + mCnvsHeight * 0.8f +
				mCnvsHeight * 0.25f, mCnvsHeight * 0.25f,  mCnvsHeight * 0.12f, mCnvsHeight * 0.5f);

		// pass ADI coordinates to ILS
		componentILS = new ComponentILS(mContext, mCnvsHeight * 0.3f, 0,
				mCnvsHeight * 0.8f, mCnvsHeight * 0.8f);

	}

	/**
	 * Fetches the animation thread corresponding to this View.
	 * 
	 * @return the animation thread
	 */
	public GameLoop getThread() {
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
		initGraphicsOk = false;
		gameLoop.setSurfaceSize(width, height);
		initGraphics();
		initGraphicsOk = true;
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {

		if (gameLoop.getState() == Thread.State.TERMINATED) {
			gameLoop = new GameLoop(holder, mContext);
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

	class GameLoop extends Thread {

		public GameLoop(SurfaceHolder surfaceHolder, Context context) {

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
		 * @see //setState(int, CharSequence)
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
						if (mMode == STATE_RUNNING && initGraphicsOk) {

							// first clear the screen
							c.drawColor(Color.BLACK);
//							componentAdi.pitch += 0.1f;
//							componentAdi.roll += 0.1f;
//							componentSpeed.mSpeed += 0.1f;
//							componentAltitude.alt += 1f;
//							componentHeading.heading += 0.1f;

							componentSpeed.updateComponent(c);
							componentAltitude.updateComponent(c);
							componentHeading.updateComponent(c);
							componentVsi.updateComponent(c);
							componentILS.updateComponent(c);

							// ADI / ILS MUST be in last position
							componentAdi.updateComponent(c);
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
	}

	public GameLoop getGameLoop() {
		return gameLoop;
	}
}
