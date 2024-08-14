package com.example.drawer.ui.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drawer.R;
import com.example.drawer.service.RetroInstance;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventRecyclerViewHolder> {

    private final OnEventItemClickListener mListener;
    public ArrayList<Event> eventsList;
    Context context;
    public EventsAdapter(Context context, ArrayList<Event> eventsList, OnEventItemClickListener listener) {
        this.eventsList = eventsList;
        this.context = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public EventRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.event_list, parent, false);
        return new EventRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventRecyclerViewHolder holder, int position) {

        Event eventItem = eventsList.get(position);

        holder.title.setText(eventItem.getTitle());
        holder.chkComplete.setChecked(eventItem.getIsComplete());

        holder.chkComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                EventsDataService eventsApi = retrofit.create(EventsDataService.class);

                Call<Event> call = eventsApi.toggleEvent(eventItem);

                call.enqueue(new Callback<Event>() {
                    @Override
                    public void onResponse(Call<Event> call, Response<Event> response) {
                        if (response.isSuccessful()) {
                            eventItem.setIsComplete(response.body().getIsComplete());
                        }
                    }

                    @Override
                    public void onFailure(Call<Event> call, Throwable t) {
                        Toast.makeText(context.getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public interface OnEventItemClickListener {
        void onEventItemClick(View view, Event event);
    }

    public static class EventRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        CheckBox chkComplete;
        CardView card;

        public EventRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            chkComplete = itemView.findViewById(R.id.checkbox_complete);
            card = itemView.findViewById(R.id.card);

            chkComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });
        }
    }
}
