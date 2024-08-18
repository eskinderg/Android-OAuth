package app.mynote.fragments.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.mynote.core.callback.AppCallback;
import app.mynote.service.RetroInstance;
import mynote.R;
import retrofit2.Call;
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

                call.enqueue(new AppCallback<Event>(EventsAdapter.this.context) {
                    @Override
                    public void onResponse(Event response) {
                        eventItem.setIsComplete(response.getIsComplete());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
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
