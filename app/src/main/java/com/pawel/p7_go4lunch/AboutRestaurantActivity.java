package com.pawel.p7_go4lunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;
import com.pawel.p7_go4lunch.viewModels.AboutRestaurantViewModel;

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
    // TODO get restaurantID.
    private int restaurantID;
    private boolean hasImage = false;
    private int starsRange = 3;// TODO 1 is for test of layout. Default number is 0.
    private boolean hasPhoneNumber = false;
    private boolean hasWebSite = false;
    private boolean isLiked = true;
    // icon witch indicate if this restaurant is the chosen one for the lunch
    private boolean isChosen = false;
    // icon drawable
    private Drawable ic_Like;
    private Drawable ic_notLike;
    private Drawable ic_rest_chosen;
    private Drawable ic_rest_not_chosen;
    // final variable for makePhoneCall()
    private static final int REQUEST_CALL = 1;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference firestoreUserColRef = db.collection(Const.COLLECTION_USERS);
    private WorkmateAdapter mWorkmateAdapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAboutRestaurantVM = new ViewModelProvider(this).get(AboutRestaurantViewModel.class);
        mAboutRestaurantVM.init();
        mBinding = com.pawel.p7_go4lunch.databinding.ActivityAboutRestaurantBinding
                .inflate(getLayoutInflater());
        view = mBinding.getRoot();
        setContentView(view);
        setSupportActionBar(mBinding.aboutTheRestaurantToolbar);
        mBinding.toolbarLayout.setTitle(getTitle());
        /**
         * TODO ind restaurantID or by Navigation Arguments or by
         * call to research fot the restaurant where user go to lunch
         * if restaurantID is not present in arguments then get it from user instance choice of restaurant
         */
        getInstanceUserRestaurant();
        setRestaurantImage();
        setDrawable();
        setNameAddressStarsRange();
        setRecyclerViewWorkmates();
        // TODO isLiked() fun is here only fot the time of test and construction.
        //  At the moment when wy will have the Firebase instance of restaurant and user,
        //  we can delete ths call from here
        isLiked();
        setOnClickListeners();
        // windowTranslucentStatus for KITKAT android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void setRecyclerViewWorkmates() {
        Log.i(TAG, "setRecyclerViewWorkmates: START ");
        /*
         * TODO get the RestaurantName from
         * - extra of intent (click on mark google maps or on list of restaurants)
         * - currentUser choice of restaurant
         * https://medium.com/@haxzie/using-intents-and-extras-to-pass-data-between-activities-android-beginners-guide-565239407ba0
         * Intent intent = getIntent();
         *
         * //get the attached extras from the intent
         * //we should use the same key as we used to attach the data.
         * String user_name = intent.getStringExtra("USER_NAME");
         *
         * //if you have used any other type of data, you should use the
         * //particular getExtra method to extract the data from Intet
         * Integer user_id = intent.getIntExtra("USER_ID");
         * float user_rating = intent.getFloatExtra("USER_RATING");
          */
        String restaurantName = "RestaurantName";
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
//        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
//                .setQuery(mAboutRestaurantVM.getSelectedUsersFromCollection(), User.class)
//                .setLifecycleOwner(this)
//                .build();
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(mAboutRestaurantVM.getAllUsersFromCollection(), User.class)
                .setLifecycleOwner(this)
                .build();
        mWorkmateAdapter = new WorkmateAdapter(options,this,2);
        mBinding.abInclude.aboutTheRestRecyclerView.setAdapter(mWorkmateAdapter);
        mBinding.abInclude.aboutTheRestRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    // Workmates list onClickListener
    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {

    }

    private void setDrawable() {
        ic_Like = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_black_yellow_36, null);
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
        mBinding.abInclude.aboutTheRestName.setText("Restaurant name");
        mBinding.abInclude.aboutTheRestAddress.setText("French restaurant - 69 rue Faubourge Poissonier");
        if (starsRange>0) {
            mBinding.abInclude.aboutTheRestStar1.setVisibility(View.VISIBLE);
            if (starsRange>1) {
                mBinding.abInclude.aboutTheRestStar2.setVisibility(View.VISIBLE);
                if (starsRange>2) {
                    mBinding.abInclude.aboutTheRestStar3.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // set "Like" star according to choice of user, if it's liked or not it has different star
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
            mBinding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_Like, null, null);
        } else {
            mBinding.abInclude.aboutTheRestTxLike
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(null, ic_notLike, null, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setOnClickListeners() {

        // manage call for the restaurant
        // TODO if user choice restaurant has number
        if (hasPhoneNumber) {
            mBinding.abInclude.aboutTheRestTxCall.setOnClickListener(v -> {
                makePhoneCall();
            });
        } else {
            mBinding.abInclude.aboutTheRestTxCall.setAlpha(0.3f);
        }

        // manage "LIKE" for restaurant
        mBinding.abInclude.aboutTheRestTxLike.setOnClickListener(v -> {
            isLiked();
        });

        // manage website link
        if (hasWebSite) {
            mBinding.abInclude.aboutTheRestTxWebsite.setOnClickListener(v -> {
                visitWebsite();
            });
        } else {
            mBinding.abInclude.aboutTheRestTxWebsite.setAlpha(0.3f);
        }


        // manage choice of restaurant for lunch
        // TODO change the star in
        mBinding.aboutRestaurantFab.setOnClickListener(view -> {
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
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_chosen);
        } else {
            mBinding.aboutRestaurantFab.setImageDrawable(ic_rest_not_chosen);
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