package com.example.drawer.ui.notes.archived;

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
import com.example.drawer.ui.notes.Note;

import java.util.ArrayList;
import java.util.List;


public class ArchivedNotesAdapter extends RecyclerView.Adapter<ArchivedNotesAdapter.ArchivedNoteRecyclerViewHolder>{

    public interface OnNoteItemClickListener {
        void onNoteItemClick(View view, Note note);
    }

    Context context;
    public ArrayList<Note> notesList;
    private final OnNoteItemClickListener mListener;

    public ArchivedNotesAdapter(Context context, List<Note> notesList, OnNoteItemClickListener listener) {
        this.notesList = getArchivedNotes(notesList);
        this.context = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ArchivedNoteRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.archived_note_list,parent, false);
        return new ArchivedNoteRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivedNoteRecyclerViewHolder holder, int position) {

        Note noteItem = notesList.get(position);

        if(noteItem.getHeader() == null || noteItem.getHeader().isEmpty()) {
            holder.header.setText("Untitled");
        } else {
            holder.header.setText(noteItem.getHeader());
        }

        holder.txtDateModified.setText("Last modified " + TimeAgo2.covertTimeToText(noteItem.getDateModified()));
        holder.txtDateArchived.setText("Archived " + TimeAgo2.covertTimeToText(noteItem.getDateArchived()));

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

    public static class ArchivedNoteRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView header;
        TextView txtDateModified;
        TextView txtDateArchived;
        CardView card;

        public ArchivedNoteRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            txtDateModified = itemView.findViewById(R.id.txtdatemodified);
            txtDateArchived = itemView.findViewById(R.id.txtdatearchived);
            card = itemView.findViewById(R.id.card);
        }
    }

    private ArrayList<Note> getArchivedNotes(List<Note> list){
        List<Note> archivedNotes = list.stream().filter(n -> n.isArchived()).toList();
        return new ArrayList<Note>(archivedNotes);
    }
}