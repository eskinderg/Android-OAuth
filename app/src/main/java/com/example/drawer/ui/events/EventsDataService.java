package com.example.drawer.ui.events;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface EventsDataService {
    @GET("events")
    Call<Event[]> getEvents(@Header("Authorization") String authHeader);

    @Headers({"Content-Type: application/json"})
    @PUT("events/toggle")
    Call<Event> toggleEvent(@Header("Authorization") String authHeader, @Body Event event);
}