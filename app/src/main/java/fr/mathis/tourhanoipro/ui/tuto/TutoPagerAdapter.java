package fr.mathis.tourhanoipro.ui.tuto;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TutoPagerAdapter extends FragmentStateAdapter {

    public static int NB_STEPS = 4;

    public TutoPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TutoStepFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return NB_STEPS;
    }
}