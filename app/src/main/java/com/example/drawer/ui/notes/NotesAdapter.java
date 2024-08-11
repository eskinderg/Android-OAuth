package com.example.drawer.ui.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drawer.R;
import com.example.drawer.TimeAgo2;

import java.util.ArrayList;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteRecyclerViewHolder>{

    public interface OnNoteItemClickListener {
        void onNoteItemClick(View view, Note note);
    }

    Context context;
    ArrayList<Note> notesList;
    private final OnNoteItemClickListener mListener;

    public NotesAdapter(Context context, ArrayList<Note> notesList, OnNoteItemClickListener listener) {
        this.notesList = notesList;
        this.context = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public NoteRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.note_list,parent, false);
        return new NoteRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteRecyclerViewHolder holder, int position) {

        Note noteItem = notesList.get(position);

        if(noteItem.getHeader() == null || noteItem.getHeader().isEmpty()) {
            holder.header.setText("Untitled");
        } else {
            holder.header.setText(noteItem.getHeader());
        }

        holder.description.setText("Modified " + TimeAgo2.covertTimeToText(noteItem.getDateModified()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNoteItemClick(v, noteItem );
            }
        });

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView header;
        TextView description;
        CardView card;

        public NoteRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            description = itemView.findViewById(R.id.description);
            card = itemView.findViewById(R.id.card);
        }
    }
}