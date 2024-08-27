package app.mynote.fragments.note.archived;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import app.mynote.core.db.NoteSyncAdapter;
import app.mynote.fragments.SwipeController;
import app.mynote.fragments.note.Note;
import app.mynote.fragments.note.NoteService;
import mynote.R;
import mynote.databinding.FragmentArchivedNotesBinding;

public class ArchivedNotesFragment extends Fragment implements ArchivedNotesAdapter.OnNoteItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public RecyclerView recyclerView;
    public ArchivedNotesAdapter notesAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentArchivedNotesBinding binding;

    public ArchivedNotesFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentArchivedNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.archivednoterecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SwipeController swipeController = new SwipeController(getContext(), recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(
                        "Restore",
                        SwipeController.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_restore_white),
                        ContextCompat.getColor(getContext(), R.color.green),
                        new UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                Note noteItem = notesAdapter.notesList.get(position);
                                noteItem.setArchived(false);
                                NoteService noteService = new NoteService(getContext());
                                noteService.update(noteItem, false);
                                String textMsg = "restored";
                                Toast.makeText(getContext(), "Note " + textMsg, Toast.LENGTH_LONG).show();
                                notesAdapter.notesList.remove(position);
                                notesAdapter.notifyItemRemoved(position);
                            }
                        }

                ));

            }
        };

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                fetchNotes();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onNoteItemClick(View view, Note note) {
    }

    @Override
    public void onRefresh() {
        NoteSyncAdapter.cancelSync();
        NoteSyncAdapter.performSync();
        recyclerView.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayout.setRefreshing(true);
        fetchNotes();
    }

    private void fetchNotes() {
        NoteService noteService = new NoteService(getContext());
        ArrayList<Note> notes = new ArrayList<>(noteService.getArchived());
        ArchivedNotesFragment.this.dataView(notes);
        setAppbarCount();
        mSwipeRefreshLayout.setRefreshing(false);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void dataView(List<Note> notes) {
//        List<Note> list = notes.stream().filter(n -> !n.isArchived()).toList();
        this.notesAdapter = new ArchivedNotesAdapter(getContext(), notes, this);
        recyclerView.setAdapter(this.notesAdapter);
        this.notesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setAppbarCount();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                setAppbarCount();
            }
        });
    }

    private void setAppbarCount() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Archived " + "(" + ArchivedNotesFragment.this.notesAdapter.notesList.size() + ") ");
    }

}