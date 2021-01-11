package com.pawel.p7_go4lunch.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ItemRestaurantBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.GlideApp;

import java.util.ArrayList;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final ArrayList<Restaurant> mRestaurants;
    private final OnItemRestaurantListClickListener mListClickListener;

    public RestaurantAdapter(ArrayList<Restaurant> restaurants, OnItemRestaurantListClickListener listClickListener) {
        mRestaurants = restaurants;
        mListClickListener = listClickListener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRestaurantBinding binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding, mListClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        String url = getImageUrl();
        Restaurant restaurant = mRestaurants.get(position);
        holder.restaurantName.setText(restaurant.getName());
        holder.restaurantAddress.setText(restaurant.getAddress());
        holder.restaurantOpeningTime.setText("");
        holder.restaurantDistanceFromUser.setText("");
        holder.restaurantWorkmatesNumber.setText("");
        holder.restaurantRatingBar.setRating(0.1f);
        // Restaurant image
        GlideApp.with(holder.restaurantImage.getContext())
                .load(url)// TODO pass getUrl() directly here
                .placeholder(R.drawable.restaurant_default)
                .error(R.drawable.restaurant_default)
                .into(holder.restaurantImage);
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    private String getImageUrl() {
        String url = "https://google.com";
        // TODO get Url of image of restaurant
        return url;
    }


    // ....................................................ViewHolder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatTextView restaurantName;
        AppCompatTextView restaurantAddress;
        AppCompatTextView restaurantOpeningTime;
        AppCompatTextView restaurantDistanceFromUser;
        AppCompatTextView restaurantWorkmatesNumber;
        AppCompatImageView restaurantImage;
        AppCompatRatingBar restaurantRatingBar;
        OnItemRestaurantListClickListener mOnItemRestaurantListClickListener;

        public RestaurantViewHolder(@NonNull ItemRestaurantBinding vBinding, OnItemRestaurantListClickListener onItemRestaurantListClickListener ) {
            super(vBinding.getRoot());
            restaurantName = vBinding.listRestaurantName;
            restaurantAddress = vBinding.listRestaurantAddress;
            restaurantOpeningTime = vBinding.listRestaurantOpenTime;
            restaurantDistanceFromUser = vBinding.listRestaurantDistance;
            restaurantWorkmatesNumber = vBinding.listRestaurantNumberWorkmates;
            restaurantImage = vBinding.listRestaurantImage;
            restaurantRatingBar = vBinding.listRestaurantRatingBar;
            mOnItemRestaurantListClickListener = onItemRestaurantListClickListener;
            vBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemRestaurantListClickListener.onItemRestaurantListClick(getAdapterPosition());
        }
    }

    public interface OnItemRestaurantListClickListener {
        void onItemRestaurantListClick(int position);
    }
}
