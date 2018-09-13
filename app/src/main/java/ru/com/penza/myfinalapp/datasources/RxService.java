package ru.com.penza.myfinalapp.datasources;

import java.util.List;


import io.reactivex.Observable;
import retrofit2.http.GET;
import ru.com.penza.myfinalapp.datamodel.Person;


public interface RxService {

    @GET("/lesson11.json?key=e2356920")
    Observable<List<Person>> getAnswers();
}
