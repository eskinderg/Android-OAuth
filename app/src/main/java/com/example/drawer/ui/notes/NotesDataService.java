package com.example.drawer.ui.notes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface NotesDataService {

    @GET("notes")
    Call<Note[]> getNotes();

    @Headers({"Content-Type: application/json"})
    @PUT("notes")
    Call<Note> updateNote(@Body Note note);

    @Headers({"Content-Type: application/json"})
    @POST("notes")
    Call<Note> addNote(@Body Note note);
}
