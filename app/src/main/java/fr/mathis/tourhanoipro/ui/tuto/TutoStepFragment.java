package fr.mathis.tourhanoipro.ui.tuto;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.TutoActivity;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.view.StateView;
import fr.mathis.tourhanoipro.view.game.GameView;

public class TutoStepFragment extends Fragment {

    int num = -1;

    public static TutoStepFragment newInstance(int num) {
        TutoStepFragment fragment = new TutoStepFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    public TutoStepFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        num = getArguments().getInt("num");

        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = null;

        switch (num) {
            case 0:
                v = inflater.inflate(R.layout.fragment_step0, container, false);

                StateView sv = (StateView) v.findViewById(R.id.sv_logo);
                sv.setSvgResource(R.raw.logo);
                sv.reveal(v, 0);
                sv.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((StateView) v).reset();
                        ((StateView) v).reveal(v.getRootView(), 0);

                        ((TutoActivity) getActivity()).unloackHack(R.string.achievement_amazed);
                    }
                });

                TextView tvName = (TextView) v.findViewById(R.id.tv_name);
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvName, "alpha", 0.0f, 1.0f);
                alphaAnimator.setDuration(300);
                alphaAnimator.setStartDelay(1800);

                ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(tvName, "translationY", 0.0f, -Tools.convertDpToPixel(40));
                translationAnimator.setDuration(300);
                translationAnimator.setStartDelay(1800);

                alphaAnimator.start();
                translationAnimator.start();

                break;
            case 1:
                v = inflater.inflate(R.layout.fragment_step1, container, false);
                ((GameView) v.findViewById(R.id.gv_tuto_1)).setDisabled(true);
                ((GameView) v.findViewById(R.id.gv_tuto_1)).setDemoMode(GameView.MODE_GOAL);
                break;
            case 2:
                v = inflater.inflate(R.layout.fragment_step2, container, false);
                ((GameView) v.findViewById(R.id.gv_tuto_2)).setDisabled(true);
                ((GameView) v.findViewById(R.id.gv_tuto_2)).setDemoMode(GameView.MODE_MULTIPLE);
                ((GameView) v.findViewById(R.id.gv_tuto_3)).setDisabled(true);
                ((GameView) v.findViewById(R.id.gv_tuto_3)).setDemoMode(GameView.MODE_SIZE);
                break;
            case 3:
                v = inflater.inflate(R.layout.fragment_step3, container, false);
                break;
            default:
                break;
        }

        return v;
    }
}