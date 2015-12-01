/*
 * Project: asqare
 * Copyright (C) 2008-2012 rdrr.labs@gmail.com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.asqare.gameplay;

import java.util.HashMap;
import java.util.Random;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.R;
import com.alfray.asqare.component_i.ConBijouxCalculation;
import com.alfray.asqare.component_i.ConcreteScoreCalculator;
import com.alfray.asqare.component_i.ScoreBpOsv;
import com.alfray.asqare.engine.AnimThread;
import com.alfray.asqare.engine.Board;
import com.alfray.asqare.engine.Color;
import com.alfray.asqare.engine.Board.Cell;
import com.alfray.asqare.prefs.PrefsValues;
import com.alfray.asqare.sprite.BevelCircle;
import com.alfray.asqare.sprite.BevelRect;
import com.alfray.asqare.sprite.BevelSprite;
import com.alfray.asqare.sprite.BevelSquare;
import com.alfray.asqare.sprite.ResSpriteSquare;
import com.alfray.asqare.sprite.ResSpriteStrech;
import com.alfray.asqare.sprite.Sprite;
import com.alfray.asqare.view.AsqareView;
import com.alfray.asqare.view.AsqareView.drawBoard;

//-----------------------------------------------

public class Bijoux extends Gameplay {

	private static final String TAG = "Asqare.Mode1";
    private static final int SERIAL_ID = 1;

    /** Event to call doSetup, which fills the initial board randomly. */
	private static final int EVENT_SETUP = 0;
	/** Event to swap two selected items */
	private static final int EVENT_SWAP = 1;
	/** Event to check if items can vanish and need to be removed */
	private static final int EVENT_CHECK = 2;
	/** Event to fill in new replacement items after some have vanished */
	private static final int EVENT_FILL = 3;
	/** Event to indicate when the swap/check/fill sequence as completed */
	private static final int EVENT_DONE = 4;

	/** Percentage score penalty for non-hit moves */
	private static final int MOVE_PENALTY = 10;

	private AnimThread mAnimThread;
	private boolean mCreated;
	private int mSelectX = -1;
	private int mSelectY = -1;
	private Board<Board.Cell> mBoard;
	private int mBx;
	private int mBy;
	private Sprite mBgSprite;
	private Sprite mSelSprite;
	private Sprite[] mSprites;
	private Random mRandom;
	private Cell mFirstCell;
	private Cell mSecondCell;
	private int[] mTempStack;
	private long mFillChanged;
	private boolean mApplyMovePenalty;
	private boolean mStopped;
	private int mSetupEvent;
	private boolean mUseAnims;
    private String mCurrVisualTheme;

	public Bijoux(AsqareContext context) {
		super(context);

		mStopped = true; // stopped when created, start() must be called once
		mAnimThread = getContext().getAnimThread();
		mRandom = new Random();
	}

	@Override
	public String getName() {
		return getContext().getActivity().getString(R.string.title_bijoux);
	}

	@Override
	public void create(String state) {
	    // onPrefsUpdated calls createSprites below.
        onPrefsUpdated(getContext().getPrefsValues());

		if (state != null &&
				state.length() > 0 &&
				restoreState(state)) {
//			updateMessage();
			mSetupEvent = EVENT_CHECK;
		} else {
			mSetupEvent = EVENT_SETUP;
		}
	}

	private void createSprites(String visualTheme, boolean preserveState) {
	    if (mCreated && visualTheme.equals(mCurrVisualTheme)) return;
	    if (!mCreated) preserveState = false;

	    String state = preserveState ? saveState() : null;

	    if ("flowers".equals(visualTheme)) {
	        createFlowerTheme();
	    } else if ("rustworn".equals(visualTheme)) {
	        createRustwornTheme();
        } else if ("truckin".equals(visualTheme)) {
            createTruckinTheme();
        } else {
	        createBasicTheme();
	    }

	    if (preserveState) restoreState(state);
        mCurrVisualTheme = visualTheme;
    }


    @SuppressWarnings("unchecked")
    private void createBasicTheme() {
        mBgSprite = new BevelRect(Color.BLUE1, 2, 3);
        mSelSprite = new BevelRect(Color.RED2, 2, 3);

        Object[] types = { BevelSquare.class, BevelCircle.class };

        // the background and selection colors are not used for sprites
        int n = Color.colors1.length - 2;
        mSprites = new Sprite[n];

        n = 0;
        for (Color color : Color.colors1) {
        	if (color != Color.BLUE1 && color != Color.RED2) {
        		Object type = types[n & 1];
        		mSprites[n++] = BevelSprite.instantiate(
        				(Class<? extends Sprite>) type, color, 8, 5);
        	}
        }
    }

    private void createFlowerTheme() {
        Resources r = getContext().getActivity().getResources();
        mBgSprite  = new ResSpriteStrech(1, r.getDrawable(R.drawable.flowers_background));
        mSelSprite = new ResSpriteStrech(1, r.getDrawable(R.drawable.flowers_bg_selected));

        mSprites = new Sprite[8];
        int i = 0;
        final int n = 3;
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_blue));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_lightblue));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_magenta));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_orange));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_pink));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_red));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_teal));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.flowers_yellow));
    }

    private void createRustwornTheme() {
        Resources r = getContext().getActivity().getResources();
        mBgSprite  = new ResSpriteStrech(1, r.getDrawable(R.drawable.rustworn_background));
        mSelSprite = new ResSpriteStrech(1, r.getDrawable(R.drawable.rustworn_bg_selected));

        mSprites = new Sprite[8];
        int i = 0;
        final int n = 3;
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_aqua));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_blue));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_darkgreen));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_limegreen));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_orange));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_purple));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_redorange));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.rustworn_bolt_yella));
    }

    private void createTruckinTheme() {
        Resources r = getContext().getActivity().getResources();
        mBgSprite  = new ResSpriteStrech(1, r.getDrawable(R.drawable.truckin_background));
        mSelSprite = new ResSpriteStrech(1, r.getDrawable(R.drawable.truckin_bg_selected));

        mSprites = new Sprite[8];
        int i = 0;
        final int n = 3;
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_aqua));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_blue));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_lilac));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_lime));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_orange));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_red));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_silver));
        mSprites[i++] = new ResSpriteSquare(n, r.getDrawable(R.drawable.truckin_sign_yella));
    }

	@Override
	public String saveState() {
		mAnimThread.pauseThread(true);

		HashMap<Sprite, Integer> sp_map = new HashMap<Sprite, Integer>();
		int nsp = mSprites.length;
		for (int i = 0; i < nsp; i++) {
			sp_map.put(mSprites[i], Integer.valueOf(i));
		}

		Cell[] cells = mBoard.getBoardArray();
		int n = cells.length;

		int[] state = new int[n + 6];

		state[0] = SERIAL_ID;
		state[1] = 0; 		// place for checksum
		state[2] = mBx;  	// note: n = mBx * mBy so it's not saved
		state[3] = mBy;
		state[4] = mScore;
		state[5] = mMoves;

		for (int i = 0; i < n; i++) {
			Integer k = sp_map.get(cells[i].getSprite());
			state[i + 6] = (k == null ? -1 : k.intValue());
		}

		// compute a simple checksum (when we check it back in restoreState,
		// the checksum will xor itself to 0).
		int c = 0;
		for (int s : state) {
			c = c ^ s;
		}
		state[1] = c;

		StringBuilder sb = new StringBuilder();
		for (int s : state) {
			sb.append(Integer.toHexString(s)).append(',');
		}
		sb.setLength(sb.length() - 1); // remove last comma

		mAnimThread.pauseThread(false);

		return sb.toString();
	}

	/**
	 * Tries to restore state from the given state string, which should
	 * have been created using saveState() earlier. Returns false if this
	 * failed (invalid format or invalid serialized state.)
	 */
	private boolean restoreState(String state) {
		String[] strings = state.split(",");
		int n = strings.length;
		int[] values = new int[n];
		for (int i = 0; i < n; i++) {
			try {
				values[i] = Integer.parseInt(strings[i], 16);
			} catch (NumberFormatException e) {
				Log.e(TAG, String.format("Invalid hex state[%d]=%s (%s)",
						i, strings[i], state), e);
				return false;
			}
		}

		try {
			int serial = values[0];
			if (serial != SERIAL_ID) {
				Log.w(TAG, "Older state, ignored");
			} else {

				int checksum = 0;
				for (int v : values) {
					checksum = checksum ^ v;
				}
				if (checksum != 0) {
					Log.w(TAG, "Invalid state checksum, ignored");
				} else {
					mBx = values[2];
					mBy = values[3];
					n = mBx * mBy;
					mScore = values[4];
					mMoves = values[5];

					if (mBx > 0 && mBy > 0 && values.length >= n + 6) {
						createBoard();

						int nsp = mSprites.length;

						Cell[] cells = mBoard.getBoardArray();
						for (int i = 0; i < n; i++) {
							int k = values[i + 6];
							if (k >= 0) cells[i].setSprite(mSprites[k % nsp]);
						}

						return true;
					}
				}
			}

		} catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "State too short: " + state, e);
		}

		return false;
	}

	@Override
	public void start() {
		if (!mStopped) return; // ignore if not stopped
		mStopped = false;

		mAnimThread.start();
		//**ivan
		ScoreBpOsv.lst_bonus_score=0;
		if (mBoard == null) {
			mSelectX = -1;
			mSelectY = -1;
			mScore = 0;
			mMoves = 0;
			mFirstCell = null;
			mSecondCell = null;

			mBx = 6;
			mBy = 7;
			createBoard();
		}

        setup();
	}

	private void createBoard() {
		int n = mBx * mBy;
		assert mBx < 128; // we encode x/y position in 7-bits in the removeAdjacent stack.
		assert mBy < 128; // we encode x/y position in 7-bits in the removeAdjacent stack.
		assert n < 64;    // we use 64 bit longs as bit fields to keep track of changes.
		if (mBoard == null || mBoard.getWidth() != mBx || mBoard.getHeight() != mBy) {
			mBoard = new Board<Board.Cell>(mBx, mBy, Board.Cell.class);
			mBoard.setBackground(mBgSprite);
	        mBoard.setSelectSprite(mSelSprite);
		}
		if (mTempStack == null || mTempStack.length != n) {
			mTempStack = new int[n];
		}
		setBoard(mBoard);
	}

	@Override
	public void pause(boolean pause) {
		mAnimThread.pauseThread(pause);
	}

	@Override
	public void stop() {
		mStopped = true;
		mAnimThread.clear();
		mFirstCell = null;
		mSecondCell = null;
		mBoard = null;
		setBoard(null);
	}

	@Override
	public void onPrefsUpdated(PrefsValues prefs) {
		mUseAnims = prefs.playAnims();
		createSprites(prefs.getVisualTheme(), true /*preserveState*/);
	}

	@Override
	public Bitmap createPreview(String state, int width, int height) {
		create(state);
		if (mBoard == null) return null;

		// get desired cell size that fits the hint width/height
		// with a square aspect ratio
		int s = width / mBx;
		int t = height / mBy;
		if (t < s) s = t;
		width = s * mBx;
		height = s * mBy;

		Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(b);
		c.drawARGB(255, 0, 0, 0);
		drawBoard db = new AsqareView.drawBoard();
		db.draw(c, mBoard, s, s);

		return b;
	}

	//-----------------

	private void setup() {
//		updateMessage();

        onCursorMoved(mBx / 2, mBy / 2);
        getContext().invalidateAll();

		mAnimThread.queueGameplayEvent(this, mSetupEvent);
	}

