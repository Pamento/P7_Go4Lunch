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

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final List<Restaurant> mRestaurants;
    private final OnItemRestaurantListClickListener mListClickListener;

    public RestaurantAdapter(List<Restaurant> restaurants, OnItemRestaurantListClickListener listClickListener) {
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

        Restaurant restaurant = mRestaurants.get(position);
        String d = holder.itemView.getResources().getString(R.string.distance_to_restaurant, restaurant.getDistance());
        int n = 0;
        String w = "";
        if (restaurant.getUserList() != null) {
            n = restaurant.getUserList().size();
            w = holder.itemView.getResources().getString(R.string.workmate_number, restaurant.getUserList().size());
        }
        String h = holder.itemView.getResources().getString(R.string.close_now);
        if (restaurant.getOpeningHours() != null && restaurant.getOpeningHours().getOpenNow())
            h = holder.itemView.getResources().getString(R.string.open_now);
        holder.restaurantName.setText(restaurant.getName());
        holder.restaurantAddress.setText(restaurant.getAddress());
        holder.restaurantOpeningTime.setText(h);
        holder.restaurantDistanceFromUser.setText(d);
        if (n > 0)
            holder.restaurantWorkmatesNumber.setText(w);
        else {
            holder.restaurantWorkmatesNumber.setVisibility(View.GONE);
            holder.workmatesIcon.setVisibility(View.GONE);
        }

        holder.restaurantRatingBar.setRating((float)restaurant.getRating());
        // Restaurant image
        GlideApp.with(holder.restaurantImage.getContext())
                .load(restaurant.getImage())
                .placeholder(R.drawable.restaurant_default)
                .error(R.drawable.restaurant_default)
                .into(holder.restaurantImage);
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    // ....................................................ViewHolder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatTextView restaurantName;
        AppCompatTextView restaurantAddress;
        AppCompatTextView restaurantOpeningTime;
        AppCompatTextView restaurantDistanceFromUser;
        AppCompatTextView restaurantWorkmatesNumber;
        AppCompatImageView restaurantImage;
        AppCompatImageView workmatesIcon;
        AppCompatRatingBar restaurantRatingBar;
        OnItemRestaurantListClickListener mOnItemRestaurantListClickListener;

        public RestaurantViewHolder(@NonNull ItemRestaurantBinding vBinding, OnItemRestaurantListClickListener onItemRestaurantListClickListener ) {
            super(vBinding.getRoot());
            restaurantName = vBinding.listRestaurantName;
            restaurantAddress = vBinding.listRestaurantAddress;
            restaurantOpeningTime = vBinding.listRestaurantOpenTime;
            restaurantDistanceFromUser = vBinding.listRestaurantDistance;
            restaurantWorkmatesNumber = vBinding.listRestaurantNumberWorkmates;
            workmatesIcon = vBinding.listRestaurantIcPersona;
            restaurantImage = vBinding.listRestaurantImage;
            restaurantRatingBar = vBinding.listRestaurantRatingBar;
            mOnItemRestaurantListClickListener = onItemRestaurantListClickListener;
            vBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemRestaurantListClickListener.onItemRestaurantListClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnItemRestaurantListClickListener {
        void onItemRestaurantListClick(int position);
    }
}
