package com.example.drawer.ui.notes;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.drawer.Constants;
import com.example.drawer.Note;
import com.example.drawer.NotesDataService;
import com.example.drawer.R;
import com.example.drawer.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class NoteFragment extends Fragment implements Callback<Note> {

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

        EditText txtNote = view.findViewById(R.id.txtEdit);
        txtNote.setText(Html.fromHtml(this.note.getText(), Html.FROM_HTML_MODE_COMPACT).toString());
//
        txtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

//                NoteActivity.this.note.setText(NoteActivity.this.txtNote.getText().toString());
                NoteFragment.this.note.setText(Html.toHtml(txtNote.getText(),Html.FROM_HTML_MODE_COMPACT));

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                NotesDataService notesApi = retrofit.create(NotesDataService.class);
                Call<Note> call = notesApi.updateNote("Bearer " + Constants.ACCESS_TOKEN, NoteFragment.this.note);
                call.enqueue(NoteFragment.this);
            }
        });
    }

    @Override
    public void onResponse(Call<Note> call, Response<Note> response) {
        Toast.makeText(getContext(),"Success", Toast.LENGTH_LONG).show();
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
        if(item.getItemId() == R.id.action_back) {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_nav_note_to_nav_notes );
            return true;
        }
        return super.onContextItemSelected(item);

    }
}