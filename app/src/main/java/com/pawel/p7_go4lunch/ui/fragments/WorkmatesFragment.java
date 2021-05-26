package com.pawel.p7_go4lunch.ui.fragments;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ErrorNoDataFullscreenMessageBinding;
import com.pawel.p7_go4lunch.databinding.FragmentWorkmatesBinding;
import com.pawel.p7_go4lunch.databinding.ProgressBarBinding;
import com.pawel.p7_go4lunch.databinding.WifiOffBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;
import com.pawel.p7_go4lunch.viewModels.WorkmatesViewModel;

public class WorkmatesFragment extends Fragment implements WorkmateAdapter.OnItemClickListener {
    private static final String TAG = "workmate";

    private WorkmatesViewModel mWorkmatesVM;
    private FragmentWorkmatesBinding mBinding;
    private ProgressBarBinding mBarBinding;
    private WifiOffBinding mWifiOffBinding;
    private ErrorNoDataFullscreenMessageBinding mErrorMessageBinding;
    private WorkmateAdapter mWorkmateAdapter;

    // To disable SearchView Widget step 1/2
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    // To disable SearchView Widget step 2/2
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.toolbar_search_icon);
        if (item != null) item.setVisible(false);
        menu.clear();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initWorkmatesViewModel();
        mBinding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        mBarBinding = mBinding.workmatesProgressBar;
        mWifiOffBinding = mBinding.workmatesWifiOff;
        mErrorMessageBinding = mBinding.workmatesErrorNoData;
        View view = mBinding.getRoot();
        setProgressBar();
        setWorkmatesRecyclerView();
        return view;
    }

    private void initWorkmatesViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mWorkmatesVM = new ViewModelProvider(requireActivity(), vmf).get(WorkmatesViewModel.class);
    }

    public void setProgressBar() {
        mBarBinding.progressBar.setVisibility(View.VISIBLE);
    }

    private void setWorkmatesRecyclerView() {
        mWorkmatesVM.getAllUsersFromCollection().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        mBarBinding.progressBar.setVisibility(View.GONE);
                        mErrorMessageBinding.errorNoData.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    } else {
                        if (!LocationUtils.isNetworkAvailable()) {
                            mBarBinding.progressBar.setVisibility(View.GONE);
                            mWifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
                        }
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
//        String itemId = documentSnapshot.getId();
//        //String itemId = documentSnapshot.get("email");
//        if (itemId.isEmpty()) {
//            Log.i(TAG, "onItemClick: WorkmateID: null");
//        } else {
//            Log.i(TAG, "onItemClick: WorkmateID: " + itemId);
//            // itemID give the id of user in firebaseCollection("users");
//        }
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
        mBarBinding = null;
        mErrorMessageBinding = null;
        mBinding = null;
    }
}