package ru.andreysolovyov.androidbenchmark;

import java.util.ArrayList;

import ru.andreysolovyov.androidbenchmark.client.BenchmarkResults;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ChartView extends View {
	Paint paint = new Paint();
	private static final int SYMBOLS_IN_MODEL = 15;
	private static int BAR_OTHER_DEVICE = Color.rgb(0x33, 0xB5, 0xE5);
	private static int BAR_YOUR_DEVICE = Color.rgb(0xFF, 0xBB, 0x33);

	public ArrayList<BenchmarkResults> nearestResults;

	public ChartView(Context context) {
		super(context);
	}

	public ChartView(Context context, AttributeSet set) {
		super(context, set);
	}

	public ChartView(Context context, AttributeSet set, int dfa) {
		super(context, set, dfa);
	}

	public ChartView(Context context, ArrayList<BenchmarkResults> resultsList) {
		super(context);
		this.nearestResults = resultsList;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (nearestResults != null) {
			float WIDTH = canvas.getWidth();
			float HEIGHT = canvas.getHeight();

			float TOP_SPACE = HEIGHT * 0.0434f + getStatusBarHeight();// 50 - 1
																		// +
																		// getStatusBarHeight()
																		// -
																		// 39/4;
			// float TOP_SPACE_WITHOUT_STATUS_BAR = HEIGHT * 0.0434f;
			float BAR_HEIGHT = HEIGHT * 0.0586f;
			float INTERBAR_SPACE = HEIGHT * 0.0304f;
			float SIDE_SPACE = WIDTH * 0.0556f;
			float BAR_LENGTH = WIDTH - SIDE_SPACE * 2;

			float TEXT_SIZE = BAR_HEIGHT * 2f / 3f; // 36;
			float TEXT_X_OFFSET = TEXT_SIZE / 4f;
			float TEXT_Y_OFFSET = TEXT_SIZE / 2f - TEXT_SIZE / 9;

			Log.i("Benchmark top", "" + TOP_SPACE);
			Log.i("Benchmark hw ", canvas.getHeight() + " " + canvas.getWidth());

			paint.setStyle(Paint.Style.FILL);
			// paint.setColor(Color.WHITE);
			// canvas.drawPaint(paint);

			paint.setAntiAlias(true);
			paint.setColor(BAR_OTHER_DEVICE);
			paint.setTextSize(TEXT_SIZE);

			/*
			 * Resources r = getResources(); float px =
			 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
			 * r.getDisplayMetrics());
			 * 
			 * float topX = 30, topY = 30, length = 480; canvas.drawRect(topX,
			 * topY, topX + length, topY + px, paint);
			 * 
			 * for(int i = 21; i < 29 ; i++){ topY += px + 30; px =
			 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, i,
			 * r.getDisplayMetrics()); canvas.drawRect(topX, topY, topX +
			 * length, topY + px, paint); }
			 */

			float topX = SIDE_SPACE, topY = TOP_SPACE;
			// int step = 0;
			if (!nearestResults.isEmpty()) {
				float quantum = BAR_LENGTH / nearestResults.get(0).overallMark;

				for (BenchmarkResults results : nearestResults) {
					// for(int i = 0; i < 11; i++){
					if (results.model.equalsIgnoreCase(getResources()
							.getString(R.string.your_device))) {
						paint.setColor(BAR_YOUR_DEVICE);
						canvas.drawRect(topX, topY, topX + quantum
								* results.overallMark, topY - BAR_HEIGHT, paint);
					} else {
						canvas.drawRect(topX, topY, topX + quantum
								* results.overallMark, topY - BAR_HEIGHT, paint);
					}

					paint.setColor(Color.BLACK);
					canvas.drawText(getDrawableResultsString(results), topX
							+ TEXT_X_OFFSET, (topY - TEXT_Y_OFFSET), paint);
					paint.setColor(BAR_OTHER_DEVICE);

					// step += (BAR_LENGTH/22);
					topY += BAR_HEIGHT + INTERBAR_SPACE;
					// }

				}
			}
		}
	}

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		Log.i("Benchmark status bar", "" + result);
		return result;
	}

	private String getDrawableResultsString(BenchmarkResults results) {
		String result = "";
		if (results != null) {
			String model = (results.model.length() <= SYMBOLS_IN_MODEL) ? results.model
					: results.model.substring(0, SYMBOLS_IN_MODEL - 1);
			result = model + ": " + results.overallMark;
		}
		return result;
	}
}