//	private void updateMessage() {
//		String msg = String.format("Bijoux - Moves: %d, Score: %d, Ratio: %.2f",
//				mMoves, mScore, (float)mScore / (float)mMoves);
//		getContext().setStatus(msg);
//	}

	//-----------------

	/* This runs in the UI thread */
    @Override
	protected void onActivate() {
		int sx = mSelectX;
		int sy = mSelectY;
		int bx = mBx;
		int by = mBy;
		if (sx >= 0 && sy >= 0 && sx < bx && sy < by) {
			final Board.Cell cell = mBoard.getCell(sx, sy);

			if (mSecondCell != null) {
				// Selection is disabled.
			} else if (mFirstCell == null) {
				// if there's no first selection, set it
				mFirstCell = cell;
				mAnimThread.startBlinking(cell, AnimThread.FPS / 2);
				getContext().vibrateTouch();
			} else if (mFirstCell == cell) {
				// If first==second selection, remove first selection
				mFirstCell = null;
				mAnimThread.stopAnim(cell);
				getContext().vibrateTouch();
			} else {
				int sx1 = mFirstCell.getX();
				int sy1 = mFirstCell.getY();
				// the second selection is only accepted if it has a different
				// sprite and it is adjacent in H or V to the first one (not
				// in diagonal.)
				if (mFirstCell.getSprite() == cell.getSprite()) return;
				if ((sx == sx1 && (sy1 == sy-1 || sy1 == sy+1)) ||
					(sy == sy1 && (sx1 == sx-1 || sx1 == sx+1))) {
					mSecondCell = cell;

					mMoves++;
					mApplyMovePenalty = true;
					updateMessage();
					mAnimThread.stopAnim(mFirstCell);
					getContext().vibrateTouch();
					if (mUseAnims) {
						mAnimThread.startBlinking(mFirstCell, AnimThread.FPS / 5);
						mAnimThread.startBlinking(mSecondCell, AnimThread.FPS / 5);

						mAnimThread.queueDelayFor(500);
					}
					mAnimThread.queueGameplayEvent(this, EVENT_SWAP);
				}
			}
			invalidateCell(cell);
		}
	}

	/* This runs in the UI thread */
	@Override
	protected void onCursorMoved(int x, int y) {
		int sx = mSelectX;
		int sy = mSelectY;
		if (sx != x || sy != y) {
			int bx = mBx;
			int by = mBy;
			if (sx >= 0 && sy >= 0 && sx < bx && sy < by) {
				invalidateCell(sx, sy);
			}

			// keep in valid boundaries
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			if (x >= bx) x = bx - 1;
			if (y >= by) y = by - 1;

	        mBoard.setSelectedCell(x, y);
	        mSelectX = x;
	        mSelectY = y;
			if (x >= 0 && y >= 0 && x < bx && y < by) {
				invalidateCell(x, y);
			}
		}
	}

	/* This runs in the UI thread */
	@Override
	protected void onCursorMovedDelta(int dx, int dy) {
		onCursorMoved(mSelectX + dx, mSelectY + dy);
	}

	private void createCell(int x, int y, int yfall, final Sprite sprite) {
		Cell cell = mBoard.getCell(x, y);
		mAnimThread.stopAnim(cell);
		cell.setVisible(false);
		cell.setSprite(sprite);

		if (mUseAnims) {
			int n = yfall * 4;
			int px = 0;
			int py = getContext().getBoardView().getCellSy() / 4;
			mAnimThread.startFlyOver(cell, px, py, n);
		}
		cell.setVisible(true);
	}

	private void invalidateCell(int x, int y) {
		getContext().invalidateCell(mBoard.getCell(x, y));
	}

	private void invalidateCell(Cell cell) {
		getContext().invalidateCell(cell);
	}

	/* This runs in the anim thread */
	@Override
	public void onActionEvent(int eventId) {
		switch (eventId) {
		case EVENT_SETUP:
			doSetup();
			break;
		case EVENT_SWAP:
			doSwap();
			break;
		case EVENT_CHECK:
			doCheck();
			break;
		case EVENT_FILL:
			doFill();
			break;
		case EVENT_DONE:
			doDone();
			break;
		}
	}

	/* This runs in the anim thread */
	private void doSetup() {
		int n = mSprites.length;
        for (int y = mBy - 1; y >= 0; y--) {
        	for (int x = 0; x < mBx; x++) {
        		int i = mRandom.nextInt(n);
        		createCell(x, y, mBy, mSprites[i]);
        	}
        }

		if (mUseAnims) mAnimThread.queueDelayFor(500);
		mAnimThread.queueGameplayEvent(this, EVENT_CHECK);
	}

	/* This runs in the anim thread */
	private void doSwap() {
		Cell c1 = mFirstCell;
		Cell c2 = mSecondCell;

		// swap sprites
		Sprite s1 = c1.getSprite();
		Sprite s2 = c2.getSprite();
		c1.setSprite(s2);
		c2.setSprite(s1);
		getContext().invalidateCell(c1);
		getContext().invalidateCell(c2);

		if (mUseAnims) mAnimThread.queueDelayFor(500);
		mAnimThread.queueGameplayEvent(this, EVENT_CHECK);
	}

	/* This runs in the anim thread */
	/**
	 * Given a bit-mark of cells which are to be changed (aka removed), this does two things:
	 * first it "slides down" existing non-removed cells to fill gaps;
	 * then it makes new sprites appear to the top of the column.
	 */
	private void doFill() {
		long changed = mFillChanged;
		if (changed != 0) {

			Cell[] cells = mBoard.getBoardArray(); // cells in order y(x)

			int n = mSprites.length;

			final int sy = mBy;
			final int sx = mBx;
			final int sy1 = sy-1;
			final int sxMy1 = sx * sy1;

			for (int x = 0; x < sx; x++) {
				// which y has the first hole in this column from the bottom?
				int k = sxMy1 + x;
				int y = sy1;
				for (; y >= 0; y--, k -= sx) {
					if (((changed >> k) & 1) != 0) break;
				}
				if (y < 0) continue; // no holes in this column

				// the fall down animation will cover y cells + an extra one
				// so that it starts off screen
				int h = y + 1;

				// find all non-holes y1<y and move them to y
				int y1 = y - 1, k1 = k - sx;
				for(; y1 >= 0; y1--, k1 -= sx) {
					if (((changed >> k1) & 1) != 0) continue; // skip hole

					// move from x,y1 to x,y
					cells[k1].transferTo(cells[k], mAnimThread, false /* visible */);

					// y1/k1 is now empty, it moved to y/k (1=hole, 0=filled)
					changed |= (1L << k1);
					changed = changed & ~(1L << k);

					// animate fall "down" on x,y
					int m = (y - y1) * 4;
					int px = 0;
					int py = getContext().getBoardView().getCellSy() / 4;
					if (mUseAnims) mAnimThread.startFlyOver(cells[k], px, py, m);
					cells[k].setVisible(true);

					// find next hole to fill
					for(y--, k -= sx; y >= y1; y--, k -= sx) {
						if (((changed >> k) & 1) != 0) break;
					}
				}

				// fill remaining top holes from y to 0
				h = y + 1;
				for(; y >= 0; y--, k-= sx) {
	        		int i = mRandom.nextInt(n);
	        		createCell(x, y, h, mSprites[i]);
				}
			}
		}

		mAnimThread.queueGameplayEvent(this, EVENT_CHECK);
	}

	/* This runs in the anim thread */
	/**
	 * Check if there are 3 or more sprites of the same kind in a row, horizontally
	 * or vertically. If there is, use a flood-fill algorithm to mark them for removal.
	 * <p/>
	 * An int64 is used to keep track of cells already marked.
	 * <p/>
	 * Eventually update score based on how many cells will be removed.
	 */
	private void doCheck() {
		mAnimThread.stopAnim(mFirstCell);
		mAnimThread.stopAnim(mSecondCell);

		/*
		 * Check if there are 3 or more sprites of the same kind in a row.
		 */

		long changed = 0; // assumption: mBx * mBy < 64
		final int sy = mBy;
		final int sx = mBx;
		final int sy2 = sy - 2;
		final int sx2 = sx - 2;
		final int sxM2 = sx * 2;

		Cell[] cells = mBoard.getBoardArray(); // cells in order y(x)

		for(int y = 0, k = 0; y < sy; y++) {
			for(int x = 0; x < sx; x++, k++) {
				// ignore if already changed
				if (((changed >> k) & 1) != 0) continue;

				// get sprite to compare with
				Sprite s = cells[k].getSprite();
				if (s == null) continue;

				boolean ok = false;

				// check horizontal
				int k1 = k+1;
				int k2 = k+2;
				ok = x < sx2 &&
				     ((changed >> k1) & 1) == 0 &&
					 ((changed >> k2) & 1) == 0 &&
					 s == cells[k1].getSprite() &&
					 s == cells[k2].getSprite();

				if (!ok) {
					// check vertical
					k1 = k + sx;
					k2 = k + sxM2;
					ok = y < sy2 &&
					     ((changed >> k1) & 1) == 0 &&
						 ((changed >> k2) & 1) == 0 &&
						 s == cells[k1].getSprite() &&
						 s == cells[k2].getSprite();
				}
				if (!ok) continue;

				changed = removeAdjacent(cells, changed, x, y, k, s);

			}
		}

		if (changed != 0) {
			// increase score
           //***ivan scoreCalculator = new BijouxCalculation();
			scoreCalculator = new ConBijouxCalculation(new ConcreteScoreCalculator());
			int count = 0;
			long temp = changed;
			while (temp != 0) {
				if ((temp & 1) != 0) {
//                    mScore += ++count;

                    // BJ Calculation Strategy Yunlong Xu
                    mScore = scoreCalculator.scoreCalculation(mScore,++count);

                }
				temp = temp >> 1;
			}
			updateMessage();
			getContext().vibrateSequence(count);

			mFillChanged = changed;
			if (mUseAnims) mAnimThread.queueDelayFor(250 /* ms */);
			mAnimThread.queueGameplayEvent(this, EVENT_FILL);
		} else {
            scoreCalculator = new PenaltyCalculation();
			if (mApplyMovePenalty) {

                // BJ Penalty Strategy Yunlong Xu
//				mScore -= (mScore * MOVE_PENALTY) / 100;
                mScore = scoreCalculator.scoreCalculation(mScore, MOVE_PENALTY);
				updateMessage();
			}
			mAnimThread.queueGameplayEvent(this, EVENT_DONE);
		}
		mApplyMovePenalty = false;
	}

	/**
	 * Find all adjacent cells starting at the x/y/k position.
	 * The actual finding of adjacent cells is done in removeOne(), which uses the
	 * mTempStack stack to add new leads to investigate.
	 * This is the "outer" loop that exhaust the stack till there are no leads to examine.
	 * <p/>
	 * "changed" is a 64-bit mask that indicates which cells have already been marked.
	 */
	private long removeAdjacent(Cell[] cells, long changed,
			int x, int y, int k, Sprite s) {

		// TODO: duplicate with a down-first-then-right-left for the vertical case

		int[] stack = mTempStack;
		// stack[0] is reserved to pass the push int to the subroutine
		int pop = 1;
		int push = 1;
		stack[0] = push;
		int size = stack.length;

		while(true) {
			// process x/y/k
			changed = removeOne(cells, changed, x, y, k, s, stack);
			push = stack[0];

			if (pop == push) {
				// stack is empty
				break;
			} else {
				x = stack[pop++];
				k = (x >> 16);
				y = (x >> 8) & 0x7F;
				x = x & 0x7F;
				if (pop == size) pop = 1;
			}
		}

		return changed;
	}

	/**
	 * "inner" part of the flood-fill algorithm. See removeAdjacent().
	 */
	private long removeOne(Cell[] cells, long changed,
			int x, int y, int k, Sprite s, int[] stack) {

		/*
		 * Standard fill method except we don't recurse, we use an int array
		 * as a temporary stack.
		 * - mark N max to the right (including this)
		 * - for the N marked, if there's one up not marked, push it
		 * - for the N marked, if there's one down not marked, push it
		 * - mark N max to the left (probably 0)
		 * - for the N marked, if there's one up not marked, push it
		 * - for the N marked, if there's one down not marked, push it
		 * - loop again using the first one from the stack.
		 * The stack really is a circular buffer. Push to the end, pop from the
		 * beginning.
		 */
		// TODO: duplicate with a down-first-then-right-left for the vertical case

		int push = stack[0]; // stack[0] is reserved to pass the push index
		int size = stack.length;

		final int sy = mBy;
		final int sx = mBx;

		int n = 0;
		int x1, y1, k1, n1;
		int sy1 = sy - 1;

		// mark all right ones (always mark this one)
		for (x1 = x, k1 = k; x1 < sx; x1++, k1++) {
			if (((changed >> k1) & 1) != 0) break;
			Sprite s1 = cells[k1].getSprite();
			if (s != s1) break;

			n++;
			changed = mark(cells, k1, changed);
		}

		// recurse on all upper on the right ones
		if (y > 0) {
			for (n1 = 0, x1 = x, y1 = y - 1, k1 = k - sx; n1 < n; n1++, x1++, k1++) {
				if (((changed >> k1) & 1) != 0) continue;
				Sprite s1 = cells[k1].getSprite();
				if (s != s1) continue;

				// push
				stack[push++] = (k1 << 16) | (y1 << 8) | x1;
				if (push == size) push = 1;
			}
		}

		// recurse on all lower on the right ones
		if (y < sy1) {
			for (n1 = 0, x1 = x, y1 = y + 1, k1 = k + sx; n1 < n; n1++, x1++, k1++) {
				if (((changed >> k1) & 1) != 0) continue;
				Sprite s1 = cells[k1].getSprite();
				if (s != s1) continue;

				// push
				stack[push++] = (k1 << 16) | (y1 << 8) | x1;
				if (push == size) push = 1;
			}
		}

		// mark all left ones
		n = 0;
		for (x1 = x - 1, k1 = k - 1; x1 >= 0; x1--, k1--) {
			if (((changed >> k1) & 1) != 0) break;
			Sprite s1 = cells[k1].getSprite();
			if (s != s1) break;

			n++;
			changed = mark(cells, k1, changed);
		}

		if (n > 0) {
			// recurse on all upper on the left ones
			if (y > 0) {
				for (n1 = 0, x1 = x - 1, y1 = y - 1, k1 = k - sx; n1 < n; n1++, x1--, k1--) {
					if (((changed >> k1) & 1) != 0) continue;
					Sprite s1 = cells[k1].getSprite();
					if (s != s1) continue;

					// push
					stack[push++] = (k1 << 16) | (y1 << 8) | x1;
					if (push == size) push = 1;
				}
			}

			// recurse on all lower on the left ones
			if (y < sy1) {
				for (n1 = 0, x1 = x - 1, y1 = y + 1, k1 = k + sx; n1 < n; n1++, x1--, k1--) {
					if (((changed >> k1) & 1) != 0) continue;
					Sprite s1 = cells[k1].getSprite();
					if (s != s1) continue;

					// push
					stack[push++] = (k1 << 16) | (y1 << 8) | x1;
					if (push == size) push = 1;
				}
			}
		}

		stack[0] = push;
		return changed;
	}

	/**
	 * Marks one cell for removal in the bit field.
	 * Also start a shrinking animation on it.
	 */
	private long mark(Cell[] cells, int k1, long changed) {
		if (mUseAnims) mAnimThread.startShrink(cells[k1], 2, 10 /* 1s @ 10fps */);
		changed |= (1L << k1);

		return changed;
	}

	/* This runs in the anim thread */
	private void doDone() {
		mFirstCell = null;
		mSecondCell = null;
	}

}


