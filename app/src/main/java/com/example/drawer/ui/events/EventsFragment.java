package com.example.drawer.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.drawer.Constants;
import com.example.drawer.R;
import com.example.drawer.databinding.FragmentEventsBinding;
import com.example.drawer.ui.notes.EventsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventsFragment extends Fragment implements EventsAdapter.OnEventItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public ArrayList<Event> eventsList;
    public RecyclerView recyclerView;
    public EventsAdapter eventsAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    private FragmentEventsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventsViewModel eventsViewModel =
                new ViewModelProvider(this).get(EventsViewModel.class);

        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.eventsrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.teal_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                fetchEvents();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchEvents();
    }

    @Override
    public void onEventItemClick(View view, Event event) {

    }

    private void fetchEvents() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventsDataService eventsApi = retrofit.create(EventsDataService.class);

        Call<Event[]> call = eventsApi.getEvents("Bearer " + Constants.ACCESS_TOKEN);

        call.enqueue(new Callback<Event[]>() {
            @Override
            public void onResponse(@NonNull Call<Event[]> call, @NonNull Response<Event[]> response) {
                mSwipeRefreshLayout.setRefreshing(false);
                eventsList = new ArrayList(Arrays.asList(response.body()));
                EventsFragment.this.dataView(eventsList);
            }

            @Override
            public void onFailure(@NonNull Call<Event[]> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void dataView(List<Event> events) {
        this.eventsAdapter = new EventsAdapter(getContext(), this.eventsList, this);
        recyclerView.setAdapter(this.eventsAdapter);
    }
}