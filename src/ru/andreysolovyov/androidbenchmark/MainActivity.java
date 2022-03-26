package ru.andreysolovyov.androidbenchmark;

import java.util.ArrayList;
import java.util.Random;

import ru.andreysolovyov.androidbenchmark.client.BenchmarkResults;
import ru.andreysolovyov.androidbenchmark.client.ServerWorker;
import ru.andreysolovyov.androidbenchmark.utils.DevicePropertiesReceiver;
import ru.andreysolovyov.androidbenchmark.utils.TirelessAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// Элементы UI
	private ImageView splashImageView;
	private ImageView lavrImageView;
	private ServerWorker sw;
	private TextView modelTextView;
	private Button mainButton;

	private TextView intProgressTextView;
	private TextView floatProgressTextView;
	private TextView doubleProgressTextView;

	private TextView lavrTextView;
	//

	private CalculationType type;
	private Handler handler = new Handler();

	private BenchmarkResults thisDeviceResults;

	private ArrayList<BenchmarkResults> nearestResults;

	private TirelessAnimator animator;

	// public static boolean calculate = true;

	public static final int REQUEST_SUBMIT_RESULTS = 11;
	public static final int REQUEST_SHOW_RESULTS = 21;

	private long seed = 123;
	private int pow = 18;
	private int runs = 3;
	private int threadCount = DevicePropertiesReceiver.getNumberOfCores();
	private int iterationCount = 30;
	private int SPLASHTIME = 3000;
	private int ANIMATION_DURATION = 500;
	private int arraySize = (int) Math.pow(2, pow);

	private String deviceName = DevicePropertiesReceiver.getDeviceName();
	// private BenchmarkResults results = new BenchmarkResults();

	java.util.Random random = new Random(seed);

	enum CalculationType {
		INTEGER, FLOAT, DOUBLE;
	}

	enum MainButtonBehavior {
		CALCULATE, SUBMIT_RESULTS, SHOW_RESULTS;
	}

	MainButtonBehavior mainButtonCurrentBehavior;

	ConnectivityManager conMgr;

	class BenchmarkingThread extends Thread {

		public long doTheBenchmarking() {
			Log.d("Benchmark ", "doTheBenchmarking()");
			long operationsPerMillisecond = -1;
			long averageRunTime = -1;
			prepareCalculations(arraySize);
			try {
				double sum = 0;
				long totalTimeElapsed = 0;

				for (int run = 0; run < runs; run++) {
					sum = 0;
					final long computationsStarted = System.currentTimeMillis();
					for (int i = 0; i < iterationCount; i++) {
						sum = calculateSum(); // самая суть
					}

					final long computationsEnded = System.currentTimeMillis();
					totalTimeElapsed += computationsEnded - computationsStarted;
				}

				// Вычислить всякие средние
				averageRunTime = totalTimeElapsed / runs; // миллисекунд
				// размер массива * число арифм операций * число итераций /
				// время прогона
				operationsPerMillisecond = arraySize * 4 * iterationCount
						/ averageRunTime;

				Log.d("Benchmark ", "ТИП + " + type + " cреднее за прогон "
						+ averageRunTime + " операций в мсек "
						+ operationsPerMillisecond);

			} catch (final Exception e) {
				// TODO
			} finally {
				resetArrays();
			}
			return operationsPerMillisecond;
		}

		public void run() {
			Log.d("Benchmark ", "run()");

			// Вычисления с интом
			type = CalculationType.INTEGER;
			handler.post(new Runnable() {
				public void run() {
					intProgressTextView.setText("           ");
					animator.setViewToAnimate(intProgressTextView);
					animator.startAnimating();
				}
			});

			final int intOps = (int) doTheBenchmarking();
			handler.post(new Runnable() {
				public void run() {
					animator.stopAnimating();
					intProgressTextView.clearAnimation();
					intProgressTextView.setText(Integer.toString(intOps));
				}
			});
			try {
				Thread.sleep(ANIMATION_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Вычисления с флоатом
			type = CalculationType.FLOAT;
			handler.post(new Runnable() {
				public void run() {
					floatProgressTextView.setText("           ");
					animator.setViewToAnimate(floatProgressTextView);
					animator.startAnimating();
				}
			});

			final int floatOps = (int) doTheBenchmarking();
			handler.post(new Runnable() {
				public void run() {
					animator.stopAnimating();
					floatProgressTextView.clearAnimation();
					floatProgressTextView.setText(Integer.toString(floatOps));
				}
			});
			try {
				Thread.sleep(ANIMATION_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Вычисления с даблом
			type = CalculationType.DOUBLE;
			handler.post(new Runnable() {
				public void run() {
					doubleProgressTextView.setText("           ");
					animator.setViewToAnimate(doubleProgressTextView);
					animator.startAnimating();
				}
			});

			final int doubleOps = (int) doTheBenchmarking();
			handler.post(new Runnable() {
				public void run() {
					animator.stopAnimating();
					doubleProgressTextView.clearAnimation();
					doubleProgressTextView.setText(Integer.toString(doubleOps));
				}
			});

			int overallMark = (intOps + floatOps + doubleOps) / 3;

			thisDeviceResults = new BenchmarkResults(deviceName, intOps,
					floatOps, doubleOps, overallMark);

			Log.d("Benchmark ", thisDeviceResults.toString());

			handler.post(new Runnable() {
				public void run() {
					lavrTextView = placeResultTextViewInLavr(Integer
							.toString(thisDeviceResults.overallMark));
					mainButton.setEnabled(true);
					mainButtonCurrentBehavior = MainButtonBehavior.SUBMIT_RESULTS;
					mainButton.setText(getResources().getString(R.string.main_button_submit_results));
					
					
					
					
					
				}
			});
		}
	};

	/*
	 * TextView preparingDataTextView; TextView calculatedResultTextView;
	 * 
	 * EditText runsEditText; EditText powEditText; EditText
	 * threadCountEditText; EditText iterationCountEditText;
	 * 
	 * RadioGroup floatPointRadioGroup;
	 */

	// final static int MAX_SIZE = (int) Math.pow(2, 30);

	TreeReductiveRunnable[] treeReductives;
	static int[] intArrayToReduce;
	static float[] floatArrayToReduce;
	static double[] doubleArrayToReduce;

	abstract class TreeReductiveRunnable implements Runnable {
		int startPosition, endPosition;

		int getStartPosition() {
			return startPosition;
		}

		void setStartPosition(int position) {
			startPosition = position;
		}

		int getEndPosition() {
			return startPosition;
		}

		void setEndPosition(int position) {
			endPosition = position;
		}

		abstract void resetSum();
	}

	class IntegerTreeReductiveRunnable extends TreeReductiveRunnable {
		int sum;

		@Override
		public void run() {
			for (int i = startPosition; i < endPosition; i++) {
				sum += intArrayToReduce[i];
				sum *= intArrayToReduce[i];
				sum -= intArrayToReduce[i];
				sum /= intArrayToReduce[i];
			}
		}

		void resetSum() {
			sum = 0;
		}

		int getSum() {
			return sum;
		}
	}

	class FloatTreeReductiveRunnable extends TreeReductiveRunnable {
		float sum;

		@Override
		public void run() {
			for (int i = startPosition; i < endPosition; i++) {
				sum += floatArrayToReduce[i];
				sum *= floatArrayToReduce[i];
				sum -= floatArrayToReduce[i];
				sum /= floatArrayToReduce[i];
			}
		}

		void resetSum() {
			sum = 0;
		}

		float getSum() {
			return sum;
		}
	}

	class DoubleTreeReductiveRunnable extends FloatTreeReductiveRunnable {
		@Override
		public void run() {
			for (int i = startPosition; i < endPosition; i++) {
				sum += doubleArrayToReduce[i];
				sum *= doubleArrayToReduce[i];
				sum -= doubleArrayToReduce[i];
				sum /= doubleArrayToReduce[i];
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Ядра ", "" + threadCount);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		splashImageView = (ImageView) findViewById(R.id.splashImageView);

		mainButtonCurrentBehavior = MainButtonBehavior.CALCULATE;

		modelTextView = (TextView) findViewById(R.id.modelTextView);
		modelTextView.setText(deviceName);
		mainButton = (Button) findViewById(R.id.mainButton);
		mainButton.setText(getResources().getString(R.string.main_button_estimate));
		handler.postDelayed(new Runnable() {
			public void run() {
				splashImageView.setVisibility(View.GONE);
			}
		}, SPLASHTIME);

		intProgressTextView = (TextView) findViewById(R.id.intProgressTextView);
		floatProgressTextView = (TextView) findViewById(R.id.floatProgressTextView);
		doubleProgressTextView = (TextView) findViewById(R.id.doubleProgressTextView);

		animator = new TirelessAnimator(this);
		lavrImageView = (ImageView) findViewById(R.id.lavrImageView);
	}

	// Вызывется прямо перед показом меню. Последние штрихи вносятся тут.
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainButtonCurrentBehavior == MainButtonBehavior.CALCULATE) {
			menu.findItem(R.id.reset_settings).setEnabled(false);
		} else {
			menu.findItem(R.id.reset_settings).setEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.server_settings:
			showServerDialog();
			break;
		case R.id.reset_settings:
			reset();
			break;
		}
		return true;
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	public void onMainButtonClicked(View v) {
		switch (mainButtonCurrentBehavior) {
		case CALCULATE:
			Log.d("Benchmark ", "Calculate Click!");
			mainButton.setText(getResources().getString(R.string.main_button_calculation_in_progress));
			mainButton.setEnabled(false);
			BenchmarkingThread bt = new BenchmarkingThread();
			bt.start();
			break;
		case SUBMIT_RESULTS:
			if (sw == null) {
				sw = new ServerWorker(this);
			}
			if (!isNetworking()) {
				makeErrorDialog(getResources().getString(R.string.error), 
						getResources().getString(R.string.error_no_internet_connection))
						.show();
				return;
			}
			final ProgressDialog serverAccessProgressDialog = makePorgressDialog(
					getResources().getString(R.string.wait), 
					getResources().getString(R.string.wait_server_connection));
			Thread netWorker = new Thread() {
				public void run() {
					// Log.d("Benchmark ",sw.addResults(results));

					ArrayList<BenchmarkResults> results = sw
							.submitResults(thisDeviceResults);

					handler.post(new Runnable() {
						@Override
						public void run() {
							serverAccessProgressDialog.dismiss();
						}
					});

					if (results == null) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								makeErrorDialog(getResources().getString(R.string.error),
										getResources().getString(R.string.error_server_fault))
										.show();
							}
						});
					} else {
						nearestResults = results;
						mainButtonCurrentBehavior = MainButtonBehavior.SHOW_RESULTS;

						Intent chartActivityIntent = new Intent(
								MainActivity.this, ChartActivity.class);
						chartActivityIntent.putExtra("nearestResults", results);
						startActivityForResult(chartActivityIntent,
								REQUEST_SUBMIT_RESULTS);
					}
				}
			};
			netWorker.start();
			Log.d("Benchmark ", "Send to WS");
			break;
		case SHOW_RESULTS:
			Intent chartActivityIntent = new Intent(MainActivity.this,
					ChartActivity.class);
			chartActivityIntent.putExtra("nearestResults", nearestResults);
			startActivity(chartActivityIntent);
			break;
		}
	}

	private void prepareCalculations(final int arraySize) {
		// если число элементов не делится на колво тредов
		// то число элементов последнего массива больше на размер остатка
		treeReductives = new TreeReductiveRunnable[threadCount];

		switch (type) {
		case INTEGER:
			intArrayToReduce = new int[arraySize];
			for (int i = 0; i < arraySize; i++) {
				intArrayToReduce[i] = random.nextInt(5) + 25;
			}
			for (int i = 0; i < threadCount; i++) {
				treeReductives[i] = new IntegerTreeReductiveRunnable();
			}
			break;
		case FLOAT:
			floatArrayToReduce = new float[arraySize];
			for (int i = 0; i < arraySize; i++) {
				floatArrayToReduce[i] = random.nextFloat() * 5 + 25;
			}
			for (int i = 0; i < threadCount; i++) {
				treeReductives[i] = new FloatTreeReductiveRunnable();
			}
			break;
		case DOUBLE:
			doubleArrayToReduce = new double[arraySize];
			for (int i = 0; i < arraySize; i++) {
				doubleArrayToReduce[i] = random.nextDouble() * 5 + 25;
			}
			for (int i = 0; i < threadCount; i++) {
				treeReductives[i] = new DoubleTreeReductiveRunnable();
			}
			break;
		}

		int residual = arraySize % threadCount;
		for (int i = 0; i < threadCount; i++) {
			// если число элементов не делится на колво тредов
			// то число элементов последнего массива больше на размер
			// остатка
			int offset = (i != threadCount - 1 ? arraySize / threadCount
					: arraySize / threadCount + residual);

			if (i != 0) {
				treeReductives[i].startPosition = treeReductives[i - 1].endPosition;
			}
			treeReductives[i].endPosition = treeReductives[i].startPosition
					+ offset;
		}
	}

	private double calculateSum() throws Exception {
		double sum = 0;

		Thread[] threads = prepareThreads();

		int length = threads.length;
		for (int i = 0; i < length; i++) {
			threads[i].start();
		}

		for (int i = 0; i < length; i++) {
			threads[i].join();
		}
		for (int i = 0; i < length; i++) {
			switch (type) {
			case INTEGER:
				sum += ((IntegerTreeReductiveRunnable) treeReductives[i])
						.getSum();
				break;
			case FLOAT:
				sum += ((FloatTreeReductiveRunnable) treeReductives[i])
						.getSum();
				break;
			case DOUBLE:
				sum += ((DoubleTreeReductiveRunnable) treeReductives[i])
						.getSum();
				break;
			}
		}
		return sum;
	}

	private Thread[] prepareThreads() {
		int length = treeReductives.length;
		Thread[] threads = new Thread[length];
		for (int i = 0; i < length; i++) {
			treeReductives[i].resetSum();
			threads[i] = new Thread(treeReductives[i]);
		}
		return threads;
	}

	private void resetArrays() {
		intArrayToReduce = null;
		floatArrayToReduce = null;
		doubleArrayToReduce = null;
		// System.gc();
	}

	private boolean isNetworking() {
		NetworkInfo i = conMgr.getActiveNetworkInfo();
		if (i == null)
			return false;
		if (!i.isConnected())
			return false;
		if (!i.isAvailable())
			return false;
		return true;
	}

	public TextView placeResultTextViewInLavr(String text) {
		// BAD DESIGN
		int[] lavrCoordinates = new int[2];
		lavrImageView.getLocationOnScreen(lavrCoordinates);

		int lavrHeight = lavrImageView.getHeight();
		int lavrWidth = lavrImageView.getWidth();

		TextView resultTextView;

		if (lavrTextView != null) {
			resultTextView = lavrTextView;
		} else {
			resultTextView = new TextView(this);
		}
		resultTextView.setText(text);
		resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f);

		resultTextView.measure(0, 0); // must call measure!
		int resultTextViewMasuredHeight = resultTextView.getMeasuredHeight(); // get
																				// width
		int resultTextViewMasuredWidth = resultTextView.getMeasuredWidth(); // get
																			// height

		int resultTextViewTopX = (lavrWidth - resultTextViewMasuredWidth) / 2;
		int resultTextViewTopY = lavrHeight / 2 - resultTextViewMasuredHeight;

		resultTextViewTopX += lavrCoordinates[0];
		resultTextViewTopY += lavrCoordinates[1];

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				resultTextViewMasuredWidth, resultTextViewMasuredHeight);
		params.leftMargin = resultTextViewTopX;
		params.topMargin = resultTextViewTopY;
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainRelativeLayout);
		
		if(lavrTextView != null){
			rl.removeView(lavrTextView);
		}
		
		rl.addView(resultTextView, params);
		return resultTextView;
	}

	private AlertDialog makeErrorDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		return dialog;
	}

	private ProgressDialog makePorgressDialog(String title, String message) {
		return ProgressDialog.show(this, title, message);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_SUBMIT_RESULTS) {
			if (resultCode == ChartActivity.RESULT_OK) {
				mainButtonCurrentBehavior = MainButtonBehavior.SHOW_RESULTS;
				mainButton.setText(getResources().getString(R.string.main_button_show_results));
			}
		}
	}

	private void reset() {
		mainButtonCurrentBehavior = MainButtonBehavior.CALCULATE;

		String stars = getResources().getString(R.string.stars);
		intProgressTextView.setText(stars);
		floatProgressTextView.setText(stars);
		doubleProgressTextView.setText(stars);
		lavrTextView.setText("");

		mainButton.setText(getResources().getString(R.string.main_button_estimate));
	}

	private void showServerDialog() {
		if (sw == null) {
			sw = new ServerWorker(this);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout serverDialogLinearLayout = (LinearLayout) inflater
				.inflate(R.layout.server_dialog, (ViewGroup) getCurrentFocus());

		final EditText URLEditText = (EditText) serverDialogLinearLayout
				.findViewById(R.id.URLEditText);
		final EditText operationEditText = (EditText) serverDialogLinearLayout
				.findViewById(R.id.operationEditText);

		URLEditText.setText(sw.getURL());
		operationEditText.setText(sw.getOperation());

		builder.setTitle(getResources().getString(R.string.choose_server));
		builder.setView(serverDialogLinearLayout);
		builder.setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				sw.setServerInfo(URLEditText.getText().toString(),
						operationEditText.getText().toString());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();

	}
}
