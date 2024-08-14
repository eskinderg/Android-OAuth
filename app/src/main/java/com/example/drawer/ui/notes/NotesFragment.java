package com.example.drawer.ui.notes;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.drawer.R;
import com.example.drawer.databinding.FragmentNotesBinding;
import com.example.drawer.service.RetroInstance;
import com.example.drawer.ui.notes.NotesAdapter.OnNoteItemClickListener;
import com.example.drawer.utils.GsonParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NotesFragment extends Fragment implements OnNoteItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public RecyclerView recyclerView;
    public FloatingActionButton fab;
    public NotesAdapter notesAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private FragmentNotesBinding binding;
    public NotesFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotesViewModel notesViewModel =
                new ViewModelProvider(this).get(NotesViewModel.class);

        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.noterecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        NoteSwipeController swipeController = new NoteSwipeController(new NoteSwipeControllerActions() {
            @Override
            public void onEditBtnClicked(int position) {
                super.onEditBtnClicked(position);

                Bundle bundle = new Bundle();
                String noteJsonString = GsonParser.getGsonParser().toJson(notesAdapter.notesList.get(position));
                bundle.putString("note", noteJsonString);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);
            }

            @Override
            public void onArchiveBtnClicked(int position) {
                super.onArchiveBtnClicked(position);

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                NotesDataService notesDataService = retrofit.create(NotesDataService.class);

                Note noteItem = notesAdapter.notesList.get(position);
                noteItem.setArchived(true);

                Call<Note> call = notesDataService.updateNote(noteItem);
                call.enqueue(new Callback<Note>() {
                    @Override
                    public void onResponse(Call<Note> call, Response<Note> response) {
                        if (response.isSuccessful()) {
                            notesAdapter.notesList.remove(position);
                            notesAdapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Note archived", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Note> call, Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
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
        this.fab = view.findViewById(R.id.fab);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Retrofit retrofit = RetroInstance.getRetrofitInstance();

                NotesDataService notesApi = retrofit.create(NotesDataService.class);

                Call<Note> call = notesApi.addNote(new Note());

                call.enqueue(new Callback<Note>() {
                    @Override
                    public void onResponse(Call<Note> call, Response<Note> response) {
                        Note newNote = response.body();
                        Bundle bundle = new Bundle();
                        String noteJsonString = GsonParser.getGsonParser().toJson(newNote);
                        bundle.putString("note", noteJsonString);

//                        NotesFragment.this.notesList.add(0, response.body());
//                        notesAdapter.notifyDataSetChanged();

                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_nav_notes_to_nav_note, bundle);
                    }

                    @Override
                    public void onFailure(Call<Note> call, Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

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

//        NoteFragment frag = new NoteFragment(note);
//        FragmentManager fragmentManager = getFragmentManager();
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

        call.enqueue(new Callback<Note[]>() {
            @Override
            public void onResponse(@NonNull Call<Note[]> call, @NonNull Response<Note[]> response) {
                if(response.isSuccessful()) {
                    NotesFragment.this.dataView(new ArrayList(Arrays.asList(response.body())));
                    mSwipeRefreshLayout.setRefreshing(false);
                    setAppbarCount();
                } else {
                    Toast.makeText(getContext(), response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Note[]> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notes " + "(" + String.valueOf(NotesFragment.this.recyclerView.getAdapter().getItemCount()) + ")");
    }

}
