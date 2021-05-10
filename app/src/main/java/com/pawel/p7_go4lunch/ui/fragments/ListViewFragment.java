package com.pawel.p7_go4lunch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pawel.p7_go4lunch.databinding.MessageNoRestoBinding;
import com.pawel.p7_go4lunch.ui.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.databinding.FragmentListViewBinding;
import com.pawel.p7_go4lunch.databinding.ProgressBarBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.adapters.RestaurantAdapter;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.RestaurantsViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends Fragment implements RestaurantAdapter.OnItemRestaurantListClickListener {
    private static final String TAG = "AUTO_COM";
    private RestaurantsViewModel mRestaurantsVM;
    private FragmentListViewBinding mBinding;
    private ProgressBarBinding progressBarBiding;
    private MessageNoRestoBinding mMessageNoRestoBinding;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private AutoSearchEvents autoEvent = AutoSearchEvents.AUTO_NULL;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initVM();
        mBinding = FragmentListViewBinding.inflate(inflater, container, false);
        com.pawel.p7_go4lunch.databinding.WifiOffBinding wifiOffBinding = mBinding.listWifiOff;
        bindIncludesLayouts();
        if (LocationUtils.isWifiOn()) wifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
        else {
            setProgressBar();
            // initially the recyclerView start with list of Resto from NearBy
            isRestoReceived();
            // Then, hi listen for the change
            observeRestaurantAPIResponse();
        }
        return mBinding.getRoot();
    }

    private void initVM() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mRestaurantsVM = new ViewModelProvider(requireActivity(), vmf).get(RestaurantsViewModel.class);
        // TODO replace getRestoCache with getRestaurant() -> mRestaurant
        mRestaurants = mRestaurantsVM.getRestaurantsCache();
        setAutocompleteEventObserver();
    }

    private void setAutocompleteEventObserver() {
        mRestaurantsVM.getAutoSearchEventList().observe(getViewLifecycleOwner(), autoSearchEvents -> {
            Log.i(TAG, "ListViewFragment.setAutocompleteEventObserver.onChanged: " + autoSearchEvents);
            autoEvent = autoSearchEvents;
            if (autoSearchEvents.equals(AutoSearchEvents.AUTO_SEARCH_EMPTY)) {
                mRestaurants.clear();
                updateRecyclerView();
                displayRestoInRecyclerV();
            }
            // TODO all cases for AutoSearchEvents
            // if AUTO_ERROR display SnackBar message
            // TODO if AUTO_START
            // List.clear() ?
            // TODO if AUTO_STOP
            // call faction which get RestaurantCache and display them on Map
        });
    }

    private void bindIncludesLayouts() {
        View progressBinding = mBinding.restaurantProgressBar.getRoot();
        progressBarBiding = ProgressBarBinding.bind(progressBinding);
        View msNoRestoViewBinding = mBinding.restaurantFullscreenNoData.getRoot();
        mMessageNoRestoBinding = MessageNoRestoBinding.bind(msNoRestoViewBinding);
    }

    private void setProgressBar() {
        progressBarBiding.progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void observeRestaurantAPIResponse() {
        Log.i(TAG, "ListViewFragment.observeRestaurantAPIResponse: ");
        // TODO : check what is he listening : getRestaurants
        // Is listening: mRestaurantLiveData in GoogleRepo
        mRestaurantsVM.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            Log.i(TAG, "OB__ ListViewFragment.observeRestaurantAPIResponse: " + restaurants.size());
            mRestaurants = restaurants;
            updateRecyclerView();
        });
    }

    private void updateRecyclerView() {
        isRestoReceived();
    }

    private void isRestoReceived() {
        Log.i(TAG, "RUN _ListViewFragment.isRestoReceived: RUN");
        if (mRestaurants.isEmpty()) {
            Log.i(TAG, "isRestoReceived: if (mRestaurants.isEmpty()");
            new android.os.Handler().postDelayed(
                    () -> {
                        // This'll run 600 milliseconds later
                        if (autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
                            mMessageNoRestoBinding.messageNoResto.setVisibility(View.VISIBLE);
                        } else {
                            mMessageNoRestoBinding.messageNoResto.setVisibility(View.GONE);
                        }
                        progressBarBiding.progressBarLayout.setVisibility(View.GONE);
                    },
                    600);
        } else {
            displayRestoInRecyclerV();
        }
    }

    private void displayRestoInRecyclerV() {
        if (!mRestaurants.isEmpty() || !autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
            Log.i(TAG, "m_ displayRestoInRecyclerV: mRestaurants.isFully");

            RecyclerView recView = mBinding.restaurantRecyclerView;
            RestaurantAdapter adapter = new RestaurantAdapter(mRestaurants, this);
            recView.setAdapter(adapter);
            recView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recView.addItemDecoration(new DividerItemDecoration(recView.getContext(), DividerItemDecoration.VERTICAL));
        }
    }

    @Override
    public void onItemRestaurantListClick(int position) {
        Restaurant restaurant = mRestaurants.get(position);
        Intent intent = new Intent(getActivity(), AboutRestaurantActivity.class);
        intent.putExtra(Const.EXTRA_KEY_RESTAURANT, restaurant.getPlaceId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBarBiding = null;
        mMessageNoRestoBinding = null;
        mBinding = null;
    }
}