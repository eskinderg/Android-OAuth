package app.mynote.fragments.event;

import app.mynote.auth.Authorized;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventsDataService {

    @Authorized
    @GET("events")
    Call<Event[]> getEvents();

    @Authorized
    @Headers({"Content-Type: application/json"})
    @PUT("events/toggle")
    Call<Event> toggleEvent(@Body Event event);

    @Authorized
    @DELETE("events/{id}")
    Call<Event> deleteEvent(@Path("id") String eventId);

}