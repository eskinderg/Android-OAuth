package com.example.drawer.ui.events;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventsDataService {
    @GET("events")
    Call<Event[]> getEvents();

    @Headers({"Content-Type: application/json"})
    @PUT("events/toggle")
    Call<Event> toggleEvent(@Body Event event);

    @DELETE("events/{id}")
    Call<Event> deleteEvent(@Path("id") int eventId);
}