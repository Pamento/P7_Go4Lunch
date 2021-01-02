package com.pawel.p7_go4lunch.ui.workmates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mWorkmatesViewModel =
                ViewModelProviders.of(this).get(WorkmatesViewModel.class);
        mBinding = FragmentWorkmatesBinding.inflate(inflater,container,false);
        mView = mBinding.getRoot();
        Log.i(TAG, "onCreateView: mView " + mView);
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

    private void setWorkmatesRecyclerView() {
        Query query = firebaseUserCollection.orderBy("email", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        mWorkmateAdapter = new WorkmateAdapter(options);
        // TODO set onClickListener
        mWorkmateAdapter.setOnItemClickListener(documentSnapshot -> {
            String itemId = documentSnapshot.getId();
            Log.i(TAG, "setWorkmatesRecyclerView: itemId of CARDview " + itemId);
            //String itemId = documentSnapshot.get("email");
            if (itemId.isEmpty()) {
                ViewWidgets.showSnackBar(0,mView,"CardView non ID");
            } else {
                ViewWidgets.showSnackBar(0,mView,"CardView ID: " + itemId);
                // TODO go to Chat by calling action (Start Intent)
                // itemID give the id of user in firebaseCollection("users");
            }
        });
        Log.i(TAG, "setWorkmatesRecyclerView: START + adapter " + mWorkmateAdapter);
        mBinding.workmatesRecyclerView.setHasFixedSize(true);
        mBinding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.workmatesRecyclerView.setAdapter(mWorkmateAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        mWorkmateAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        mWorkmateAdapter.stopListening();
    }

    // Need to manually destroy view to avoid memory leaks.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        mBinding = null;
    }
}