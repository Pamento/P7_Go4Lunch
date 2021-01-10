package com.pawel.p7_go4lunch.ui.listView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pawel.p7_go4lunch.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.databinding.FragmentListViewBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.adapters.RestaurantAdapter;
import com.pawel.p7_go4lunch.utils.adapters.WorkmateAdapter;

import java.util.ArrayList;

public class ListViewFragment extends Fragment implements RestaurantAdapter.OnItemRestaurantListClickListener {

    private FragmentListViewBinding mBinding;
    private ListViewViewModel mListViewVM;
    private ArrayList<Restaurant> mRestaurants;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mListViewVM = new ViewModelProvider(this).get(ListViewViewModel.class);
        mListViewVM.init();
        // TODO mListViewVM.getRestaurants();
        setProgressBar();
        setRecyclerView();
        mBinding = FragmentListViewBinding.inflate(inflater, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        mListViewViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return mBinding.getRoot();
    }

    private void setProgressBar() { mBinding.restaurantListProgressBar.setVisibility(View.VISIBLE); }

    private void setRecyclerView() {
        if (mRestaurants.isEmpty()) {
            mBinding.restaurantListProgressBar.setVisibility(View.GONE);
            mBinding.restaurantErrorNonData.setVisibility(View.VISIBLE);
        } else {
            RestaurantAdapter adapter = new RestaurantAdapter(mRestaurants ,this);
            mBinding.restaurantListProgressBar.setVisibility(View.GONE);
            mBinding.restaurantRecyclerView.setAdapter(adapter);
            mBinding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    @Override
    public void onItemRestaurantListClick(int position) {
        Restaurant restaurant = mRestaurants.get(position);
        Intent intent = new Intent(getActivity(), AboutRestaurantActivity.class);
        intent.putExtra(Const.EXTRA_KEY_RESTAURANT, restaurant.getPlaceId());
        startActivity(intent);
    }
}