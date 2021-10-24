package fr.mathis.tourhanoipro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.core.tools.DataManager;
import fr.mathis.tourhanoipro.ui.home.GameAction;
import fr.mathis.tourhanoipro.ui.home.HomeViewModel;
import fr.mathis.tourhanoipro.ui.picker.NumberPickerDialog;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.view.game.GameView;
import fr.mathis.tourhanoipro.view.game.listener.TurnListener;


public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {

    private int RC_SIGN_IN = 1001;
    private int RC_ACHIVEMENT_UI = 1002;
    private int RC_LEADERBOARD_UI = 1003;
    static final int RESULT_TUTORIAL = 1005;


    private AppBarConfiguration mAppBarConfiguration;

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private PlayersClient mPlayersClient;

    private DrawerLayout mDrawer;
    private LinearLayout llDrawer;
    private ImageView ivDrawerLogo;
    private TextView tvDrawerTitle;
    private TextView tvDrawerSubtitle;
    NavController navController;

    private MenuItem miConnect;
    private MenuItem miLeaderboard;
    private MenuItem miAchievements;

    private int mDiskCount = 5;
    private HomeViewModel viewModel;

    boolean tutorialShownWaitForConnectionAfterResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tools.applyColoredTheme(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        updateDrawerLockMode();

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.init(this);

        llDrawer = (LinearLayout) navigationView.getHeaderView(0);
        ivDrawerLogo = llDrawer.findViewById(R.id.ivDrawerLogo);
        tvDrawerTitle = llDrawer.findViewById(R.id.tvDrawerTitle);
        tvDrawerSubtitle = llDrawer.findViewById(R.id.tvDrawerSubtitle);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(mDrawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        miConnect = navigationView.getMenu().findItem(R.id.nav_connect);
        miLeaderboard = navigationView.getMenu().findItem(R.id.nav_leaderboard);
        miAchievements = navigationView.getMenu().findItem(R.id.nav_achivements);

        FrameLayout flDiskNumber = (FrameLayout) navigationView.getMenu().findItem(R.id.nav_home).getActionView();
        final TextView tvDiskNumber = (TextView) flDiskNumber.findViewById(R.id.tvDiskNumber);

        flDiskNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerDialog numberPicker = new NumberPickerDialog(mDiskCount);
                numberPicker.setValueChangeListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        PrefHelper.SaveInt(MainActivity.this, PrefHelper.KEY_DISK_COUNT, newVal);
                        mDiskCount = newVal;
                        tvDiskNumber.setText(mDiskCount + "");

                        startNewGame();
                    }
                });

                numberPicker.show(getSupportFragmentManager(), "tag");
            }
        });

        mDiskCount = PrefHelper.ReadInt(this, PrefHelper.KEY_DISK_COUNT, 5);
        tvDiskNumber.setText(mDiskCount + "");

        navController.addOnDestinationChangedListener(this);

        if (!DataManager.GetMemorizedValueBoolean("showTutoFirstTime2", getApplicationContext())) {
            tutorialShownWaitForConnectionAfterResume = true;
            DataManager.MemorizeValue("showTutoFirstTime2", true, getApplicationContext());
            showTutorial(false);
        }
    }

    public void drawerTutoClick(MenuItem menuItem) {
        showTutorial(true);
        DataManager.MemorizeValue("helpCompleted", false, getApplicationContext());
    }

    /*
     * Méthode appelé sur le clic de Nouvelle partie dans le Drawer
     */
    public void drawerHomeClick(MenuItem menuItem) {
        startNewGame();
    }

    private void startNewGame() {

        // Génère la nouvelle partie
        ArrayList<String> savedGames = viewModel.getAllGames();
        savedGames.add(0, GameView.getNewSaveData(getApplicationContext(), mDiskCount));
        DataManager.SaveAllGames(savedGames, getApplicationContext());

        // Puis gère son affichage
        NavDestination n = navController.getCurrentDestination();

        if(n.getId() != R.id.nav_home)
            navController.navigateUp();
        else
            viewModel.sendEvent(new GameAction(true));

        mDrawer.closeDrawers();
    }

    /*
     * Méthode appelé sur le clic de Succès dans le drawer.
     * Ouvre l'écran des succès
     * */
    public void drawerAchivementsClick(MenuItem menuItem) {

        if (mAchievementsClient != null) {
            mAchievementsClient
                    .getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_ACHIVEMENT_UI);
                        }
                    });
        }
    }

    /*
     * Méthode appelé sur le clic de Classements dans le drawer.
     * Ouvre l'écran des classements
     * */
    public void drawerLeaderboardsClick(MenuItem menuItem) {
        if (mLeaderboardsClient != null) {
            mLeaderboardsClient
                    .getAllLeaderboardsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    });
        }
    }

    /*
     * Méthode appelé sur le clic de Se connecter dans le drawer
     */
    public void drawerConnectClick(MenuItem menuItem) {

        signInSilently();
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestSignIn();
    }

    private void requestSignIn() {
        if (mTryReconnectAutomatically && !tutorialShownWaitForConnectionAfterResume)
            signInSilently();
        else
            onDisconnected();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //region Sign In & Out

    private boolean mPlayerInfoLoaded;

    private void onConnected(GoogleSignInAccount _signedInAccount) {

        miConnect.setVisible(false);
        miLeaderboard.setVisible(true);
        miAchievements.setVisible(true);

        PlayersClient d = Games.getPlayersClient(this, _signedInAccount);

        GamesClient mGamesClient = Games.getGamesClient(this, _signedInAccount);
        mGamesClient.setViewForPopups(findViewById(android.R.id.content));
        mGamesClient.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        mAchievementsClient = Games.getAchievementsClient(this, _signedInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, _signedInAccount);
        mPlayersClient = Games.getPlayersClient(this, _signedInAccount);

        d.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        if (task.isSuccessful() && !mPlayerInfoLoaded) {

                            Player player = task.getResult();

                            tvDrawerTitle.setText(player.getDisplayName());
                            tvDrawerSubtitle.setText(player.getTitle());

                            Uri uri = player.getIconImageUri();
                            if (uri != null)
                                ImageManager.create(MainActivity.this).loadImage(ivDrawerLogo, uri);

                            mPlayerInfoLoaded = true;

                            miConnect.setVisible(false);
                            miLeaderboard.setVisible(true);
                            miAchievements.setVisible(true);
                        } else {
                            Exception e = task.getException();
                        }
                    }
                });
    }

    private void onDisconnected() {
        miConnect.setVisible(true);
        miLeaderboard.setVisible(false);
        miAchievements.setVisible(false);

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
    }

    private void signInSilently() {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {

            GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
                // Already signed in.
                // The signed in account is stored in the 'account' variable.
                GoogleSignInAccount signedInAccount = account;
                onConnected(signedInAccount);
            } else {
                // Haven't been signed-in before. Try the silent sign-in first.
                GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);



                signInClient
                        .silentSignIn()
                        .addOnCompleteListener(
                                this,
                                new OnCompleteListener<GoogleSignInAccount>() {
                                    @Override
                                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                        if (task.isSuccessful()) {
                                            // The signed in account is stored in the task's result.
                                            GoogleSignInAccount signedInAccount = task.getResult();
                                            onConnected(signedInAccount);
                                        } else {
                                            // Player will need to sign-in explicitly using via UI.
                                            // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                                            // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                                            // Interactive Sign-in.
                                            startSignInIntent();
                                        }
                                    }
                                });
            }
        } else {
            onDisconnected();
        }
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private static boolean mTryReconnectAutomatically = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                onConnected(signedInAccount);
            } else {
                mTryReconnectAutomatically = false;
                onDisconnected();

                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.play_games_connection_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();

            }
        }
        else if(requestCode == RESULT_TUTORIAL) {

                if (tutorialShownWaitForConnectionAfterResume) {
                    tutorialShownWaitForConnectionAfterResume = false;
                    requestSignIn();
                }

        }
    }

    public void onGameFinished(int nbCoup, int nbTotal, long miliseconds)
    {
        if (mPlayersClient != null) {
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
            }

            if (nbTotal == nbCoup) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_perfect_score), (int) (Math.log(nbCoup + 1) / Math.log(2)));
            }
        }



    }

    //endregion

    //#region Prefs

    public void updateDrawerLockMode() {
        if (PrefHelper.ReadBool(this, PrefHelper.KEY_DRAWER_LOCKED, false))
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    //#endregion

    //#region Toolbar title

    private String currentMainTitle = null;

    public void updateMainTitle(String title) {
        this.currentMainTitle = title;

        setTitle(this.currentMainTitle);
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if(destination.getId() == R.id.nav_home)
        {
            if(TextUtils.isEmpty(this.currentMainTitle))
                setTitle(R.string.app_name);
            else
                setTitle(this.currentMainTitle);
        }
        else if(destination.getId() == R.id.nav_settings)
        {
            setTitle(R.string.menu_settings);
        }
    }

    //#endregion

    //#region Tuto

    public void showTutorial(boolean animate) {
        Intent i = new Intent(MainActivity.this, TutoActivity.class);
        i.putExtra("connectClient", animate && mPlayersClient != null);
        startActivityForResult(i, RESULT_TUTORIAL);
        if (!animate)
            overridePendingTransition(0, 0);

        mDrawer.closeDrawers();
    }

    //endregion


}
