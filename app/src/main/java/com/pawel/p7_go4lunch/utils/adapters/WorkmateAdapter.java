package com.pawel.p7_go4lunch.utils.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import android.content.res.Resources;

import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ItemWorkmateBinding;
import com.pawel.p7_go4lunch.model.User;

public class WorkmateAdapter extends FirestoreRecyclerAdapter<User, WorkmateAdapter.WorkmateViewHolder> {
    private static final String TAG = "workmate";
    private final OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private Resources mResources;
    /**
     * @field int mode:
     * 1 - for RecyclerView with all Workmates
     * 2 - for RecyclerView with one specific restaurant chosen
     */
    private final int mMode;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options we pass model and OnClickListener
     */
    public WorkmateAdapter(@NonNull FirestoreRecyclerOptions<User> options, OnItemClickListener onItemClickListener, int mode) {
        super(options);
        mOnItemClickListener = onItemClickListener;
        mMode = mode;
        Log.i(TAG, "WorkmateAdapter: mode " + mMode);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkmateBinding binding = ItemWorkmateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        mContext = parent.getContext();
        mResources = parent.getResources();
        return new WorkmateViewHolder(binding, mOnItemClickListener);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int i, @NonNull User user) {
        Log.i(TAG, "onBindViewHolder: user.getUserRestaurant(): " + user.getUserRestaurant());
        // TODO add logic for add restaurant name.
        boolean workmateEatAt = false;
        String beOrNotToBe = "";
        if (user.getUserRestaurant() != null) {
            if (mMode == 1) {
                beOrNotToBe = String.format(mResources.getString(R.string.workmate_eat_at), user.getName(), user.getUserRestaurant().getName());
            } else {
                beOrNotToBe = String.format(mResources.getString(R.string.about_restaurant_workmate_going_there), user.getName());
            }
        } else {
            // TODO this conditions is it to adapt after add GoogleApi Places services we don't needed
            if (mMode == 1) {
                beOrNotToBe = String.format(mResources.getString(R.string.workmate_not_decide), user.getName());
                workmateEatAt = true;
            }
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.placeholder(R.drawable.persona_placeholder_gray);
        requestOptions = requestOptions.error(R.drawable.persona_placeholder_gray);
        requestOptions = requestOptions.circleCrop();

        Glide.with(holder.workmateImage.getContext())
                .load(user.getUrlImage())
                .apply(requestOptions)
                .into(holder.workmateImage);
//        Glide.with(holder.workmateImage.getContext())
//                .load(user.getUrlImage())
//                .error(R.drawable.persona_placeholder_gray)
//                .placeholder(R.drawable.persona_placeholder_gray)
//                .circleCrop()
//                .into(holder.workmateImage);
        holder.description.setText(beOrNotToBe);
        if (workmateEatAt)
            holder.description.setTextAppearance(mContext, R.style.TextItalicGrayLight);
        //else holder.description.setTextAppearance(mContext, R.style.TextNormalBlack);
    }

    /**
     * If you want to support API 23 or higher as well as lower one,
     * you can use the below method to simplify your task.
     * Use the below method only if you are already targeting API 23 or higher.
     * If you are targeting API is less than 23,
     * the below code will give error as the new method wasn't available in it.
     * https://stackoverflow.com/questions/16270814/setting-textview-textappeareance-programmatically-in-android
     * public void setTextAppearance(Context context, int resId) {
     * if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
     * super.setTextAppearance(context, resId);
     * } else {
     * super.setTextAppearance(resId);
     * }
     * }
     */


    // ...............................................................WorkmateViewHolder.class
    public class WorkmateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView workmateImage;
        AppCompatTextView description;
        OnItemClickListener mOnItemClickListener;

        public WorkmateViewHolder(@NonNull ItemWorkmateBinding vBinding, OnItemClickListener onItemClickListener) {
            super(vBinding.getRoot());
            workmateImage = vBinding.workmateImg;
            description = vBinding.workmateDescription;
            mOnItemClickListener = onItemClickListener;
            vBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnItemClickListener.onItemClick(getSnapshots().getSnapshot(position));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
    }
}
