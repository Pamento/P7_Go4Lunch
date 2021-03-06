package com.pawel.p7_go4lunch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding;
import com.pawel.p7_go4lunch.databinding.ContentScrollingBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.service.AlarmService;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.GlideApp;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.AboutRestaurantViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AboutRestaurantActivity extends AppCompatActivity implements WorkmateAdapter.OnItemClickListener {

    // view
    private AboutRestaurantViewModel mAboutRestaurantVM;
    private View view;
    private ActivityAboutRestaurantBinding mBinding;
    private ContentScrollingBinding mScrolBinding;
    private com.pawel.p7_go4lunch.databinding.WifiOffBinding mWifiOffBinding;
    // data
    private User mUser;
    private Restaurant mThisRestaurant;
    private String restaurantId;
    private List<String> favoritesResto = new ArrayList<>();
    private boolean isChosen;
    private boolean isLiked;
    private Drawable ic_Like;
    private Drawable ic_notLike;
    private Drawable ic_rest_chosen;
    private Drawable ic_rest_not_chosen;
    private Drawable ic_full_star;
    private Drawable ic_empty_star;
    private LocalAppSettings mAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAboutRestaurantViewModel();
        mBinding = com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding
                .inflate(getLayoutInflater());
        mWifiOffBinding = mBinding.abInclude.aboutWifiOff;
        view = mBinding.getRoot();
        bindIncludesLayouts();
        setContentView(view);
        setSupportActionBar(mBinding.aboutTheRestaurantToolbar);
        mBinding.toolbarLayout.setTitle(getTitle());
        // windowTranslucentStatus for KITKAT android version
        if (Build.VERSION.SDK_INT >= 21) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        getLocalAppSettings();
        getDrawable();
        getUser();
    }

    private void bindIncludesLayouts() {
        View scrollContent = mBinding.abInclude.getRoot();
        mScrolBinding = ContentScrollingBinding.bind(scrollContent);
    }

    private void initAboutRestaurantViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mAboutRestaurantVM = new ViewModelProvider(this, vmf).get(AboutRestaurantViewModel.class);
        mAboutRestaurantVM.init();
    }

    private void getDrawable() {
        ic_Like = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_black_yellow_36, null);
        ic_notLike = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_primary_36, null);
        ic_rest_chosen = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle_32, null);
        ic_rest_not_chosen = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle_red_32, null);
        ic_full_star = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_star_24, null);
        ic_empty_star = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_star_border_24, null);
    }

    private void getUser() {
        mAboutRestaurantVM.getUser().observe(this, user -> {
            if (user != null) {
                mUser = user;
                if (user.getFavoritesRestaurants() != null)
                    favoritesResto = user.getFavoritesRestaurants();
                if (isCalledFromGoogleMap()) getRestaurantFromGoogleMap();
                else getRestaurantFromUser();
            }
        });
    }

    // set "Like" star according to choice of user, if it's liked or not it has different star
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isCalledFromGoogleMap() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return false;
        else restaurantId = extras.getString(Const.EXTRA_KEY_RESTAURANT);
        return true;
    }

    private void getRestaurantFromGoogleMap() {
        mThisRestaurant = mAboutRestaurantVM.getRestoSelected(restaurantId);
        isChosen = mUser.getUserRestaurant() != null
                && mUser.getUserRestaurant().getPlaceId().equals(mThisRestaurant.getPlaceId())
                && isSetAlarmRemainder();
        updateUI();
    }

    private void getRestaurantFromUser() {
        if (mUser != null) {
            if (mUser.getUserRestaurant() != null) {
                mThisRestaurant = mUser.getUserRestaurant();
                restaurantId = mUser.getUserRestaurant().getPlaceId();
                isChosen = isChosen();
                updateUI();
            } else {
                mScrolBinding.aboutTheRestName.setText(R.string.no_restaurant_data);
                mScrolBinding.aboutTheRestName.setText(R.string.no_restaurant_data);
                mScrolBinding.aboutTheRestTxCall.setAlpha(0.3f);
                mScrolBinding.aboutTheRestTxLike.setAlpha(0.3f);
                mScrolBinding.aboutTheRestTxWebsite.setAlpha(0.3f);
                mBinding.aboutRestaurantFab.setVisibility(View.GONE);
            }
        }
    }

    private boolean isSetAlarmRemainder() {
        PendingIntent alarmUp = AlarmService.isAlarmSet();
        return alarmUp != null;
    }

    private boolean isChosen() {
        return isSetAlarmRemainder() && (mAppSettings.isNotif_recurrence() || mAppSettings.isNotification());
    }

    private void updateUI() {
        setUpUiUser();
        setRestaurantImage();
        setUiAboutRestaurant();
        setRecyclerViewWorkmates();
        setOnClickListeners();
    }

    private void setUpUiUser() {
        if (mUser != null) {
            checkIfThisRestaurantIsFavorite();
            updateIconChoiceFAB();
        }
    }

    private void setRestaurantImage() {
        if (mThisRestaurant != null) {
            GlideApp.with(this)
                    .load(mThisRestaurant.getImage())
                    .error(R.drawable.restaurant_default)
                    .placeholder(R.drawable.restaurant_default)
                    .into(mBinding.aboutTheRestImg);
        }
    }

    private void setUiAboutRestaurant() {
        if (mThisRestaurant != null) {
            // name
            if (mThisRestaurant.getName().isEmpty()) {
                mScrolBinding.aboutTheRestName.setText(getString(R.string.name_restaurant_absent));
            } else {
                mScrolBinding.aboutTheRestName.setText(mThisRestaurant.getName());
            }
            // address
            if (mThisRestaurant.getAddress().isEmpty()) {
                mScrolBinding.aboutTheRestAddress.setText(getString(R.string.address_restaurant_absent));
            } else {
                mScrolBinding.aboutTheRestAddress.setText(mThisRestaurant.getAddress());
            }
            // rating
//            double starsRange = Math.round(mThisRestaurant.getRating() * 3 / 5);
            double starsRange = mThisRestaurant.getRating();
            switch ((int) starsRange) {
                case 1:
                    mScrolBinding.aboutTheRestStar1.setImageDrawable(ic_full_star);
                    mScrolBinding.aboutTheRestStar2.setImageDrawable(ic_empty_star);
                    mScrolBinding.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
                case 2:
                    mScrolBinding.aboutTheRestStar1.setImageDrawable(ic_full_star);
                    mScrolBinding.aboutTheRestStar2.setImageDrawable(ic_full_star);
                    mScrolBinding.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
                case 3:
                    mScrolBinding.aboutTheRestStar1.setImageDrawable(ic_full_star);
                    mScrolBinding.aboutTheRestStar2.setImageDrawable(ic_full_star);
                    mScrolBinding.aboutTheRestStar3.setImageDrawable(ic_full_star);
                    break;
                default:
                    mScrolBinding.aboutTheRestStar1.setImageDrawable(ic_empty_star);
                    mScrolBinding.aboutTheRestStar2.setImageDrawable(ic_empty_star);
                    mScrolBinding.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
            }
        }
    }

    private void setRecyclerViewWorkmates() {
        if (!LocationUtils.isNetworkAvailable()) mWifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
        else {
            mAboutRestaurantVM.getUsersWithTheSameRestaurant(restaurantId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isEmpty()) {
                    mScrolBinding.aboutTheRestWorkmatesListEmpty.setVisibility(View.VISIBLE);
                }
            });
            FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                    .setQuery(mAboutRestaurantVM.getUsersWithTheSameRestaurant(restaurantId), User.class)
                    .setLifecycleOwner(this)
                    .build();
            WorkmateAdapter workmateAdapter = new WorkmateAdapter(options, this, 2);
            mScrolBinding.aboutTheRestRecyclerView.setAdapter(workmateAdapter);
            mScrolBinding.aboutTheRestRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        }
    }

    // Workmates list onClickListener on RecyclerView item
    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {
        // Go to chat with your workmate ...
    }

    private void checkIfThisRestaurantIsFavorite() {
        isLiked = false;
        if (favoritesResto != null) {
            for (int i = 0; i < favoritesResto.size(); i++) {
                if (favoritesResto.get(i).equals(mThisRestaurant.getPlaceId())) {
                    isLiked = true;
                }
            }
        }
        updateIconLike();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setOnClickListeners() {
        if (mThisRestaurant != null) {
            // Call Restaurant
            if (mThisRestaurant.getPhoneNumber() != null)
                mScrolBinding.aboutTheRestTxCall.setOnClickListener(v -> makePhoneCall());
            else mScrolBinding.aboutTheRestTxCall.setAlpha(0.3f);
            // Like Restaurant
            mScrolBinding.aboutTheRestTxLike.setOnClickListener(v -> isLiked());
            // Go to Website of Restaurant
            if (mThisRestaurant.getWebsite() != null)
                mScrolBinding.aboutTheRestTxWebsite.setOnClickListener(v -> visitWebsite());
            else mScrolBinding.aboutTheRestTxWebsite.setAlpha(0.3f);
            // manage choice of restaurant for lunch
            mBinding.aboutRestaurantFab.setOnClickListener(view -> choseThisRestaurant());
        }
    }

    // ......................................................... Utils functions
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void isLiked() {
        isLiked = !isLiked;
        updateIconLike();
        if (isLiked && !favoritesResto.contains(restaurantId)) {
            favoritesResto.add(mThisRestaurant.getPlaceId());
        } else if (!isLiked && favoritesResto.contains(restaurantId)) {
            favoritesResto.remove(mThisRestaurant.getPlaceId());
        }
    }

    private void updateIconLike() {
        mScrolBinding.aboutTheRestTxLike
                .setCompoundDrawablesRelativeWithIntrinsicBounds(null, isLiked ? ic_Like : ic_notLike, null, null);
    }

    private void updateIconChoiceFAB() {
        mBinding.aboutRestaurantFab.setImageDrawable(isChosen ? ic_rest_chosen : ic_rest_not_chosen);
    }

    private void choseThisRestaurant() {
        isChosen = !isChosen;
        if (isChosen) {
            mUser.setUserRestaurant(mThisRestaurant);
        } else {
            mUser.setUserRestaurant(null);
        }
        updateIconChoiceFAB();
        showMessageBookingRestaurant(isChosen);
    }

    private void makePhoneCall() {
        String number = mThisRestaurant.getPhoneNumber();
        if ((number != null) && (number.trim().length() > 0)) {
            Permissions.check(this, Manifest.permission.CALL_PHONE, null, new PermissionHandler() {
                @SuppressLint("MissingPermission")
                @Override
                public void onGranted() {
                    String dial = "tel:" + number;
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
                    if (intent.resolveActivity(Objects.requireNonNull(AboutRestaurantActivity.this).getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        } else {
            showMessagePhoneNumberInvalid();
        }
    }

    private void visitWebsite() {
        String url = null;
        if (mThisRestaurant != null) url = mThisRestaurant.getWebsite();
        if ((url != null) && !url.contains("google.com")) {
            if (!url.startsWith("https://") || !url.startsWith("http://")) {
                url = "http://" + url;
            }
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(Objects.requireNonNull(AboutRestaurantActivity.this).getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showMessageHttpInvalid();
            }
        } else {
            showMessageHttpInvalid();
        }
    }

    private void showMessageHttpInvalid() {
        ViewWidgets.showSnackBar(0, view, getString(R.string.invalid_http_address));
    }

    private void showMessagePhoneNumberInvalid() {
        ViewWidgets.showSnackBar(0, view, getString(R.string.invalid_phone_number));
    }

    private void showMessageBookingRestaurant(boolean mode) {
        ViewWidgets.showSnackBar(0, view, !mode ?
                getString(R.string.cancel_choice_restaurant)
                : getString(R.string.save_choice_restaurant));
    }

    private void setRemainderOnce(String h) {
        if (!mAppSettings.isNotification()) {
            ViewWidgets.showSnackBar(1,view,getString(R.string.notification_disabled_warning));
        }
        AlarmService.startAlarm(h);
    }

    private void setMultiRemainder(String h) {
        AlarmService.startRepeatedAlarm(h);
    }

    private void cancelAlarm() {
        AlarmService.cancelAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocalAppSettings();
    }

    private void getLocalAppSettings() {
        mAppSettings = new LocalAppSettings(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUser != null && mThisRestaurant != null) {
            if (isChosen) {
                mThisRestaurant.setDateCreated(new Date());
                if (!mAppSettings.isNotification()) mAppSettings.setNotification(true);
                setRemainder();
                mAboutRestaurantVM.updateUserRestaurant(mUser.getUid(), mThisRestaurant);
            }
            mAboutRestaurantVM.updateUserFavoriteRestaurantsList(mUser.getUid(), favoritesResto);
        }
    }

    private void setRemainder() {
        // if the alarm was set delete it for give the place for next one
        cancelAlarm();
        if (mAppSettings.isNotif_recurrence()) setMultiRemainder(mAppSettings.getHour());
        else setRemainderOnce(mAppSettings.getHour());
    }
}