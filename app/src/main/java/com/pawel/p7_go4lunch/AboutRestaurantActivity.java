package com.pawel.p7_go4lunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding;
import com.pawel.p7_go4lunch.utils.ViewWidgets;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

public class AboutRestaurantActivity extends AppCompatActivity {

    View view;
    ActivityAboutRestaurantBinding binding;
    // TODO get restaurantID.
    private int restaurantID;
    private boolean hasImage = false;
    private int starsRange = 3;// TODO 1 is for test of layout. Default number is 0.
    private boolean hasPhoneNumber = false;
    private boolean hasWebSite = false;
    private boolean isLiked = true;
    // icon witch indicate if this restaurant is the chosen one for the lunch
    private boolean isChosen = false;
    // ic drawable
    private Drawable ic_Like;
    private Drawable ic_notLike;
    private Drawable ic_rest_chosen;
    private Drawable ic_rest_not_chosen;
    // final variable for makePhoneCall()
    private static final int REQUEST_CALL = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding
                .inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.aboutTheRestaurantToolbar);
        binding.toolbarLayout.setTitle(getTitle());
        /**
         * TODO ind restaurantID or by Navigation Arguments or by
         * call to research fot the restaurant where user go to lunch
         * if restaurantID is not present in arguments then get it from user instance choice of restaurant
         */
        getInstanceUserRestaurant();
        setDrawable();
        setRestaurantImage();
        setNameAddressStarsRange();
        // TODO isLiked() fun is here only fot the time of test and construction.
        //  At the moment when wy will have the Firebase instance of restaurant and user,
        //  we can delete ths call from here
        isLiked();
        setOnClickListeners();
        // windowTranslucentStatus for KITKAT android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void setDrawable() {
        ic_Like = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_in_star_36, null);
        ic_notLike = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_primary_36, null);
        ic_rest_chosen = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle_32, null);
        ic_rest_not_chosen = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_circle_red_32, null);
    }

    private void setRestaurantImage() {
        if (hasImage) {
            // TODO code for this action
        }
    }

    private void setNameAddressStarsRange() {
        // TODO set name, address, stars range
        binding.abInclude.aboutTheRestName.setText("Restaurant name");
        binding.abInclude.aboutTheRestAddress.setText("French restaurant - 69 rue Faubourge Poissonier");
        if (starsRange>0) {
            binding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
            if (starsRange>1) {
                binding.abInclude.aboutTheRestStar2.setVisibility(View.VISIBLE);
                if (starsRange>2) {
                    binding.abInclude.aboutTheRestStar3.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // set "Like" star according to choice of user, if it's liked or not it has different star
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getInstanceUserRestaurant() {
        // TODO check if In ModelView user has liked or not this restaurant
        // TODO getUser & getRestaurant
        // restaurantID = getUser().getRestaurants().contain(getRestaurant().getId());
        // hasImage = getUser().getRestaurants().contain(getRestaurant().getImage());
        // or
        // ModelRestaurant mMR = getRestaurant().
        // restaurantID = mMR.getID();
        // isLiked = ...
        // hasWebSite = ...
        // hasPhoneNumber = ...
        // myRestaurant = ...

        isLiked();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void isLiked() {
        // TODO getUser for check if the restaurant is liked or not
        // TODO getRestaurantID and check if it is in the liked restaurants list of user
        /**
         * ex.:
         * String restaurantID = getRestaurant().getId();
         * if (restaurantID.isEmpty()) {
         *     setDrawablePrimary
         * } else {
         *     setDrawableYellow
         * }
         * **********************************************************
         * Liked function set restaurantID in ViwModel instance of restaurant
         * if id is present, star is yellow if not, star is primary.
         * or
         * manage during life of activity just the boolean check
         * ex:isLiked = !isLiked;
         * and only in moment OnPause() with the choice of like in the Firebase instance
         */
        isLiked = !isLiked;
        if (isLiked) {
            binding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_Like, null, null);
        } else {
            binding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_notLike, null, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOnClickListeners() {

        // manage call for the restaurant
        // TODO if user choice restaurant has number
        if (hasPhoneNumber) {
            binding.abInclude.aboutTheRestTxCall.setOnClickListener(v -> {
                makePhoneCall();
            });
        } else {
            binding.abInclude.aboutTheRestTxCall.setAlpha(0.3f);
        }

        // manage "LIKE" for restaurant
        binding.abInclude.aboutTheRestTxLike.setOnClickListener(v -> {
            isLiked();
        });

        // manage website link
        if (hasWebSite) {
            binding.abInclude.aboutTheRestTxWebsite.setOnClickListener(v -> {
                visitWebsite();
            });
        } else {
            binding.abInclude.aboutTheRestTxWebsite.setAlpha(0.3f);
        }


        // manage choice of restaurant for lunch
        // TODO change the star in
        binding.aboutRestaurantFab.setOnClickListener(view -> {
            choseThisRestaurant();
            // TODO change the choice of the user for this restaurant and register it in Firebase
            // TODO set icon if restaurant is chosen or not
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

    }

    private void choseThisRestaurant() {
        // TODO this fun() is same like isLiked().
        // TODO add logic to change status of choice of restaurant
        /**
         * ex.:
         * String restaurantID = getRestaurant().getId();
         * if (restaurantID.isEmpty()) {
         *     setDrawablePrimary
         * } else {
         *     setDrawableYellow
         * }
         * **********************************************************
         * Chosen function set restaurantID in ViwModel instance of restaurant
         * if id is present, star is yellow if not, star is primary.
         *          * or
         *          * manage during life of activity just the boolean check
         *          * ex:isLiked = !isLiked;
         *          * and only in moment OnPause() with the choice of like in the Firebase instance
         */
        isChosen = !isChosen;
        if (isChosen) {
            binding.aboutRestaurantFab.setImageDrawable(ic_rest_chosen);
        } else {
            binding.aboutRestaurantFab.setImageDrawable(ic_rest_not_chosen);
        }
    }

    /**
     * in correlation with makePhoneCall()
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                //Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall() {
        String number = "1234567890";
        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(AboutRestaurantActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AboutRestaurantActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
                if (intent.resolveActivity(Objects.requireNonNull(AboutRestaurantActivity.this).getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        } else {
            ViewWidgets.showSnackBar(0,view,"Numero est incorrect.");
            //Toast.makeText(MainActivity.this, "Phone number is incorrect", Toast.LENGTH_SHORT).show();
        }
    }


    private void visitWebsite() {
        // TODO this code
        //String url = getRestaurant().getWebSite();
        String url = "google.com";
        if (!url.startsWith("https://") || !url.startsWith("http://")) {
            url = "http://" + url;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        if (intent.resolveActivity(Objects.requireNonNull(AboutRestaurantActivity.this).getPackageManager()) != null) {
            startActivity(intent);
        } else {
            //Toast.makeText(getContext(), "address don't found", Toast.LENGTH_LONG).show();
        }
    }

    // TODO set onClickListener on RecyclerView for chat


    @Override
    protected void onPause() {
        super.onPause();
        // TODO if changes was made, persist new data in Firebase
    }
}