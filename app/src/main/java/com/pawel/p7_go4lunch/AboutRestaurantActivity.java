package com.pawel.p7_go4lunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

public class AboutRestaurantActivity extends AppCompatActivity implements WorkmateAdapter.OnItemClickListener {
    private static final String TAG = "workmate";
    private AboutRestaurantViewModel mAboutRestaurantVM;
    private View view;
    private ActivityAboutRestaurantBinding mBinding;
    private User mUser;
    private Restaurant mThisRestaurant;
    private String restaurantId;
    // The restaurant is isLiked & or isChosen ? Set icons witch indicate if this restaurant is the chosen one for the lunch
    private boolean isChosen;
    private boolean isLiked;
    private Drawable ic_Like;
    private Drawable ic_notLike;
    private Drawable ic_rest_chosen;
    private Drawable ic_rest_not_chosen;

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
        getRestaurant();
        setUiAboutRestaurant();
        setRecyclerViewWorkmates();
        setOnClickListeners();
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
    }

    private void getUser() {
        if (mAboutRestaurantVM.getUser() != null) mUser = mAboutRestaurantVM.getUser();
        if (mUser != null) setUpUiUser();
    }

    // set "Like" star according to choice of user, if it's liked or not it has different star
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getRestaurant() {
        Bundle extras = getIntent().getExtras();
        Log.i(TAG, "ABOUT ___getRestaurant: extras: " + extras);
        if (extras != null) {
            String placeId = extras.getString(Const.EXTRA_KEY_RESTAURANT);
            mThisRestaurant = mAboutRestaurantVM.getRestaurant(placeId);
            restaurantId = placeId;
        } else {
            mThisRestaurant = mUser.getUserRestaurant();
            restaurantId = mUser.getUserRestaurant().getPlaceId();
        }

        setRestaurantImage();
    }

    private void setUiAboutRestaurant() {
        // name
        mBinding.abInclude.aboutTheRestName.setText(
                mThisRestaurant.getName() != null ?
                        getString(R.string.name_restaurant_absent) : mThisRestaurant.getName());
        // address
        mBinding.abInclude.aboutTheRestAddress.setText(
                mThisRestaurant.getAddress() != null ?
                        getString(R.string.address_restaurant_absent) : mThisRestaurant.getAddress());
        // TODO set name, address, stars range. Maybe refactor stars logic.?
        // rating google in 5 stars convert in 3 stars
        double starsRange = Math.round(mThisRestaurant.getRating() * 3 / 5);
        if (starsRange > 0) {
            mBinding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
            if (starsRange > 1) {
                mBinding.abInclude.aboutTheRestStar2.setVisibility(View.VISIBLE);
                if (starsRange > 2) {
                    mBinding.abInclude.aboutTheRestStar3.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setRecyclerViewWorkmates() {
        Log.i(TAG, "ABOUT ___setRecyclerViewWorkmates: START ");
        // TODO when restaurantName set, than set query below in AboutRestaurantViewModel
//        mAboutRestaurantVM.getSelectedUsersFromCollection(restaurantName).get()
//                .addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult().isEmpty()) {
//                // TODO mBinding.abInclude.progressBar.setVisibility(View.GONE);
//                mBinding.abInclude.aboutTheRestWorkmatesListEmpty.setVisibility(View.VISIBLE);
//                Log.e(TAG, "Error getting documents: ", task.getException());
//            } else {
//                boolean isEmpty = task.getResult().isEmpty();
//                Log.i(TAG, "setWorkmatesRecyclerView: query isEmpty? false when run: " + isEmpty);
//            }
//        });
        // TODO pas the name of restaurant to search in firestore: which user goes to this restaurant in this context. HOM = restaurantId
        // version 1
        if (restaurantId != null) {
            Log.i(TAG, "ABOUT ___setRecyclerViewWorkmates: restaurantId " + restaurantId);
//        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
//                .setQuery(mAboutRestaurantVM.getSelectedUsersFromCollection(restaurantId), User.class)
//                .setLifecycleOwner(this)
//                .build();
        }
        // version 2
//        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
//                .setQuery(mAboutRestaurantVM.getSelectedUsersFromCollection(), User.class)
//                .setLifecycleOwner(this)
//                .build();
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(mAboutRestaurantVM.getAllUsersFromCollection(), User.class)
                .setLifecycleOwner(this)
                .build();
        WorkmateAdapter workmateAdapter = new WorkmateAdapter(options, this, 2);
        mBinding.abInclude.aboutTheRestRecyclerView.setAdapter(workmateAdapter);
        mBinding.abInclude.aboutTheRestRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    // Workmates list onClickListener
    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {

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

    private void setUpUiUser() {
        Log.i(TAG, "ABOUT ____setUpUiUser START: isChosen " + isChosen);
        isLiked = checkIfThisRestaurantIsFavorite();
        isChosen = mUser.getUserRestaurant() != null && mUser.getUserRestaurant().getPlaceId().equals(mThisRestaurant.getPlaceId());
        Log.i(TAG, "ABOUT ____setUpUiUser END: isChosen " + isChosen);
        updateIconChoiceFAB();
        updateIconLike();
    }

    private boolean checkIfThisRestaurantIsFavorite() {
        if (mUser.getFavoritesRestaurants() != null) {
            for (String rstId : mUser.getFavoritesRestaurants()) {
                return rstId.equals(mThisRestaurant.getPlaceId());
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setOnClickListeners() {
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

    /**
     * In correlation with makePhoneCall()
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Const.REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                showMessagePhoneNumberInvalid(1);
            }
        }
    }

    // ......................................................... Utils functions
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void isLiked() {
        isLiked = !isLiked;
        updateIconLike();
        if (isLiked) {
            mUser.getFavoritesRestaurants().add(mThisRestaurant.getPlaceId());
        } else {
            mUser.getFavoritesRestaurants().remove(mThisRestaurant.getPlaceId());
        }
    }

    private void updateIconLike() {
        Log.i(TAG, "ABOUT ___updateIconLike: isLiked " + isLiked);
        mBinding.abInclude.aboutTheRestTxLike
                .setCompoundDrawablesRelativeWithIntrinsicBounds(null, isLiked ? ic_Like : ic_notLike, null, null);
        /*
                if (isLiked) {
            mBinding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_Like, null, null);
        } else {
            mBinding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_notLike, null, null);
        }
         */
    }

    private void updateIconChoiceFAB() {
        int counter = 0;
        Log.i(TAG, "ABOUT ___updateIconChoiceFAB: isChosen " + isChosen + " _"+ counter);
        mBinding.aboutRestaurantFab.setImageDrawable(isChosen ? ic_rest_chosen : ic_rest_not_chosen);
        counter = counter +1;
    }

    private void choseThisRestaurant() {
        isChosen = !isChosen;
        if (isChosen) {
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_chosen);
        } else {
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_not_chosen);
        }
        showMessageBookingRestaurant(isChosen);
    }

    private void makePhoneCall() {
        Log.i(TAG, "ABOUT __makePhoneCall: START ");
        String number = mThisRestaurant.getPhoneNumber();
        if ((number != null) && (number.trim().length() > 0)) {
            if (ContextCompat.checkSelfPermission(AboutRestaurantActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AboutRestaurantActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, Const.REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
                if (intent.resolveActivity(Objects.requireNonNull(AboutRestaurantActivity.this).getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        } else {
            showMessagePhoneNumberInvalid(0);
        }
    }

    private void visitWebsite() {
        Log.i(TAG, "ABOUT ___visitWebsite: START ");
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

    private void showMessagePhoneNumberInvalid(int mode) {
        ViewWidgets.showSnackBar(mode, view, mode == 0 ?
                getString(R.string.invalid_phone_number)
                : getString(R.string.permission_call_denided));
    }

    private void showMessageBookingRestaurant(boolean mode) {
        ViewWidgets.showSnackBar(0, view, !mode ?
                getString(R.string.cancel_choice_restaurnat)
                : getString(R.string.save_choice_restaurant));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO if changes was made, persist new data in Firebase
        if (isChosen) {
            mAboutRestaurantVM.updateUserRestaurant(mUser.getUid(), mThisRestaurant);
        } else if (!isChosen && mUser.getUserRestaurant() != null) {
            mAboutRestaurantVM.updateUserRestaurant(mUser.getUid(),null);
        }
        mAboutRestaurantVM.updateUserFavoriteRestaurantsList(mUser.getUid(), mUser.getFavoritesRestaurants());
    }
}