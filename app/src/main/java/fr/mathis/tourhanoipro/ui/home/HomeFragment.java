package fr.mathis.tourhanoipro.ui.home;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.CongratsActivity;
import fr.mathis.tourhanoipro.MainActivity;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.DataManager;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.view.MorphView;
import fr.mathis.tourhanoipro.view.game.GameView;
import fr.mathis.tourhanoipro.view.game.listener.HelpListener;
import fr.mathis.tourhanoipro.view.game.listener.QuickTouchListener;
import fr.mathis.tourhanoipro.view.game.listener.TurnListener;
import fr.mathis.tourhanoipro.view.game.model.QuickTouch;

public class HomeFragment extends Fragment implements TurnListener, QuickTouchListener {

    private GameView gvMain;

    static final int MENU_QUICK_TOUCH_ENABLE = 1;
    static final int MENU_QUICK_TOUCH_MODIFY = 4;
    static final int MENU_QUICK_TOUCH_REMOVE = 5;
    static final int MENU_UNDO = 6;

    private MenuItem menuItemSmallTouchEnable;
    private MenuItem menuItemSmallTouchModify;
    private MenuItem menuItemSmallTouchRemove;
    private MenuItem menuItemSmallUndo;

    private ActivityResultLauncher<Intent> congratsLauncher;
    private HomeViewModel viewModel;
    boolean wantstoShowHelp;

    View stepContainer;
    View step0;
    View step1;
    View step2;

    public static final String RESULT_INCREMENT_DISK = "incrementDisk";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        congratsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean incrementDisk = false;

                    Intent i = result.getData();
                    if (i != null)
                        incrementDisk = i.getBooleanExtra(RESULT_INCREMENT_DISK, false);
                    int diskCount = PrefHelper.ReadInt(getContext(), PrefHelper.KEY_DISK_COUNT, -1);

                    if (incrementDisk) {
                        if (diskCount < 20)
                            diskCount++;
                        PrefHelper.SaveInt(getContext(), PrefHelper.KEY_DISK_COUNT, diskCount);
                        viewModel.sendDiskCountUpdate(diskCount);
                    }
                    ArrayList<String> savedGames = viewModel.getAllGames();
                    savedGames.set(viewModel.getCurrentGameIndex(), gvMain.saveGameAsString());

