package com.pawel.p7_go4lunch.ui.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentWorkmatesBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;

public class WorkmatesFragment extends Fragment {
    private static final String TAG = "WORKMATE";

    private WorkmatesViewModel mWorkmatesViewModel;
    private FragmentWorkmatesBinding mBinding;
    private View mView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference firebaseUserCollection = db.collection(Const.COLLECTION_USERS);
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
        if (item != null ) item.setVisible(false);
        menu.clear();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mWorkmatesViewModel =
                ViewModelProviders.of(this).get(WorkmatesViewModel.class);
        mBinding = FragmentWorkmatesBinding.inflate(inflater,container,false);
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
        mBinding.workmatesProgressBar.setVisibility(View.VISIBLE);
    }

    private void setWorkmatesRecyclerView() {
        Query query = firebaseUserCollection.orderBy(Const.FIREBASE_ADAPTER_QUERY_EMAIL, Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        mWorkmateAdapter = new WorkmateAdapter(options);
        // TODO set onClickListener
        mWorkmateAdapter.setOnItemClickListener(documentSnapshot -> {
            String itemId = documentSnapshot.getId();
            //String itemId = documentSnapshot.get("email");
            if (itemId.isEmpty()) {
                ViewWidgets.showSnackBar(0,mView,"CardView non ID");
            } else {
                ViewWidgets.showSnackBar(0,mView,"CardView ID: " + itemId);
                // TODO go to Chat by calling action (Start Intent)
                // itemID give the id of user in firebaseCollection("users");
            }
        });
        mBinding.workmatesProgressBar.setVisibility(View.GONE);
        mBinding.workmatesRecyclerView.setAdapter(mWorkmateAdapter);
        mBinding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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