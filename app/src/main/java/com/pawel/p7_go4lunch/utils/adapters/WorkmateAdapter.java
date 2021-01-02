package com.pawel.p7_go4lunch.utils.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ItemWorkmateBinding;
import com.pawel.p7_go4lunch.model.User;

public class WorkmateAdapter extends FirestoreRecyclerAdapter<User, WorkmateAdapter.WorkmateViewHolder> {
    private static final String TAG = "WORKMATE";
    OnItemClickListener onItemClickListener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public WorkmateAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
        Log.i(TAG, "WorkmateAdapter: CONSTRUCTOR");
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkmateBinding binding = ItemWorkmateBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new WorkmateViewHolder(binding);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int i, @NonNull User user) {
        // TODO add logic for add restaurant name.
        Glide.with(holder.workmateImage.getContext())
                .load(user.getUrlImage())
                .circleCrop()
                .error(R.drawable.persona_placeholder_gray)
                .into(holder.workmateImage);
        holder.description.setText(user.getName());
        Log.i(TAG, "onBindViewHolder: HOLDER");
    }


    class WorkmateViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "WORKMATE";
        ImageView workmateImage;
        TextView description;

        public WorkmateViewHolder(@NonNull ItemWorkmateBinding vBinding) {
            super(vBinding.getRoot());
            workmateImage = vBinding.workmateImg;
            description = vBinding.workmateDescription;
            Log.i(TAG, "WorkmateViewHolder: ");
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