                    String newGame = GameView.getNewSaveData(getContext(), diskCount);
                    viewModel.getAllGames().add(0, newGame);
                    gvMain.launchGame(newGame);
                });

        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        gvMain = root.findViewById(R.id.gv_main);

        stepContainer = root.findViewById(R.id.rl_help);
        step0 = root.findViewById(R.id.ll_help_step0);
        step1 = root.findViewById(R.id.ll_help_step1);
        step2 = root.findViewById(R.id.ll_help_step2);

        gvMain.setTurnListener(this);
        gvMain.setQuickTouchListener(this);
        gvMain.setColorPalette(Tools.getDiskColors(getContext(), -1));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.init(getActivity());

        viewModel.getEvent().observe(requireActivity(), action -> {
            if (action.isRestart())
                gvMain.launchGame(viewModel.getAllGames().get(0));
        });
    }

    @Override
    public void onResume() {
        gvMain.setTouchMode(PrefHelper.ReadString(getContext(), PrefHelper.KEY_MOUVEMENT, "swipe"));
        gvMain.setColorPalette(Tools.getDiskColors(getContext(), PrefHelper.ReadInt(getContext(), PrefHelper.KEY_THEME_DISK_INDEX, 0)));

        QuickTouch savedQt = PrefHelper.GetQtPosition(getContext(), getActivity().getResources().getConfiguration().orientation);
        if (savedQt != null)
            gvMain.setQt(savedQt);

        gvMain.launchGame(viewModel.getAllGames().get(0));

        super.onResume();
    }

    private boolean skipSave = false;

    @Override
    public void onPause() {
        super.onPause();

        if (!skipSave) {
            viewModel.getAllGames().set(viewModel.getCurrentGameIndex(), gvMain.saveGameAsString());
            DataManager.SaveAllGames(viewModel.getAllGames(), getActivity().getApplicationContext());
        }

        skipSave = false;
    }

    //#region Menus

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();

        menuItemSmallUndo = menu.add(0, MENU_UNDO, 0, R.string.undo);
        menuItemSmallUndo.setIcon(R.drawable.ic_action_undo);
        menuItemSmallUndo.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menuItemSmallTouchEnable = menu.add(0, MENU_QUICK_TOUCH_ENABLE, 0, R.string.quick_touch_enable);
        menuItemSmallTouchEnable.setIcon(R.drawable.ic_action_smalltouch);
        menuItemSmallTouchEnable.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menuItemSmallTouchModify = menu.add(0, MENU_QUICK_TOUCH_MODIFY, 0, R.string.quick_touch_update);
        menuItemSmallTouchModify.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menuItemSmallTouchRemove = menu.add(0, MENU_QUICK_TOUCH_REMOVE, 0, R.string.quick_touch_delete);
        menuItemSmallTouchRemove.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        menu.findItem(MENU_QUICK_TOUCH_REMOVE).setVisible((gvMain != null && gvMain.getQt() != null));
        menu.findItem(MENU_QUICK_TOUCH_MODIFY).setVisible((gvMain != null && gvMain.getQt() != null));
        menu.findItem(MENU_QUICK_TOUCH_ENABLE).setVisible((gvMain == null || gvMain.getQt() == null));
        menu.findItem(MENU_UNDO).setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case MENU_QUICK_TOUCH_ENABLE:
                gvMain.activateQuickTouchMode();

                menuItemSmallTouchEnable.setVisible(false);

                initHelpPopup(true);

                return true;
            case MENU_QUICK_TOUCH_MODIFY:
                gvMain.enterEditMode();
                break;
            case MENU_QUICK_TOUCH_REMOVE:
                gvMain.activateQuickTouchMode();
                cleanHelpPopup();
                PrefHelper.ClearQtPosition(getContext(), getActivity().getResources().getConfiguration().orientation);

                menuItemSmallTouchRemove.setVisible(false);
                menuItemSmallTouchModify.setVisible(false);
                menuItemSmallTouchEnable.setVisible(true);
                break;
            case MENU_UNDO:
                gvMain.undo();
                if (menuItemSmallUndo != null)
                    menuItemSmallUndo.setVisible(false);
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    //#endregion

    //#region Listeners

    @Override
    public void turnPlayed(int nbCoup, int nbTotal) {

        ((MainActivity) getActivity()).updateMainTitle(nbCoup + " / " + nbTotal);
        if (menuItemSmallUndo != null)
            menuItemSmallUndo.setVisible(nbCoup > 0 && !gvMain.isFinished());
    }

    @Override
    public void gameFinished(int nbCoup, int nbTotal, long miliseconds) {

        ArrayList<String> savedGames = viewModel.getAllGames();
        savedGames.set(viewModel.getCurrentGameIndex(), gvMain.saveGameAsString());

        String newGame = GameView.getNewSaveData(getContext(), PrefHelper.ReadInt(getContext(), PrefHelper.KEY_DISK_COUNT, -1));
        viewModel.getAllGames().add(0, newGame);

        DataManager.SaveAllGames(viewModel.getAllGames(), getActivity().getApplicationContext());

        skipSave = true;

        Intent i = new Intent(getActivity(), CongratsActivity.class);
        Bundle b = new Bundle();
        b.putInt("userMovements", nbCoup);
        b.putInt("totalMovements", nbTotal);
        b.putLong("time", miliseconds);
        i.putExtras(b);

        congratsLauncher.launch(i);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void quickTouchConstructed(QuickTouch _qt) {

        menuItemSmallTouchRemove.setVisible(true);
        menuItemSmallTouchModify.setVisible(true);

        this.quickTouchUpdated(_qt);
    }

    @Override
    public void quickTouchUpdated(QuickTouch _qt) {
        PrefHelper.SaveQtPosition(getContext(), getActivity().getResources().getConfiguration().orientation, _qt);
    }

    //#endregion

    //#region Popup d'aide Zone Rapide

    private void cleanHelpPopup() {
        stepContainer.setVisibility(View.GONE);
        step0.setVisibility(View.GONE);
        step1.setVisibility(View.GONE);
        step2.setVisibility(View.GONE);
    }

    private void initHelpPopup(boolean show) {
        wantstoShowHelp = false;
        if (show) {
            if (!DataManager.GetMemorizedValueBoolean("helpCompleted", getContext())) {

                stepContainer.setVisibility(View.VISIBLE);
                step0.setVisibility(View.VISIBLE);
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                /*
                float dpWidth = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
                if (dpWidth > 400)
                    stepContainer.getLayoutParams().width = Tools.convertDpToPixel(300);
                else
                    findViewById(R.id.rl_help).getLayoutParams().width = -1;
                */
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
                alphaAnimator.setDuration(500);
                alphaAnimator.start();

                step0.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        wantstoShowHelp = true;
                        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
                        alphaAnimator.setDuration(500);
                        alphaAnimator.addListener(new Animator.AnimatorListener() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                step0.setVisibility(View.GONE);
                                step1.setVisibility(View.VISIBLE);
                                step2.setVisibility(View.GONE);
                                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
                                alphaAnimator.setDuration(500);
                                alphaAnimator.start();
                                gvMain.setDrawHelpLine(true);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }
                        });
                        alphaAnimator.start();

                    }
                });

                gvMain.setHelpListener(new HelpListener() {

                    @Override
                    public void stepPassed(int step) {

                        if (step == 0 && wantstoShowHelp) {
                            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
                            alphaAnimator.setDuration(500);
                            alphaAnimator.addListener(new Animator.AnimatorListener() {

                                @Override
                                public void onAnimationStart(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    step0.setVisibility(View.GONE);
                                    step1.setVisibility(View.GONE);
                                    step2.setVisibility(View.VISIBLE);
                                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
                                    alphaAnimator.setDuration(500);
                                    alphaAnimator.start();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }
                            });
                            alphaAnimator.start();
                        } else {
                            if (wantstoShowHelp) {
                                DataManager.MemorizeValue("helpCompleted", true, getContext());
                            }

                            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
                            alphaAnimator.setDuration(500);
                            alphaAnimator.addListener(new Animator.AnimatorListener() {

                                @Override
                                public void onAnimationStart(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    step0.setVisibility(View.GONE);
                                    step1.setVisibility(View.GONE);
                                    step2.setVisibility(View.GONE);
                                    stepContainer.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }
                            });
                            alphaAnimator.start();
                        }
                    }

                });
            }
        } else {
            stepContainer.setVisibility(View.GONE);
        }

    }

    //#endregion
}