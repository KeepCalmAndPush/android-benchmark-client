package ru.andreysolovyov.androidbenchmark;

import java.util.ArrayList;

import ru.andreysolovyov.androidbenchmark.client.BenchmarkResults;
import ru.andreysolovyov.androidbenchmark.client.ServerWorker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

public class ChartActivity extends Activity {
	public static final int RESULT_OK = 21;
	public static final int RESULT_FAULT = 22;

	ArrayList<BenchmarkResults> nearestResults;

	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//chartTextView = (TextView) findViewById(R.id.chartTextView);
		nearestResults = (ArrayList<BenchmarkResults>) getIntent().getExtras().get("nearestResults");
		setContentView(R.layout.activity_draw);
		ChartView cv = (ChartView) findViewById(R.id.chartView);
		cv.nearestResults = nearestResults;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chart, menu);
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		setResult(RESULT_OK, getIntent());
	    finish();
	}
}
