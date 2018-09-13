package ru.com.penza.myfinalapp.datasources;

import retrofit2.http.GET;

import java.util.List;

import retrofit2.Call;
import ru.com.penza.myfinalapp.datamodel.Person;


public interface SOService {


    @GET("/lesson11.json?key=e2356920")
    Call<List<Person>> getAnswers();



}
