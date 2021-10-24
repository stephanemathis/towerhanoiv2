package fr.mathis.tourhanoipro;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class CongratsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Tools.applyTranslucentColoredTheme(this);

        setContentView(R.layout.activity_congrats);

        int userMovements = getIntent().getExtras().getInt("userMovements");
        int totalMovements = getIntent().getExtras().getInt("totalMovements");
        long time = getIntent().getExtras().getLong("time");

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
        tvTowerSize.setText(getString(R.string.congrats_summary).replace(":tower", "" + towerDiskCount));

        RelativeLayout container =findViewById(R.id.fl_container);

        findViewById(R.id.fabRestart).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("more", false);
                setResult(1, i);
                finish();
            }
        });

        findViewById(R.id.fabNextLevel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("more", true);
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
            }
        });



    }
}