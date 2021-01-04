package com.pawel.p7_go4lunch.utils.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import android.content.res.Resources;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ItemWorkmateBinding;
import com.pawel.p7_go4lunch.model.User;

public class WorkmateAdapter extends FirestoreRecyclerAdapter<User, WorkmateAdapter.WorkmateViewHolder> {

    public OnItemClickListener onItemClickListener;
    private Context mContext;
    private Resources mResources;
    private TextView mTextView;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public WorkmateAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkmateBinding binding = ItemWorkmateBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        mContext = parent.getContext();
        mResources = parent.getResources();
        return new WorkmateViewHolder(binding);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int i, @NonNull User user) {
        // TODO add logic for add restaurant name.
        boolean workmateEatAt = false;
        String beOrNotToBe;
        if (user.getUserRestaurant() != null) {
            beOrNotToBe = String.format(mResources.getString(R.string.workmate_eat_at),user.getName(),user.getUserRestaurant().getName());
            workmateEatAt = true;
        } else {
            beOrNotToBe = String.format(mResources.getString(R.string.workmate_not_decide),user.getName());
        }

        Glide.with(holder.workmateImage.getContext())
                .load(user.getUrlImage())
                .circleCrop()
                .error(R.drawable.persona_placeholder_gray)
                .into(holder.workmateImage);
        holder.description.setText(beOrNotToBe);
        if (workmateEatAt) holder.description.setTextAppearance(mContext, R.style.TextNormalBlack);
        else holder.description.setTextAppearance(mContext,R.style.TextItalicGrayLight);
    }

    /**
     * If you want to support API 23 or higher as well as lower one,
     * you can use the below method to simplify your task.
     * Use the below method only if you are already targeting API 23 or higher.
     * If you are targeting API is less than 23,
     * the below code will give error as the new method wasn't available in it.
     * https://stackoverflow.com/questions/16270814/setting-textview-textappeareance-programmatically-in-android
     *     public void setTextAppearance(Context context, int resId) {
     *         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
     *             super.setTextAppearance(context, resId);
     *         } else {
     *             super.setTextAppearance(resId);
     *         }
     *     }
     */



    // ...............................................................WorkmateViewHolder.class
    public class WorkmateViewHolder extends RecyclerView.ViewHolder {
        ImageView workmateImage;
        TextView description;

        public WorkmateViewHolder(@NonNull ItemWorkmateBinding vBinding) {
            super(vBinding.getRoot());
            workmateImage = vBinding.workmateImg;
            description = vBinding.workmateDescription;
            vBinding.workmateCardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION &&  onItemClickListener != null) {
                    onItemClickListener.onItemClick(getSnapshots().getSnapshot(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
