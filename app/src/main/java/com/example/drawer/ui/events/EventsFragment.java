package com.example.drawer.ui.events;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.drawer.R;
import com.example.drawer.RetroInstance;
import com.example.drawer.databinding.FragmentEventsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

        SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {
                super.onRightClicked(position);

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                EventsDataService eventsApi = retrofit.create(EventsDataService.class);

                Call<Event> call = eventsApi.toggleEvent(eventsList.get(position));

                call.enqueue(new Callback<Event>() {
                    @Override
                    public void onResponse(Call<Event> call, Response<Event> response) {
                        if(response.isSuccessful()){
                            eventsList.set(position, (Event)response.body());
                            eventsAdapter.notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onFailure(Call<Event> call, Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public void onRightClicked(int position) {
                super.onLeftClicked(position);

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                EventsDataService eventsApi = retrofit.create(EventsDataService.class);

                Call<Event> call = eventsApi.deleteEvent(eventsAdapter.eventsList.get(position).getEventId());

                call.enqueue(new Callback<Event>() {
                    @Override
                    public void onResponse(Call<Event> call, Response<Event> response) {
                        if(response.isSuccessful()){
                            eventsAdapter.eventsList.remove(position);
                            eventsAdapter.notifyItemRemoved(position);
                        }
                    }

                    @Override
                    public void onFailure(Call<Event> call, Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }
        }, getContext());

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
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