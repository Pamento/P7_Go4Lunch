package com.pawel.p7_go4lunch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.GlideApp;
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

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AboutRestaurantActivity extends AppCompatActivity implements WorkmateAdapter.OnItemClickListener {

    private static final String TAG = "workmate";

    // view
    private AboutRestaurantViewModel mAboutRestaurantVM;
    private View view;
    private ActivityAboutRestaurantBinding mBinding;
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
    private Drawable ic_empty_star;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAboutRestaurantViewModel();
        mBinding = com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding
                .inflate(getLayoutInflater());
        view = mBinding.getRoot();
        setContentView(view);
        setSupportActionBar(mBinding.aboutTheRestaurantToolbar);
        mBinding.toolbarLayout.setTitle(getTitle());
        // windowTranslucentStatus for KITKAT android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        getDrawable();
        getUser();
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
        ic_empty_star = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_star_border_24, null);
    }

    private void getUser() {
        mAboutRestaurantVM.getUser().observe(this, user -> {
            mUser = user;
            if (user != null) Log.i(TAG, "getUser: user: " + user.toString());
            favoritesResto = user.getFavoritesRestaurants();
            if (isCalledFromGoogleMap()) getRestaurantFromGoogleMap();
            else getRestaurantFromUser();
            if (favoritesResto != null)
                Log.i(TAG, "getUser: favoriteResto s::: " + favoritesResto.size());
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
        mAboutRestaurantVM.getRestaurant(restaurantId).observe(this, restaurant -> {
            mThisRestaurant = restaurant;
            isChosen = mUser.getUserRestaurant() != null && mUser.getUserRestaurant().getPlaceId().equals(mThisRestaurant.getPlaceId());
            updateUI();
        });
    }

    private void getRestaurantFromUser() {
        if (mUser != null && mUser.getUserRestaurant() != null) {
            mThisRestaurant = mUser.getUserRestaurant();
            restaurantId = mUser.getUserRestaurant().getPlaceId();
            isChosen = true;
            updateUI();
        } else if (mUser != null && mUser.getUserRestaurant() == null) {
            mBinding.abInclude.aboutTheRestName.setText(R.string.no_restaurant_data);
            mBinding.abInclude.aboutTheRestTxCall.setAlpha(0.3f);
            mBinding.abInclude.aboutTheRestTxLike.setAlpha(0.3f);
            mBinding.abInclude.aboutTheRestTxWebsite.setAlpha(0.3f);
            mBinding.aboutRestaurantFab.setVisibility(View.GONE);
        }
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
                mBinding.abInclude.aboutTheRestName.setText(getString(R.string.name_restaurant_absent));
            } else {
                mBinding.abInclude.aboutTheRestName.setText(mThisRestaurant.getName());
            }
            // address
            if (mThisRestaurant.getAddress().isEmpty()) {
                mBinding.abInclude.aboutTheRestAddress.setText(getString(R.string.address_restaurant_absent));
            } else {
                mBinding.abInclude.aboutTheRestAddress.setText(mThisRestaurant.getAddress());
            }
            double starsRange = Math.round(mThisRestaurant.getRating() * 3 / 5);
            switch ((int) starsRange) {
                case 1:
                    mBinding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
                    mBinding.abInclude.aboutTheRestStar2.setImageDrawable(ic_empty_star);
                    mBinding.abInclude.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
                case 2:
                    mBinding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
                    mBinding.abInclude.aboutTheRestStar2.setVisibility(View.VISIBLE);
                    mBinding.abInclude.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
                case 3:
                    mBinding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
                    mBinding.abInclude.aboutTheRestStar2.setVisibility(View.VISIBLE);
                    mBinding.abInclude.aboutTheRestStar3.setVisibility(View.VISIBLE);
                    break;
                default:
                    mBinding.abInclude.aboutTheRestStar1.setImageDrawable(ic_empty_star);
                    mBinding.abInclude.aboutTheRestStar2.setImageDrawable(ic_empty_star);
                    mBinding.abInclude.aboutTheRestStar3.setImageDrawable(ic_empty_star);
                    break;
            }
        }
    }

    private void setRecyclerViewWorkmates() {
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(mAboutRestaurantVM.getUsersWithTheSameRestaurant(restaurantId), User.class)
                .setLifecycleOwner(this)
                .build();
        WorkmateAdapter workmateAdapter = new WorkmateAdapter(options, this, 2);
        mBinding.abInclude.aboutTheRestRecyclerView.setAdapter(workmateAdapter);
        mBinding.abInclude.aboutTheRestRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    // Workmates list onClickListener
    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {
        // Go to chat with your workmate ...
    }

    private void checkIfThisRestaurantIsFavorite() {
        Log.i(TAG, "checkIfThisRestaurantIsFavorite: ");
        if (favoritesResto != null || favoritesResto.size() > 0) {
            Log.i(TAG, "checkIfThisRestaurantIsFavorite: in if ");
            for (int i = 0; i < favoritesResto.size(); i++) {
                if (favoritesResto.get(i).equals(mThisRestaurant.getPlaceId())) {
                    isLiked = true;
                }
            }
        }
        Log.i(TAG, "checkIfThisRestaurantIsFavorite: isLiked ::: " + isLiked);
        isLiked = false;
        updateIconLike();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setOnClickListeners() {
        if (mThisRestaurant != null) {
            // Call Restaurant
            if (mThisRestaurant.getPhoneNumber() != null)
                mBinding.abInclude.aboutTheRestTxCall.setOnClickListener(v -> makePhoneCall());
            else mBinding.abInclude.aboutTheRestTxCall.setAlpha(0.3f);
            // Like Restaurant
            mBinding.abInclude.aboutTheRestTxLike.setOnClickListener(v -> isLiked());
            if (mThisRestaurant.getWebsite() != null)
                mBinding.abInclude.aboutTheRestTxWebsite.setOnClickListener(v -> visitWebsite());
                // Go to Website of Restaurant
            else mBinding.abInclude.aboutTheRestTxWebsite.setAlpha(0.3f);
            // manage choice of restaurant for lunch
            mBinding.aboutRestaurantFab.setOnClickListener(view -> choseThisRestaurant());
            // TODO change the choice of the user for this restaurant and register it in Firebase
        }
    }

    // ......................................................... Utils functions
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void isLiked() {
        isLiked = !isLiked;
        updateIconLike();
        if (isLiked && !favoritesResto.contains(restaurantId)) {
                favoritesResto.add(mThisRestaurant.getPlaceId());
        } else if (!isLiked && favoritesResto.contains(restaurantId)){
            favoritesResto.remove(mThisRestaurant.getPlaceId());
        }
    }

    private void updateIconLike() {
        Log.i(TAG, "updateIconLike: isLiked ? " + isLiked);
        mBinding.abInclude.aboutTheRestTxLike
                .setCompoundDrawablesRelativeWithIntrinsicBounds(null, isLiked ? ic_Like : ic_notLike, null, null);
    }

    private void updateIconChoiceFAB() {
        mBinding.aboutRestaurantFab.setImageDrawable(isChosen ? ic_rest_chosen : ic_rest_not_chosen);
    }

    private void choseThisRestaurant() {
        isChosen = !isChosen;
        if (isChosen) {
            mUser.setUserRestaurant(mThisRestaurant);
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_chosen);
        } else {
            mUser.setUserRestaurant(null);
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_not_chosen);
        }
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
        if ((url != null) || !url.contains("google.com")) {
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
        ViewWidgets.showSnackBar(0, view, 0 == 0 ?
                getString(R.string.invalid_phone_number)
                : getString(R.string.permission_call_denied));
    }

    private void showMessageBookingRestaurant(boolean mode) {
        ViewWidgets.showSnackBar(0, view, !mode ?
                getString(R.string.cancel_choice_restaurant)
                : getString(R.string.save_choice_restaurant));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUser != null && mThisRestaurant != null) {
            if (isChosen) {
                mAboutRestaurantVM.updateUserRestaurant(mUser.getUid(), mThisRestaurant);
            }
            mAboutRestaurantVM.updateUserFavoriteRestaurantsList(mUser.getUid(), favoritesResto);
        }
    }
}