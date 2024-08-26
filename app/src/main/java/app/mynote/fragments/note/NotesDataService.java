package app.mynote.fragments.note;

import java.util.ArrayList;

import app.mynote.auth.Authorized;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface NotesDataService {

    @Authorized
    @GET("notes")
    Call<Note[]> getNotes();

    @Authorized
    @GET("notes/archived")
    Call<Note[]> getArchivedNotes();

    @Authorized
    @Headers({"Content-Type: application/json"})
    @PUT("notes")
    Call<Note> updateNote(@Body Note note);

    @Authorized
    @Headers({"Content-Type: application/json"})
    @POST("notes")
    Call<Note> addNote(@Body Note note);

    @Authorized
    @Headers({"Content-Type: application/json"})
    @PUT("notes/update")
    Call<Note[]> updateNote(@Body ArrayList<Note> notes);

    @Authorized
    @Headers({"Content-Type: application/json"})
    @POST("notes/insert")
    Call<Note[]> insert(@Body ArrayList<Note> notes);
}
