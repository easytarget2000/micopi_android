package org.eztarget.micopi.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

import org.eztarget.micopi.R;

/**
 * Created by michel on 21/12/15.
 *
 * Super class for Activities in which images are being generated
 */
public class TaskActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int contactPerm = checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
            );

            if (contactPerm != PackageManager.PERMISSION_GRANTED) onBackPressed();
        }
    }

    protected void showSuccess() {
        final View layout = findViewById(R.id.group_success);
        if (layout == null) return;
        final View arrow = findViewById(R.id.image_success);

        final ScaleAnimation poundInAnimation = new ScaleAnimation(
                0f,
                1f,
                0f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        poundInAnimation.setInterpolator(new OvershootInterpolator());
        poundInAnimation.setDuration(1000L);
        poundInAnimation.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        layout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fadeOutView(layout);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );

        layout.setVisibility(View.VISIBLE);
        arrow.setVisibility(View.VISIBLE);
        arrow.startAnimation(poundInAnimation);
    }

    protected void fadeOutView(final View view) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(1000L);
        fadeOut.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );
        view.startAnimation(fadeOut);
    }

}
