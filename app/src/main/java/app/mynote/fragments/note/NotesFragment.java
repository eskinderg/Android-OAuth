package app.mynote.fragments.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Arrays;
import java.util.List;

import app.mynote.core.callback.AppCallback;
import app.mynote.core.utils.GsonParser;
import app.mynote.fragments.SwipeController;
import app.mynote.fragments.note.NotesAdapter.OnNoteItemClickListener;
import app.mynote.service.RetroInstance;
import mynote.R;
import mynote.databinding.FragmentNotesBinding;
import retrofit2.Call;
import retrofit2.Retrofit;

public class NotesFragment extends Fragment implements OnNoteItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public RecyclerView recyclerView;
    public FloatingActionButton fab;
    public NotesAdapter notesAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentNotesBinding binding;

    public NotesFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.noterecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                                Retrofit retrofit = RetroInstance.getRetrofitInstance();
                                NotesDataService notesDataService = retrofit.create(NotesDataService.class);
                                Note noteItem = notesAdapter.notesList.get(position);
                                noteItem.setPinned(!noteItem.isPinned());
                                Call<Note> call = notesDataService.updateNote(noteItem);
                                call.enqueue(new AppCallback<Note>(getContext()) {
                                    @Override
                                    public void onResponse(Note response) {
                                        Toast.makeText(getContext(), "Updated", Toast.LENGTH_LONG).show();
                                        notesAdapter.notifyItemChanged(position);
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                    }
                                });
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
                                Retrofit retrofit = RetroInstance.getRetrofitInstance();
                                NotesDataService notesDataService = retrofit.create(NotesDataService.class);
                                Note noteItem = notesAdapter.notesList.get(position);
                                noteItem.setArchived(true);
                                Call<Note> call = notesDataService.updateNote(noteItem);
                                call.enqueue(new AppCallback<Note>(getContext()) {
                                    @Override
                                    public void onResponse(Note response) {
                                        notesAdapter.notesList.remove(position);
                                        notesAdapter.notifyItemRemoved(position);
                                        Toast.makeText(getContext(), "Note archived", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                    }
                                });
                            }
                        }
                ));

            }
        };

        this.fab = view.findViewById(R.id.fab);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = RetroInstance.getRetrofitInstance();
                NotesDataService notesApi = retrofit.create(NotesDataService.class);
                Call<Note> call = notesApi.addNote(new Note());
                call.enqueue(new AppCallback<Note>(getContext()) {
                    @Override
                    public void onResponse(Note response) {
                        Note newNote = response;
                        Bundle bundle = new Bundle();
                        String noteJsonString = GsonParser.getGsonParser().toJson(newNote);
                        bundle.putString("note", noteJsonString);
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                });

            }
        });

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
        Bundle bundle = new Bundle();
        String noteJsonString = GsonParser.getGsonParser().toJson(note);
        bundle.putString("note", noteJsonString);

        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);

//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .add(R.id.nav_host_fragment_content_main, NoteFragment.class, bundle)
//                .addToBackStack("notes")
//                .commit();

//        FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.nav_host_fragment_content_main,frag);
//        fragmentTransaction.commit();

        //Put the value
//        NoteFragment ldf = new NoteFragment();
//        Bundle args = new Bundle();
//        args.putString("YourKey", "YourValue");
//        ldf.setArguments(args);
//
//        getFragmentManager().beginTransaction().add(R.id.nav_host_fragment_content_main, ldf).commit();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchNotes();
    }

    private void fetchNotes() {
        Retrofit retrofit = RetroInstance.getRetrofitInstance();
        NotesDataService notesApi = retrofit.create(NotesDataService.class);
        Call<Note[]> call = notesApi.getNotes();
        call.enqueue(new AppCallback<Note[]>(getContext()) {
            @Override
            public void onResponse(Note[] response) {
                NotesFragment.this.dataView(new ArrayList(Arrays.asList(response)));
                mSwipeRefreshLayout.setRefreshing(false);
                setAppbarCount();
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }


    private void dataView(List<Note> notes) {
        this.notesAdapter = new NotesAdapter(getContext(), notes, this);
        recyclerView.setAdapter(this.notesAdapter);
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
    }

    private void setAppbarCount() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notes " + "(" + NotesFragment.this.recyclerView.getAdapter().getItemCount() + ")");
    }
}
