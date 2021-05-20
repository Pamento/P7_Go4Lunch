package com.pawel.p7_go4lunch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.MessageNoRestoBinding;
import com.pawel.p7_go4lunch.ui.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.databinding.FragmentListViewBinding;
import com.pawel.p7_go4lunch.databinding.ProgressBarBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.ui.MainActivity;
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
    private View view;
    private MainActivity mMainActivity;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private RestaurantAdapter adapter;
    private AutoSearchEvents autoEvent = AutoSearchEvents.AUTO_NULL;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initVM();
        mBinding = FragmentListViewBinding.inflate(inflater, container, false);
        view = mBinding.getRoot();
        bindIncludesLayouts();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: super.onViewCreated is inactive");
        Log.i(TAG, "LVF__ onViewCreated: MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.updateMenuItems(false);
        setAutocompleteEventObserver();
        com.pawel.p7_go4lunch.databinding.WifiOffBinding wifiOffBinding = mBinding.listWifiOff;
        if (LocationUtils.isWifiOn()) wifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
        else {
            Log.i(TAG, "LVF__ onViewCreated: setProgressBar()");
            Log.i(TAG, "LVF__ onViewCreated: observeRestaurantAPIResponse()");
            setProgressBar();
            // initially the recyclerView start with list of Resto from NearBy
            //isRestoReceived();
            // Then, hi listen for the change
            observeRestaurantAPIResponse();
        }
        setRecyclerView();
    }

    private void initVM() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mRestaurantsVM = new ViewModelProvider(requireActivity(), vmf).get(RestaurantsViewModel.class);
        mRestaurantsVM.init();
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(getActivity(), Html.fromHtml("<font color='#FF5721' ><b>" + msg + "</b></font>"), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private void showToastRestosNr() {
        Log.i(TAG, "setAutocompleteEventObserver: " + mRestaurants.size());
        String restoFound = view.getResources().getString(R.string.number_resto_found, mRestaurants.size());
        showToast(restoFound);
    }

    private void setAutocompleteEventObserver() {
        mRestaurantsVM.getAutoSearchEventList().observe(getViewLifecycleOwner(), autoSearchEvents -> {
            autoEvent = autoSearchEvents;
            switch (autoSearchEvents) {
                case AUTO_START:
                case AUTO_SEARCH_EMPTY:
                    mRestaurants.clear();
                    updateRecyclerView();
                    break;
                case AUTO_ZERO_RESULT:
                    String msg = getString(R.string.search_no_resto_ms);
                    showToast(msg);
                    break;
                case AUTO_ERROR:
                    String msgE = getString(R.string.fetching_data_error_ms);
                    showToast(msgE);
                    break;
                case AUTO_OK:
                    String restoFound = view.getResources().getString(R.string.number_resto_found, mRestaurants.size());
                    showToast(restoFound);
                    break;
                case AUTO_STOP:
                    mRestaurantsVM.getRestosFromCacheOrNetwork();
                    break;
                default:
                    break;
            }
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

    final Observer<List<Restaurant>> observerRestos = restaurants -> {
        Log.i(TAG, "LVM__ OB_ observeRestaurantAPIResponse: " + restaurants.size());
        mRestaurants = restaurants;
        if (autoEvent.equals(AutoSearchEvents.AUTO_OK)) showToastRestosNr();
        updateRecyclerView();
    };

    private void observeRestaurantAPIResponse() {
        Log.i(TAG, "LVM__.observeRestaurantAPIResponse: ");
        mRestaurantsVM.getRestaurantWithUsers().observe(getViewLifecycleOwner(), observerRestos);
//        mRestaurantsVM.getRestaurantWithUsers.observe(getViewLifecycleOwner(), observerRestos);
    }

    private void unsubscribeRestaurants() {
        mRestaurantsVM.getRestaurantWithUsers().removeObserver(observerRestos);
    }

    private void updateRecyclerView() {
        isRestoReceived();
    }

    private void isRestoReceived() {
        Log.i(TAG, "RUN LVM__.isRestoReceived: RUN");
        if (mRestaurants.isEmpty() && autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
            progressBarBiding.progressBarLayout.setVisibility(View.GONE);
            mMessageNoRestoBinding.messageNoResto.setVisibility(View.VISIBLE);
        } else {
            Log.i(TAG, "LVM__ isRestoReceived: mRestaurants.isFULL");
            progressBarBiding.progressBarLayout.setVisibility(View.GONE);
            mMessageNoRestoBinding.messageNoResto.setVisibility(View.GONE);
            displayRestoInRecyclerV();
        }
    }

    private void setRecyclerView() {
        Log.i(TAG, "LVF__ setRecyclerView");
        RecyclerView recView = mBinding.restaurantRecyclerView;
        adapter = new RestaurantAdapter(mRestaurants, this);
        recView.setAdapter(adapter);
        recView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recView.addItemDecoration(new DividerItemDecoration(recView.getContext(), DividerItemDecoration.VERTICAL));
    }

    private void displayRestoInRecyclerV() {
        if (!mRestaurants.isEmpty() || !autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
            Log.i(TAG, "LVM__ m_ displayRestoInRecycler: mRestaurants.isFully");
            Log.i(TAG, "LVM__ m_ displayRestoInRecycler: mRestaurants.size() " + mRestaurants.size());
            Log.i(TAG, "LVM__ m_ displayRestoInRecycler: AutoSearchEvent " + autoEvent);
            adapter.updateList(mRestaurants);

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
    public void onStop() {
        Log.i(TAG, "LVF__ onStop: UNSUBSCRIBE observable RESTO");
        unsubscribeRestaurants();
        mRestaurantsVM.unsubscribeRestoWithUsers();
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "LVF__ onPause: unsubscribe observer ?");
        //unsubscribeRestaurants();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBarBiding = null;
        mMessageNoRestoBinding = null;
        mBinding = null;
    }
}