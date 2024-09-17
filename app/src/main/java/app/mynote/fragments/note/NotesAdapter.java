package app.mynote.fragments.note;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import app.mynote.core.utils.Time2Ago;
import mynote.R;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteRecyclerViewHolder> {

    private final OnNoteItemClickListener mListener;
    public ArrayList<Note> notesList;
    Context context;

    public NotesAdapter(Context context, List<Note> notesList, OnNoteItemClickListener listener) {
        this.context = context;
        this.notesList = getActiveNotes(notesList);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public NoteRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.note_list, parent, false);
        NoteRecyclerViewHolder holder = new NoteRecyclerViewHolder(view);
        return holder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteRecyclerViewHolder holder, int position) {

        Note noteItem = notesList.get(position);

        if (noteItem.getPinned()) {
            holder.header.setTypeface(Typeface.DEFAULT_BOLD);
//            holder.imgPinned.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.MULTIPLY);
            holder.imgPinned.setVisibility(View.VISIBLE);
        }

        if (noteItem.getHeader() == null || noteItem.getHeader().isEmpty()) {
            holder.header.setText("");
        } else {
            holder.header.setText(noteItem.getHeader());
        }

        holder.description.setText("Modified " + Time2Ago.covertTimeToText(noteItem.getDateModified().toString()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNoteItemClick(v, noteItem);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    private ArrayList<Note> getActiveNotes(List<Note> list) {
        List<Note> activeNotes = list.stream().filter(n -> !n.getArchived()).collect(Collectors.toList());

        activeNotes.sort(new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                return o2.getDateModified().compareTo(o1.getDateModified());
            }
        });

        activeNotes.sort(new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                if(o2.isPinned() || o1.isPinned())
                    return Boolean.compare(o2.isPinned(), o1.isPinned());
                return 0;
            }
        });

        activeNotes.sort(new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                if(o1.isPinned() && o2.isPinned())
                    return o1.getPinOrder().compareTo(o2.getPinOrder());
                return 0;
            }
        });

        return new ArrayList<Note>(activeNotes);
    }

    public interface OnNoteItemClickListener {
        void onNoteItemClick(View view, Note note);
    }

    public static class NoteRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView header;
        TextView description;
        ImageView imgPinned;
        CardView card;

        public NoteRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            description = itemView.findViewById(R.id.description);
            card = itemView.findViewById(R.id.card);
            imgPinned = itemView.findViewById(R.id.imgPinned);
        }
    }
}