package com.pawel.p7_go4lunch.ui.listView;

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

import com.pawel.p7_go4lunch.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.databinding.ErrorNoDataFullscreenMessageBinding;
import com.pawel.p7_go4lunch.databinding.FragmentListViewBinding;
import com.pawel.p7_go4lunch.databinding.ProgressBarBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.adapters.RestaurantAdapter;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import java.util.ArrayList;

public class ListViewFragment extends Fragment implements RestaurantAdapter.OnItemRestaurantListClickListener {

    private FragmentListViewBinding mBinding;
    private ProgressBarBinding progressBarBiding;
    private ErrorNoDataFullscreenMessageBinding errorBinding;
    private ListViewViewModel mListViewVM;
    private final ArrayList<Restaurant> mRestaurants = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initListViewModel();
        // TODO mRestaurants = mListViewVM.getRestaurants();
        mBinding = FragmentListViewBinding.inflate(inflater, container, false);
        bindIncludesLayouts();
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        mListViewViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        setProgressBar();
        setRecyclerView();
        return mBinding.getRoot();
    }

    private void initListViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mListViewVM = new ViewModelProvider(this, vmf).get(ListViewViewModel.class);
        mListViewVM.init();
    }

    private void bindIncludesLayouts() {
        View progressBinding = mBinding.restaurantProgressBar.getRoot();
        progressBarBiding = ProgressBarBinding.bind(progressBinding);
        View errorViewBinding = mBinding.restaurantFullscreenNoData.getRoot();
        errorBinding = ErrorNoDataFullscreenMessageBinding.bind(errorViewBinding);
    }

    private void setProgressBar() { progressBarBiding.progressBarLayout.setVisibility(View.VISIBLE); }

    private void setRecyclerView() {
        if (mRestaurants.isEmpty()) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("SEARCH", "This'll run 300 milliseconds later");
                            progressBarBiding.progressBarLayout.setVisibility(View.GONE);
                            errorBinding.errorNoData.setVisibility(View.VISIBLE);
                        }
                    },
                    1000);
        } else {
            RecyclerView recView = mBinding.restaurantRecyclerView;
            RestaurantAdapter adapter = new RestaurantAdapter(mRestaurants ,this);
            mBinding.restaurantProgressBar.progressBarLayout.setVisibility(View.GONE);
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
        errorBinding = null;
        mBinding = null;
    }
}