package com.example.drawer.ui.notes;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.drawer.Constants;
import com.example.drawer.R;
import com.example.drawer.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class NoteFragment extends Fragment implements Callback<Note> {
    public EditText txtNoteText;
    public EditText txtNoteHeader;

    Note note;

    public NoteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Bundle args = getArguments();
        String personJsonString = args.getString("note");
        this.note = Utils.getGsonParser().fromJson(personJsonString, Note.class);

        this.txtNoteText = view.findViewById(R.id.txtNoteText);
        this.txtNoteHeader = view.findViewById(R.id.txtNoteHeader);

        if(this.note.getHeader() != null) {
            txtNoteHeader.setText(this.note.getHeader());
        }

        if(this.note.getText() != null) {
            NoteFragment.this.txtNoteText.setText(Html.fromHtml(this.note.getText(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV).toString());
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(this.note.getHeader());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

//        AppBarMainBinding appBarMainBinding = view.findViewById(R.id.app_bar_main)
//        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
//        DrawerLayout drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar , R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    }

    @Override
    public void onResponse(Call<Note> call, Response<Note> response) {
        Toast.makeText(getContext(),"Saved", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(Call<Note> call, Throwable t) {
        Toast.makeText(getContext(),t.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.note, menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_save) {
            note.setText(Html.toHtml(txtNoteText.getText(),Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV));
            note.setHeader(txtNoteHeader.getText().toString());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            NotesDataService notesApi = retrofit.create(NotesDataService.class);
            Call<Note> call = notesApi.updateNote("Bearer " + Constants.ACCESS_TOKEN, NoteFragment.this.note);
            call.enqueue(NoteFragment.this);
            return true;
        }

        if(item.getItemId() == 16908332) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_note_to_nav_notes );
            return true;
        }

        return super.onContextItemSelected(item);

    }
}