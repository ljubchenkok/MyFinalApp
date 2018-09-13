package ru.com.penza.myfinalapp;


import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ru.com.penza.myfinalapp.datamodel.Person;
import ru.com.penza.myfinalapp.datasources.ApiUtils;
import ru.com.penza.myfinalapp.datasources.MyDBHelper;
import ru.com.penza.myfinalapp.datasources.SOService;

public class MyBackgroundTask extends AsyncTask<Void, Void, Void> {

    private Handler handler;
    private boolean gotAnswer = false;
    private MyDBHelper myDBHelper;
    private MainActivity activity;





    MyBackgroundTask(MainActivity activity, Handler handler) {
        this.handler = handler;
        myDBHelper = new MyDBHelper(activity);
        this.activity = activity;

    }

    @Override
    protected Void doInBackground(Void... params) {
        while (!gotAnswer && !isCancelled()) {
            loadContactsfromWEB();
        }
        return null;
    }


    void link(MainActivity activity) {
        this.activity = activity;
        myDBHelper = new MyDBHelper(activity);
    }


    void unLink() {
        activity = null;
        myDBHelper = null;
    }



    private void loadContactsfromWEB() {
        SOService service = ApiUtils.getSOService();
        List<Person> personList = new ArrayList<>();
        try {
            while (!gotAnswer ) {
                Response<List<Person>> response = service.getAnswers().execute();
                personList = response.body();
                if(response.isSuccessful()){
                    gotAnswer = true;
                }

            }

            handler.sendEmptyMessage(1);
            myDBHelper.resetDB();
            for (int id = 0; id < personList.size(); id++) {
                if (myDBHelper!=null) {
                    myDBHelper.addPerson(personList.get(id));
                }
            }

            if(isCancelled()) return;
            handler.sendEmptyMessage(2);
            handler.sendEmptyMessage(0);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
