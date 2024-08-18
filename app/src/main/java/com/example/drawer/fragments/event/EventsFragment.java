package com.example.drawer.fragments.event;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.drawer.core.callback.AppCallback;
import com.example.drawer.R;
import com.example.drawer.databinding.FragmentEventsBinding;
import com.example.drawer.service.RetroInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

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

        EventsSwipeController eventsSwipeController = new EventsSwipeController(new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {
                super.onRightClicked(position);

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                EventsDataService eventsApi = retrofit.create(EventsDataService.class);

                Call<Event> call = eventsApi.toggleEvent(eventsList.get(position));

                call.enqueue(new AppCallback<Event>(getContext()) {
                    @Override
                    public void onResponse(Event response) {
                        eventsList.set(position, (Event) response);
                        eventsAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });
            }

            @Override
            public void onRightClicked(int position) {
                super.onLeftClicked(position);

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                EventsDataService eventsApi = retrofit.create(EventsDataService.class);

                Call<Event> call = eventsApi.deleteEvent(eventsAdapter.eventsList.get(position).getEventId());

                call.enqueue(new AppCallback<Event>(getContext()) {
                    @Override
                    public void onResponse(Event response) {
                        eventsAdapter.eventsList.remove(position);
                        eventsAdapter.notifyItemRemoved(position);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });
            }
        }, getContext());

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(eventsSwipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                eventsSwipeController.onDraw(c);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light);

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

        Retrofit retrofit = RetroInstance.getRetrofitInstance();

        EventsDataService eventsApi = retrofit.create(EventsDataService.class);

        Call<Event[]> call = eventsApi.getEvents();

        call.enqueue(new AppCallback<Event[]>(getContext()) {
            @Override
            public void onResponse(Event[] response) {
                mSwipeRefreshLayout.setRefreshing(false);
                eventsList = new ArrayList(Arrays.asList(response));
                EventsFragment.this.dataView(eventsList);
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    private void dataView(List<Event> events) {
        this.eventsAdapter = new EventsAdapter(getContext(), this.eventsList, this);
        recyclerView.setAdapter(this.eventsAdapter);
    }
}