package app.mynote.fragments.note;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.mynote.core.db.NoteContract;
import app.mynote.core.db.NoteSyncAdapter;
import app.mynote.core.utils.AppDate;
import app.mynote.core.utils.GsonParser;
import app.mynote.fragments.SwipeController;
import app.mynote.fragments.note.NotesAdapter.OnNoteItemClickListener;
import mynote.R;
import mynote.databinding.FragmentNotesBinding;

public class NotesFragment extends Fragment implements OnNoteItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public RecyclerView recyclerView;
    public FloatingActionButton fab;
    public NotesAdapter notesAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentNotesBinding binding;
    private NoteObserver noteObserver;

    public NotesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteObserver = new NoteObserver();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        this.recyclerView = view.findViewById(R.id.noterecyclerview);
        NoteService noteService = new NoteService(getContext());
        ArrayList<Note> notes = new ArrayList<>(noteService.getAllNotes());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.notesAdapter = new NotesAdapter(getContext(),notes, this);
        this.recyclerView.setAdapter(this.notesAdapter);
        this.notesAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
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
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        SwipeController swipeHelper = new SwipeController(getContext(), recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeController.UnderlayButton(
                        "Pin",
                        SwipeController.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_pin_white),
                        ContextCompat.getColor(getContext(), R.color.primary_light),
                        true,
                        new SwipeController.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                Note noteItem = notesAdapter.notesList.get(position);
                                noteItem.setPinned(!noteItem.isPinned());
                                noteItem.setPinOrder(AppDate.Now());
                                NoteService noteService = new NoteService(getContext());
                                noteService.update(noteItem, false);
                                String textMsg = noteItem.isPinned() ? "Pinned" : "Un Pinned";
                                Toast.makeText(getContext(), "Note " + textMsg, Toast.LENGTH_LONG).show();
                                notesAdapter.notifyItemChanged(position);
                            }
                        }
                ));
                underlayButtons.add(new SwipeController.UnderlayButton(
                        "Archive",
                        SwipeController.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_archive),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        new SwipeController.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                Note noteItem = notesAdapter.notesList.get(position);
                                noteItem.setArchived(true);
                                noteItem.setDateArchived(AppDate.Now());
                                NoteService noteService = new NoteService(getContext());
                                noteService.update(noteItem, false);
                                String textMsg = "archived";
                                Toast.makeText(getContext(), "Note " + textMsg, Toast.LENGTH_LONG).show();
                                notesAdapter.notesList.remove(position);
                                notesAdapter.notifyItemRemoved(position);
                            }
                        }
                ));

            }
        };

        this.fab = view.findViewById(R.id.fab);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteService noteService = new NoteService(getContext());
                Note note = new Note();

                note.setId(UUID.randomUUID().toString());
                note.setDateModified(AppDate.Now());
                noteService.add(note);

                Bundle bundle = new Bundle();
                String noteJsonString = GsonParser.getGsonParser().toJson(note);
                bundle.putString("note", noteJsonString);
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);
            }
        });

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light,
                R.color.primary_light);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        setAppbarCount();
        getActivity().getContentResolver().registerContentObserver(
                NoteContract.Notes.CONTENT_URI,
                true,
                noteObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (noteObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(noteObserver);
        }
    }


    @Override
    public void onNoteItemClick(View view, Note note) {
        Bundle bundle = new Bundle();
        String noteJsonString = GsonParser.getGsonParser().toJson(note);
        bundle.putString("note", noteJsonString);

        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        NoteSyncAdapter.performSync();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchNotes() {
        NoteService noteService = new NoteService(getContext());
        ArrayList<Note> notes = new ArrayList<>(noteService.getAllNotes());
        NotesFragment.this.dataView(notes);
        setAppbarCount();
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    private void dataView(List<Note> notes) {
        this.notesAdapter = new NotesAdapter(getContext(), notes, this);
        recyclerView.setAdapter(this.notesAdapter);
    }

    private void setAppbarCount() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notes " + "(" + NotesFragment.this.recyclerView.getAdapter().getItemCount() + ")");
    }

    private final class NoteObserver extends ContentObserver {
        private NoteObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            fetchNotes();
        }
    }
}
