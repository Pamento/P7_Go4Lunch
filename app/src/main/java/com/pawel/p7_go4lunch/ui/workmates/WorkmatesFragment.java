package com.pawel.p7_go4lunch.ui.workmates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentWorkmatesBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;

public class WorkmatesFragment extends Fragment implements WorkmateAdapter.OnItemClickListener {
    private static final String TAG = "workmate";

    private WorkmatesViewModel mWorkmatesVM;
    private FragmentWorkmatesBinding mBinding;
    private View mView;
    private WorkmateAdapter mWorkmateAdapter;

    // To disable SearchView Widget 1 step
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    // To disable SearchView Widget 2 step
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.toolbar_search_icon);
        if (item != null) item.setVisible(false);
        menu.clear();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mWorkmatesVM = new ViewModelProvider(this).get(WorkmatesViewModel.class);
        mWorkmatesVM.init();
        mBinding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        mView = mBinding.getRoot();
        setProgressBar();
        setWorkmatesRecyclerView();
//        final TextView textView = root.findViewById(R.id.text_notifications);
//        mWorkmatesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return mView;
    }

    public void setProgressBar() {
        mBinding.workmatesProgressBar.progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void setWorkmatesRecyclerView() {
        mWorkmatesVM.getAllUsersFromCollection().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        mBinding.workmatesProgressBar.progressBarLayout.setVisibility(View.GONE);
                        mBinding.workmatesErrorNoData.errorNoData.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    } else {
                        boolean isEmpty = task.getResult().isEmpty();
                        Log.i(TAG, "setWorkmatesRecyclerView: query isEmpty? false when run: " + isEmpty);
                    }
                });
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(mWorkmatesVM.getAllUsersFromCollection(), User.class)
                .setLifecycleOwner(this)
                .build();
        mWorkmateAdapter = new WorkmateAdapter(options, this, 1);
        mBinding.workmatesProgressBar.progressBarLayout.setVisibility(View.GONE);
        mBinding.workmatesRecyclerView.setAdapter(mWorkmateAdapter);
        mBinding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {
        String itemId = documentSnapshot.getId();
        //String itemId = documentSnapshot.get("email");
        if (itemId.isEmpty()) {
            Log.i(TAG, "onItemClick: WorkmateID: null");
        } else {
            Log.i(TAG, "onItemClick: WorkmateID: " + itemId);
            // TODO go to Chat by calling action (Start Intent)
            // itemID give the id of user in firebaseCollection("users");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mWorkmateAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mWorkmateAdapter.stopListening();
    }

    // Need to manually destroy view to avoid memory leaks.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}