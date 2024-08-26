package app.mynote.fragments.note.pinned;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import app.mynote.core.db.NoteSyncAdapter;
import app.mynote.core.utils.GsonParser;
import app.mynote.fragments.SwipeController;
import app.mynote.fragments.note.Note;
import app.mynote.fragments.note.NoteService;
import mynote.R;
import mynote.databinding.FragmentPinBinding;

public class PinNoteListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, PinNotesAdapter.OnPinNoteItemClickListener {

    public RecyclerView recyclerView;
    public PinNotesAdapter pinAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentPinBinding binding;
    private FloatingActionButton fab;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPinBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.noterecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        this.fab = view.findViewById(R.id.fab);
        this.fab.setVisibility(View.INVISIBLE);

        SwipeController swipeController = new SwipeController(getContext(), recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(
                        "Un Pin",
                        SwipeController.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_pin_white),
                        Color.GRAY,
                        new UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                Note noteItem = pinAdapter.notesList.get(position);
                                noteItem.setPinned(false);
                                NoteService noteService = new NoteService(getContext());
                                noteService.update(noteItem);
                                Toast.makeText(getContext(), "Updated", Toast.LENGTH_LONG).show();
                                pinAdapter.notesList.remove(position);
                                pinAdapter.notifyItemRemoved(position);
                            }
                        }
                ));

            }
        };
    }

    @Override
    public void onRefresh() {
        NoteSyncAdapter.cancelSync();
        NoteSyncAdapter.performSync();
        recyclerView.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayout.setRefreshing(true);
        fetchNotes();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void fetchNotes() {
        NoteService noteService = new NoteService(getContext());
        ArrayList<Note> notes = new ArrayList<>(noteService.getPinned());
        PinNoteListFragment.this.dataView(notes);
        setAppbarCount();
        mSwipeRefreshLayout.setRefreshing(false);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);
    }


    private void dataView(List<Note> notes) {
        this.pinAdapter = new PinNotesAdapter(getContext(), notes, this);
        recyclerView.setAdapter(this.pinAdapter);
        this.pinAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Pinned " + "(" + PinNoteListFragment.this.recyclerView.getAdapter().getItemCount() + ")");
    }

    @Override
    public void onPinNoteItemClick(View view, Note note) {
        Bundle bundle = new Bundle();
        String noteJsonString = GsonParser.getGsonParser().toJson(note);
        bundle.putString("note", noteJsonString);

        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_nav_pin_to_nav_pin_edit, bundle);
    }

}
