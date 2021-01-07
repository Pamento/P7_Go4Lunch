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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentWorkmatesBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;

public class WorkmatesFragment extends Fragment implements WorkmateAdapter.OnItemClickListener {
    private static final String TAG = "Firestore";

    private WorkmatesViewModel mWorkmatesViewModel;
    private FragmentWorkmatesBinding mBinding;
    private FragmentActivity mFragmentActivity;
    private View mView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference firestoreUserColRef = db.collection(Const.COLLECTION_USERS);
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
        mWorkmatesViewModel =
                ViewModelProviders.of(this).get(WorkmatesViewModel.class);
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
        mBinding.workmatesProgressBar.setVisibility(View.VISIBLE);
    }

    private void setWorkmatesRecyclerView() {
        Query query = firestoreUserColRef.orderBy(Const.FIREBASE_ADAPTER_QUERY_EMAIL, Query.Direction.DESCENDING);
        Log.i(TAG, "setWorkmatesRecyclerView: QUERY " + query);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                mBinding.workmatesProgressBar.setVisibility(View.GONE);
                mBinding.workmatesErrorNonData.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error getting documents: ", task.getException());
            } else {
                boolean isEmpty = task.getResult().isEmpty();
                Log.i(TAG, "setWorkmatesRecyclerView: query isEmpty? false when run: " + isEmpty);
            }
        });

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        mWorkmateAdapter = new WorkmateAdapter(options,this);
        mBinding.workmatesProgressBar.setVisibility(View.GONE);
        mBinding.workmatesRecyclerView.setAdapter(mWorkmateAdapter);
        mBinding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot) {
        String itemId = documentSnapshot.getId();
        //String itemId = documentSnapshot.get("email");
        if (itemId.isEmpty()) {
            ViewWidgets.showSnackBar(0, mView, "CardView non ID");
        } else {
            ViewWidgets.showSnackBar(0, mView, "CardView ID: " + itemId);
            // TODO go to Chat by calling action (Start Intent)
            // itemID give the id of user in firebaseCollection("users");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mWorkmateAdapter.startListening();
        firestoreUserColRef.addSnapshotListener(getActivity(), (value, error) -> {
            if (error != null) {
                Log.e(TAG, "onEvent: ", error );
            }
            if (!value.isEmpty()) {
                // How to skip call second time the Users from firebase ?
                // How to pass QuerySnapshot in to query ?
                // For this moment we call again setWorkmatesRecyclerView();
                setWorkmatesRecyclerView();
            }
        });
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