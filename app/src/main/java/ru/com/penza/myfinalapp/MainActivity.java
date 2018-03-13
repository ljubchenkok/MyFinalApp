package ru.com.penza.myfinalapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.com.penza.myfinalapp.datamodel.Person;
import ru.com.penza.myfinalapp.datasources.ApiUtils;
import ru.com.penza.myfinalapp.datasources.MyDBHelper;
import ru.com.penza.myfinalapp.datasources.RxService;
import ru.com.penza.myfinalapp.fragments.AddCardFragment;
import ru.com.penza.myfinalapp.fragments.ListFragment;


public class MainActivity extends AppCompatActivity implements AddCardFragment.OnFragmentInteractionListener,
        ListFragment.OnFragmentInteractionListener {


    private static final int SETTINGS = 50;
    private static final String CURRENT_FRAGMENT = "current_fragment";
    private static final int ADD_FRAGMENT_ID = 2;
    private static final int LIST_FRAGMENT_ID = 1;
    private static final String PERSON_ID = "person_id";
    private static final String KEY_START_DOWNLOAD = "key_start_download";
    private static final String KEY_USE_RX = "key_use_rx";
    private MyDBHelper myDBHelper;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    private ListFragment listFragment;

    private MenuItem menuItem;
    private Unbinder unbinder;
    private ProgressDialog dialog;
    MyBackgroundTask watchInetConnection;

    class MyHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (!isFinishing() && !isChangingConfigurations()) {
                        showListFragment();
                    }
                    break;
                case 1:
                    if (dialog != null) {
                        dialog.show();
                    }
                    break;
                case 2:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
            }
            return true;
        }
    }

    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        setSupportActionBar(toolbar);
        handler = new Handler(new MyHandlerCallback());
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.download_message));
        myDBHelper = new MyDBHelper(this);

        if (savedInstanceState != null) {
            int currentFragmentId = savedInstanceState.getInt(CURRENT_FRAGMENT);
            if (currentFragmentId == 2) {
                int personId = savedInstanceState.getInt(PERSON_ID);
                showFragment(AddCardFragment.newInstance(personId));
                return;
            }

        }
        initDB();
        showListFragment();

    }

    private void initDB() {
        watchInetConnection = (MyBackgroundTask) getLastCustomNonConfigurationInstance();
        if (watchInetConnection == null) {
            watchInetConnection = new MyBackgroundTask(this, handler);
        }
        watchInetConnection.link(this);
        if (needDownloadfromWeb()) {
            if (needUseRx()) {
                getPersons();
            } else {
                refresh();
            }
        }

    }

    private boolean needUseRx() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(KEY_USE_RX, false);
    }


    private boolean needDownloadfromWeb() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(KEY_START_DOWNLOAD, false);
    }


    public void showListFragment() {
        listFragment = new ListFragment();
        showFragment(listFragment);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuItem = menu.findItem(R.id.action_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            showAddFragment();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            showFragment(listFragment);

        }
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, MyPreferenceActivity.class);
            startActivityForResult(intent, SETTINGS);

        }
        return super.onOptionsItemSelected(item);
    }


    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment.getClass().equals(AddCardFragment.class)) {
            fragmentTransaction.addToBackStack(null);
            setMainUI(View.INVISIBLE, true, false, getString(R.string.add_card_title));
        } else {
            setMainUI(View.VISIBLE, false, true, getString(R.string.list_title));
        }
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

    }


    @OnClick(R.id.fab)
    public void onFabClick(View view) {
        showAddFragment();
    }


    public void showAddFragment() {
        AddCardFragment fragment = AddCardFragment.newInstance(-1);
        showFragment(fragment);
    }

    @Override
    public void setMainUI(int fabVisible, boolean homeEnabled, boolean menuEnabled, String title) {

        ActionBar actionBar = getSupportActionBar();
        if (fab == null || actionBar == null) return;
        fab.setVisibility(fabVisible);
        actionBar.setDisplayHomeAsUpEnabled(homeEnabled);
        setTitle(title);
        if (menuItem != null) {
            menuItem.setEnabled(menuEnabled);
        }
    }


    @Override
    public void refresh() {
        if (!needUseRx()) {

            AsyncTask.Status status = watchInetConnection.getStatus();
            if (status == AsyncTask.Status.FINISHED) {
                watchInetConnection = new MyBackgroundTask(this, handler);
            }
            if (watchInetConnection.getStatus() != AsyncTask.Status.RUNNING) {
                watchInetConnection.execute();
            }
        } else {
            getPersons();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if(!needUseRx()) {
            if (watchInetConnection != null && watchInetConnection.getStatus().equals(AsyncTask.Status.RUNNING)) {
                watchInetConnection.cancel(true);
            }
        }
        unbinder.unbind();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS) {
            showListFragment();
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        watchInetConnection.unLink();
        return watchInetConnection;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment.getClass().equals(AddCardFragment.class)) {
            outState.putInt(CURRENT_FRAGMENT, ADD_FRAGMENT_ID);
            int personId = ((AddCardFragment) currentFragment).getPersonId();
            outState.putInt(PERSON_ID, personId);
        } else {
            outState.putInt(CURRENT_FRAGMENT, LIST_FRAGMENT_ID);
        }
    }

    public void getPersons() {
        RxService rxService = ApiUtils.getRxService();
        Observable<List<Person>> observable = rxService.getAnswers().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        observable.subscribe(new Observer<List<Person>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Person> persons) {
                myDBHelper.resetDB();
                handler.sendEmptyMessage(1);
                for (int id = 0; id < persons.size(); id++) {
                    if (myDBHelper != null) {
                        myDBHelper.addPerson(persons.get(id));
                    }
                }
                handler.sendEmptyMessage(2);
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        });

    }


}

