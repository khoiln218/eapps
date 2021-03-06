package vn.gomisellers.apps.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import vn.gomisellers.apps.BaseActivity;
import vn.gomisellers.apps.EappsApplication;
import vn.gomisellers.apps.R;
import vn.gomisellers.apps.databinding.ActivityMainBinding;
import vn.gomisellers.apps.event.OnClickListener;
import vn.gomisellers.apps.main.home.HomeFragment;
import vn.gomisellers.apps.main.live.LiveFragment;
import vn.gomisellers.apps.main.live.video.LiveActivity;
import vn.gomisellers.apps.main.market.MarketFragment;
import vn.gomisellers.apps.main.mypage.MyPageFragment;
import vn.gomisellers.apps.main.notification.NotificationFragment;
import vn.gomisellers.apps.utils.AlertDialogs;
import vn.gomisellers.apps.utils.Intents;
import vn.gomisellers.apps.utils.LogUtils;
import vn.gomisellers.apps.utils.MediaHelper;
import vn.gomisellers.apps.utils.PermissionHelper;
import vn.gomisellers.apps.utils.ToastUtils;
import vn.gomisellers.apps.widgets.dialog.ImageChooserDialogFragment;

public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final int SHOP_INDEX = 0;
    public static final int MALL_INDEX = SHOP_INDEX + 1;
    public static final int LIVE_INDEX = MALL_INDEX + 1;
    public static final int NOTIFY_INDEX = LIVE_INDEX + 1;
    public static final int ACCOUNT_INDEX = NOTIFY_INDEX + 1;

    private PermissionHelper permissionHelper;
    private boolean dialogShowing = false;

    private boolean isExit = false;
    private Handler handler;
    private Runnable exitApp;
    private View badge;

    private HomeFragment homeFragment;
    private MarketFragment marketFragment;
    private LiveFragment liveFragment;
    private NotificationFragment notificationFragment;
    private MyPageFragment myPageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
        initCmd();

        if (!EappsApplication.getPreferences().isLogin()) {
            LogUtils.d("TAG", "user isn't login");
            Intents.startLoginActivity(this);
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        BottomNavigationView bottomNavigation = findViewById(R.id.navigation_main);

        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        View v = bottomNavigationMenuView.getChildAt(NOTIFY_INDEX);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;
        badge = LayoutInflater.from(this).inflate(R.layout.layout_unread_message, itemView, false);
        itemView.addView(badge);

        bottomNavigation.setOnNavigationItemSelectedListener(this);

        permissionHelper = new PermissionHelper(this, PermissionHelper.photo_permissions);

        handler = new Handler();
        exitApp = new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        };

        homeFragment = new HomeFragment();
        marketFragment = new MarketFragment();
        liveFragment = new LiveFragment();
        notificationFragment = new NotificationFragment();
        myPageFragment = new MyPageFragment();

        loadFragment(homeFragment);

        EventBus.getDefault().register(this);
    }

    private void initCmd() {
        getViewModel().getCmd().observe(this, new Observer<MainEvent>() {
            @Override
            public void onChanged(MainEvent event) {
                if (event.getCode() == MainEvent.NOTIFY) {
                    int count = (int) event.getData();
                    updateNotificationBadges(count);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getViewModel().requestNotificationBadges();
    }

    @Override
    protected void initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setLifecycleOwner(this);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                loadFragment(homeFragment);
                return true;

            case R.id.nav_market:
                loadFragment(marketFragment);
                return true;

            case R.id.nav_live:
                loadFragment(liveFragment);
                return true;

            case R.id.nav_notify:
                loadFragment(notificationFragment);
                return true;

            case R.id.nav_account:
                loadFragment(myPageFragment);
                return true;

            default:
                return false;
        }
    }

    @Subscribe
    public void onMessageEvent(MainEvent event) {
        switch (event.getCode()) {
            case MainEvent.REQUEST_PERMISSION:
            case MainEvent.REQUEST_PERMISSION_LIVE:
                requestPermission(event.getCode());
                break;
            case MainEvent.CROP_IMAGE:
                cropImage((Uri) event.getData());
                break;
            case MainEvent.NOTIFY:
                getViewModel().requestNotificationBadges();
                break;
        }
    }

    private void updateNotificationBadges(int count) {
        TextView tv = badge.findViewById(R.id.notification_badge);
        tv.setText(count > 99 ? "99+" : String.valueOf(count));
        tv.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (isExit) {
            handler.removeCallbacks(exitApp);
            finish();
        } else {
            isExit = true;
            ToastUtils.showToast(getString(R.string.exit_app));
            handler.removeCallbacks(exitApp);
            handler.postDelayed(exitApp, 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null)
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void cropImage(Uri uri) {
        CropImage.activity(uri)
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                .setAutoZoomEnabled(true)
                .setShowCropOverlay(true)
                .start(this);
    }

    /**
     * Pick & Crop Image
     */
    private void showImageOptions() {

        String title = getString(R.string.cover);

        ImageChooserDialogFragment fragment = ImageChooserDialogFragment.instance(title, false);
        fragment.show(getSupportFragmentManager(), fragment.getTag());
        fragment.setClickListener(new OnClickListener() {
            @Override
            public void onTakePhoto() {
                try {
                    File file = MediaHelper.createImageFile();
                    Uri imageUri = MediaHelper.uriFromFile(file);
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
                    if (fragment instanceof MyPageFragment)
                        ((MyPageFragment) fragment).setAvatar(imageUri);
                    MediaHelper.dispatchTakePictureIntent(MainActivity.this, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChooseImage() {
                MediaHelper.dispatchPickPictureIntent(MainActivity.this);
            }

            @Override
            public void onViewPhoto() {
            }

            @Override
            public void onRemovePhoto() {
            }
        });
    }

    /**
     * Check Photo Permissions
     */
    public void requestPermission(final int type) {
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                if (type == MainEvent.REQUEST_PERMISSION) {
                    showImageOptions();
                } else if (type == MainEvent.REQUEST_PERMISSION_LIVE) {
                    startBroadcast();
                }
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
                showPermissionDialog();
            }

            @Override
            public void onPermissionDenied() {
                showPermissionDialog();
            }

            @Override
            public void onPermissionDeniedBySystem() {
                showPermissionDialog();
            }
        });
    }

    private void startBroadcast() {
        startActivity(new Intent(this, LiveActivity.class));
    }

    private void showPermissionDialog() {
        if (!dialogShowing) {
            dialogShowing = true;

            AlertDialogs.show(this, String.format(getString(R.string.title_need_camera_permission), getString(R.string.app_name)), String.format(getString(R.string.msg_need_camera_permission), getString(R.string.app_name)), getString(R.string.btn_cancel), getString(R.string.btn_setting), new AlertDialogs.OnClickListener() {
                @Override
                public void onNegativeButtonClick(DialogInterface dialog, int which) {
                    dialogShowing = false;
                    dialog.dismiss();
                }

                @Override
                public void onPositiveButtonClick(DialogInterface dialog, int which) {
                    dialogShowing = false;
                    permissionHelper.launchAppDetailsSettings();
                    dialog.dismiss();
                }
            });
        }
    }
}
