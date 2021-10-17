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
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.text.TextUtils;
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
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.mathis.tourhanoipro.ui.picker.NumberPickerDialog;
import fr.mathis.tourhanoipro.core.tools.PrefHelper;
import fr.mathis.tourhanoipro.core.tools.Tools;


public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {

    private int RC_SIGN_IN = 1001;
    private int RC_ACHIVEMENT_UI = 1002;
    private int RC_LEADERBOARD_UI = 1002;

    private AppBarConfiguration mAppBarConfiguration;

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private PlayersClient mPlayersClient;

    private DrawerLayout mDrawer;
    private LinearLayout llDrawer;
    private ImageView ivDrawerLogo;
    private TextView tvDrawerTitle;
    private TextView tvDrawerSubtitle;

    private MenuItem miConnect;
    private MenuItem miLeaderboard;
    private MenuItem miAchievements;

    private int mDiskCount = 5;

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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
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
                    }
                });

                numberPicker.show(getSupportFragmentManager(), "tag");
            }
        });


        mDiskCount = PrefHelper.ReadInt(this, PrefHelper.KEY_DISK_COUNT, 5);
        tvDiskNumber.setText(mDiskCount + "");

        navController.addOnDestinationChangedListener(this);
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

        if (mTryReconnectAutomatically)
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

    //#region Tooltip title

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

}
