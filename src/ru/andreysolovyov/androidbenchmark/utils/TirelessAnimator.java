package ru.andreysolovyov.androidbenchmark.utils;

import ru.andreysolovyov.androidbenchmark.R;
import ru.andreysolovyov.androidbenchmark.R.animator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class TirelessAnimator {

	public TirelessAnimator(Context ctx) {
		fadeIn = AnimationUtils.loadAnimation(ctx, R.animator.fade_in);
		fadeIn.setAnimationListener(myFadeInAnimationListener);

		fadeOut = AnimationUtils.loadAnimation(ctx, R.animator.fade_out);
		fadeOut.setAnimationListener(myFadeOutAnimationListener);
	}

	private Animation fadeIn;
	private Animation fadeOut;

	private View viewToAnimate;
	private boolean animationEnabled = true;

	private FadeInAnimationListener myFadeInAnimationListener = new FadeInAnimationListener();
	private FadeOutAnimationListener myFadeOutAnimationListener = new FadeOutAnimationListener();

	private Runnable launchFadeOutAnimationRunnable = new Runnable() {
		public void run() {
			launchOutAnimation();
		}
	};

	private Runnable launchFadeInAnimationRunnable = new Runnable() {
		public void run() {
			launchInAnimation();
		}
	};

	private class FadeInAnimationListener implements AnimationListener {
		public void onAnimationEnd(Animation animation) {
			if (animationEnabled) {
				viewToAnimate.post(launchFadeOutAnimationRunnable);
			}
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
		}
	};

	private class FadeOutAnimationListener implements AnimationListener {
		public void onAnimationEnd(Animation animation) {
			if (animationEnabled) {
				viewToAnimate.post(launchFadeInAnimationRunnable);
			}
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
		}
	};

	public void startAnimating() {
		if (viewToAnimate != null) {
			animationEnabled = true;
			launchOutAnimation();
		}
	}

	public void stopAnimating() {
		animationEnabled = false;
		//viewToAnimate.clearAnimation();
	}

	public void launchInAnimation() {
		viewToAnimate.startAnimation(fadeIn);
	}

	public void launchOutAnimation() {
		viewToAnimate.startAnimation(fadeOut);
	}

	public void setViewToAnimate(View v) {
		viewToAnimate = v;
	}
}
