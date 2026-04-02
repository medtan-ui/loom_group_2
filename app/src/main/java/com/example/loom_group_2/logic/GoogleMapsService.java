package com.example.loom_group_2.logic;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class GoogleMapsService {
    private static final String BASE_URL = "https://maps.googleapis.com/";
    private static GoogleMapsService instance;
    private final MapsApi api;

    private GoogleMapsService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(MapsApi.class);
    }

    public static synchronized GoogleMapsService getInstance() {
        if (instance == null) {
            instance = new GoogleMapsService();
        }
        return instance;
    }

    public interface MapsApi {
        @GET("maps/api/directions/json")
        Call<DirectionsResponse> getDirections(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("key") String apiKey
        );
    }

    public void getRoute(String origin, String destination, String apiKey, retrofit2.Callback<DirectionsResponse> callback) {
        api.getDirections(origin, destination, apiKey).enqueue(callback);
    }

    public static class DirectionsResponse {
        @SerializedName("routes")
        public List<Route> routes;

        public static class Route {
            @SerializedName("summary")
            public String summary;
            @SerializedName("legs")
            public List<Leg> legs;
            @SerializedName("overview_polyline")
            public Polyline overviewPolyline;
        }

        public static class Leg {
            @SerializedName("distance")
            public Distance distance;
            @SerializedName("duration")
            public Duration duration;
        }

        public static class Distance {
            @SerializedName("value")
            public int value; // in meters
        }

        public static class Duration {
            @SerializedName("value")
            public int value; // in seconds
        }

        public static class Polyline {
            @SerializedName("points")
            public String points;
        }
    }
}
