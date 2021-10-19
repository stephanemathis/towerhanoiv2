package fr.mathis.tourhanoipro;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.ui.tuto.TutoPagerAdapter;
import fr.mathis.tourhanoipro.view.CustomPagerIndicator;

public class TutoActivity extends AppCompatActivity {

    ViewPager2 pager;
    TextView tvStep;
    CustomPagerIndicator cpi;
    View vBottomContainer;
    View vBottomSeparator;
    boolean connectPlayGames;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Tools.applyColoredTheme(this);

        setContentView(R.layout.activity_tuto);

        connectPlayGames = getIntent().getBooleanExtra("connectClient", false);

        pager = (ViewPager2) findViewById(R.id.tuto_pager);
        tvStep = (TextView) findViewById(R.id.tv_step);
        vBottomContainer = findViewById(R.id.rl_indicator_container);
        vBottomSeparator = findViewById(R.id.ll_indicator_separator);

        TutoPagerAdapter pagerAdapter = new TutoPagerAdapter(this);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(5);

        tvStep.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cpi = findViewById(R.id.cpi_pager_indicator);


        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                cpi.updateBounds(position, TutoPagerAdapter.NB_STEPS, positionOffset);

                if (position == pager.getAdapter().getItemCount() - 2) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        vBottomContainer.setTranslationX(-positionOffsetPixels);
                        vBottomSeparator.setTranslationX(-positionOffsetPixels);
                    } else {
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(vBottomContainer, "translationX", 0, -positionOffsetPixels);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(vBottomSeparator, "translationX", 0, -positionOffsetPixels);
                        anim1.start();
                        anim2.start();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position + 1 == TutoPagerAdapter.NB_STEPS) {
                    tvStep.setText(R.string.tuto_skip_end);

                    Timer t = new Timer();
                    t.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    finish();
                                    overridePendingTransition(0, 0);
                                }
                            });

                        }
                    }, 200);

                } else {
                    tvStep.setText(R.string.tuto_skip);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    public void unloackHack(int id) {
        if (connectPlayGames) {
            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            if (status == ConnectionResult.SUCCESS) {

                GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {

                    AchievementsClient mAchievementsClient = Games.getAchievementsClient(this, account);
                    mAchievementsClient.unlock(getString(id));
                }
            }
        }
    }
}