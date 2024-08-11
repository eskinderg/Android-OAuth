package com.example.drawer.ui.notes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface NotesDataService {

    @GET("notes")
    Call<Note[]> getNotes(@Header("Authorization") String authHeader);

    @Headers({"Content-Type: application/json"})
    @PUT("notes")
    Call<Note> updateNote(@Header("Authorization") String authHeader, @Body Note note);

    @Headers({"Content-Type: application/json"})
    @POST("notes")
    Call<Note> addNote(@Header("Authorization") String authHeader, @Body Note note);
}
