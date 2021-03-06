package app.service;

import app.model.Github;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface GithubService {
    String SERVICE_ENDPOINT = "https://api.github.com";

    @GET("/users/{login}")
    Observable<Github> getUserRx(@Path("login") String login);

    @GET("/users/{login}")
    Github getUser(@Path("login") String login);
}
