package fr.mathis.tourhanoipro;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;

import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.ui.home.HomeFragment;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class CongratsActivity extends AppCompatActivity {

    int userMovements;
    int totalMovements;
    long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Tools.applyTranslucentColoredTheme(this);

        setContentView(R.layout.activity_congrats);

        userMovements = getIntent().getExtras().getInt("userMovements");
        totalMovements = getIntent().getExtras().getInt("totalMovements");
        time = getIntent().getExtras().getLong("time");

        KonfettiView konfetti = findViewById(R.id.kvConfetti);

        TextView tvMovements = (TextView) findViewById(R.id.tv_movements);
        tvMovements.setText("" + userMovements);

        TextView tvSecondes = (TextView) findViewById(R.id.tv_secondes);
        TextView tvMinutes = (TextView) findViewById(R.id.tv_minutes);

        int minutes = (int) time / (60000);
        long secondes = time % (60000);

        DecimalFormat df = null;
        if (time < 20000) {
            df = new DecimalFormat("###################.0");
        } else {
            df = new DecimalFormat("###################");
        }

        String secondsText = df.format(((float) secondes / 1000.0f));

        try {
            Integer.parseInt(secondsText.charAt(0) + "");
        } catch (Exception nfe) {
            secondsText = "0" + secondsText;
        }

        try {
            Integer.parseInt(secondsText.charAt(secondsText.length() - 2) + "");
        } catch (Exception nfe) {
            if (secondsText.endsWith("0"))
                secondsText = secondsText.substring(0, secondsText.length() - 2);
        }

        tvSecondes.setText(secondsText);
        tvMinutes.setText("" + (minutes < 10 ? "0" + minutes : minutes));

        if (time < 60000) {
            findViewById(R.id.ll_minutes).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tv_secondes_desc)).setText(getString(R.string.congrats_seconds));
        }

        TextView tvPerfect = (TextView) findViewById(R.id.tv_perfect);
        boolean perfectScore = userMovements == totalMovements;
        if (perfectScore) {
            tvPerfect.setVisibility(View.VISIBLE);
        } else {
            tvPerfect.setVisibility(View.GONE);
        }

        TextView tvTowerSize = (TextView) findViewById(R.id.tv_towerSize);
        int towerDiskCount = (int)(Math.log(totalMovements + 1) / Math.log(2));
        tvTowerSize.setText(getString(R.string.congrats_summary).replace(":tower", "" + towerDiskCount).replace(":plurial", towerDiskCount > 1 ? "s" : ""));

        RelativeLayout container = findViewById(R.id.fl_container);

        findViewById(R.id.fabRestart).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(HomeFragment.RESULT_INCREMENT_DISK, false);
                setResult(1, i);
                finish();
            }
        });

        findViewById(R.id.fabNextLevel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(HomeFragment.RESULT_INCREMENT_DISK, true);
                setResult(1, i);
                finish();
            }
        });

        container.setOnTouchListener((v, event) -> {

            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN)
            {
                konfetti.build()
                        .addColors(Tools.getRandomColorPalette(this))
                        .setDirection(0.0, 359.0)
                        .setSpeed(2f, 4f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 6f), new Size(10, 6f), new Size(14, 6f))
                        .setPosition(event.getX(), event.getX(), event.getY(), event.getY())
                        .burst(50);

            }

            return false;
        });

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                ArrayList<Integer> colors = new ArrayList<Integer>();
                colors.add(Color.parseColor("#33B5E5"));
                colors.add(Color.parseColor("#99CC00"));
                colors.add(Color.parseColor("#FF4444"));
                colors.add(Color.parseColor("#FFBB33"));
                colors.add(Color.parseColor("#AA66CC"));

                Long time = 2000L;

                konfetti.build()
                        .addColors(colors)
                        .setDirection(0.0, 359.0)
                        .setSpeed(0.5f, 2f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(12, 10f), new Size(10, 10f), new Size(14, 10f))
                        .setPosition(-50f, konfetti.getWidth() + 50f, -50f, -50f)
                        .streamFor(200, time);


                unlockAchievements(userMovements, totalMovements, time);
            }
        });


    }

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;

    private void unlockAchievements(int nbCoup, int nbTotal, long miliseconds)
    {
        // if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if(account != null) {
                GamesClient mGamesClient = Games.getGamesClient(this, account);

                mGamesClient.setViewForPopups(findViewById(android.R.id.content));
                mGamesClient.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

                mAchievementsClient = Games.getAchievementsClient(this, account);
                mLeaderboardsClient = Games.getLeaderboardsClient(this, account);

                mAchievementsClient.increment(getString(R.string.achievement_beginner), (int) (Math.log(nbCoup + 1) / Math.log(2)));
                mAchievementsClient.increment(getString(R.string.achievement_expert), (int) (Math.log(nbCoup + 1) / Math.log(2)));
                if (nbTotal == 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_1));
                } else if (nbTotal == Math.pow(2, 2) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_2));
                } else if (nbTotal == Math.pow(2, 3) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_3));
                } else if (nbTotal == Math.pow(2, 4) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_4));
                } else if (nbTotal == Math.pow(2, 5) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_5));
                    if (nbCoup == Math.pow(2, 5) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_5));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_5_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 6) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_6));
                    if (nbCoup == Math.pow(2, 6) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_6));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_6_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 7) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_7));
                    if (nbCoup == Math.pow(2, 7) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_7));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_7_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 8) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_8));
                    if (nbCoup == Math.pow(2, 8) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_8));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_8_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 9) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_9));
                    if (nbCoup == Math.pow(2, 9) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_9));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_9_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 10) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_10));
                    if (nbCoup == Math.pow(2, 10) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_10));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_10_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 11) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_11));
                    if (nbCoup == Math.pow(2, 11) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_11));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_11_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 12) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_12));
                    if (nbCoup == Math.pow(2, 12) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_12));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_12_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 13) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_13));
                    if (nbCoup == Math.pow(2, 13) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_13));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_13_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 14) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_14));
                    if (nbCoup == Math.pow(2, 14) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_14));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_14_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 15) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_15));
                    if (nbCoup == Math.pow(2, 15) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_15));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_15_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 16) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_16));
                    if (nbCoup == Math.pow(2, 16) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_16));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_16_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 17) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_15));
                    if (nbCoup == Math.pow(2, 17) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_15));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_17_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 18) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_16));
                    if (nbCoup == Math.pow(2, 18) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_15));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_18_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 19) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_15));
                    if (nbCoup == Math.pow(2, 19) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_15));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_19_disks), miliseconds);
                } else if (nbTotal == Math.pow(2, 20) - 1) {
                    mAchievementsClient.unlock(getString(R.string.achievement_level_15));
                    if (nbCoup == Math.pow(2, 20) - 1)
                        mAchievementsClient.unlock(getString(R.string.achievement_perfect_15));
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_fastest_for_20_disks), miliseconds);
                }

                if (nbTotal == nbCoup) {
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_perfect_score), (int) (Math.log(nbCoup + 1) / Math.log(2)));
                }
            }
        }
    }
}