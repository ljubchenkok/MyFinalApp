package ru.com.penza.myfinalapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.com.penza.myfinalapp.MyTransitionSet;
import ru.com.penza.myfinalapp.datasources.MyDBHelper;
import ru.com.penza.myfinalapp.views.MyRecycleViewAdapter;
import ru.com.penza.myfinalapp.views.MyRecyclerView;
import ru.com.penza.myfinalapp.datamodel.Person;
import ru.com.penza.myfinalapp.R;
import ru.com.penza.myfinalapp.views.OnLoadMoreListener;


public class ListFragment extends Fragment implements MyRecycleViewAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {


    private static final long PAGING_DELAY = 1000;
    private static final String PERSON_CLICK = "PERSON_CLICK";
    private static final String PERSON_LAST_NAME = "PERSON_LAST_NAME";
    private static final String PERSON_ID = "PERSON_ID";
    private String fontSize;
    private String scrollSpeed;
    private int limit = 10;
    private int offset = 0;
    private int curNumItems = 10;
    private MyDBHelper myDBHelper;
    private List<Person> persons;
    Unbinder unbinder;
    private MyRecycleViewAdapter recycleViewAdapter;
    private OnFragmentInteractionListener myListener;
    private static final String KEY_SORT = "key_sort";
    private static final String KEY_FONT = "key_font";
    private static final String KEY_SPEED = "key_speed";
    private FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.empty_list_view)
    TextView emptyMessageView;

    @BindView(R.id.recycle_view)
    MyRecyclerView recyclerView;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    boolean sortOrder;
    protected Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        handler = new Handler();
        myDBHelper = new MyDBHelper(getActivity());
        myListener.setMainUI(View.VISIBLE, false, true, getString(R.string.list_title));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        initPreferences();
        initRecycleView();
        return view;
    }

    private List<Person> getPersons(int number) {
        List<Person> persons = myDBHelper.getPagedPersons(number, offset, sortOrder);
        offset = offset + number;
        curNumItems = offset;
        if (!sortOrder) {
            Collections.sort(persons, new Person.MyNameComparator());
        } else {
            Collections.sort(persons, new Person.MyReverseNameComparator());
        }
        return persons;
    }

    private void initRecycleView() {
        offset = 0;
        persons = getPersons(curNumItems);
        if (persons.size() > 0) {
            emptyMessageView.setVisibility(View.INVISIBLE);
        }
        recyclerView.setOnLoadMoreListener(onLoadMoreListener);
        recyclerView.setHasFixedSize(true);
        recyclerView.setSpeedFactor(Float.valueOf(scrollSpeed));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recycleViewAdapter = new MyRecycleViewAdapter(persons, this, Integer.valueOf(fontSize), getContext(), recyclerView);
        recyclerView.setAdapter(recycleViewAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        myDBHelper.close();
    }


    private OnLoadMoreListener onLoadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore(int currentCount) {
            if (currentCount >=myDBHelper.getPersonCount()) return;
            persons.add(null);
            recyclerView.post(new Runnable() {
                public void run() {
                    recycleViewAdapter.notifyItemInserted(persons.size() - 1);
                }
            });
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    persons.remove(persons.size() - 1);
                    recycleViewAdapter.notifyItemRemoved(persons.size());
                    int start = persons.size();
                    int end = start + limit;
                    persons.addAll(getPersons(limit));
                    recycleViewAdapter.notifyItemRangeInserted(start, end);
                    if (recyclerView != null) {
                        recyclerView.setLoaded();
                    }
                }
            }, PAGING_DELAY);

        }
    };



    private void initPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortOrder = sharedPreferences.getBoolean(KEY_SORT, false);
        fontSize = sharedPreferences.getString(KEY_FONT, null);
        scrollSpeed = sharedPreferences.getString(KEY_SPEED, null);


    }

    ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            myDBHelper.delPerson(persons.get(position));
            removePerson(persons.get(position));
        }
    };


    public void removePerson(Person personToRemove) {
        int position = persons.indexOf(personToRemove);
        persons.remove(personToRemove);
        recycleViewAdapter.notifyItemRemoved(position);
        if (persons.size() == 0) emptyMessageView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            myListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myDBHelper.close();
        unbinder.unbind();
    }


    @Override
    public void onItemClick(Person person, MyRecycleViewAdapter.PersonViewHolder holder) {
        logToFirebase(person);
        AddCardFragment addCardFragment = AddCardFragment.newInstance(person.getId());
        String transitionNameforPhoto = AddCardFragment.TRANSITION_PHOTO_NAME + String.valueOf(person.getId());
        String transitionNameforContainer = AddCardFragment.TRANSITION_CONTAINER_NAME + String.valueOf(person.getId());
        addCardFragment.setSharedElementEnterTransition(new MyTransitionSet());
        addCardFragment.setEnterTransition(new Fade());
        setExitTransition(new Fade());
        addCardFragment.setSharedElementReturnTransition(new MyTransitionSet());
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.imageView, transitionNameforPhoto)
                .addSharedElement(holder.imageContainer, transitionNameforContainer)
                .replace(R.id.container, addCardFragment)
                .addToBackStack(null)
                .commit();
        myListener.setMainUI(View.INVISIBLE, true, false, getString(R.string.add_card_title));

    }

    private void logToFirebase(Person person) {
        Bundle bundle = new Bundle();
        bundle.putInt(PERSON_ID, person.getId());
        bundle.putString(PERSON_LAST_NAME, person.getLastName());
        mFirebaseAnalytics.logEvent(PERSON_CLICK, bundle);
    }

    @Override
    public void onRefresh() {
        myListener.refresh();

    }


    public interface OnFragmentInteractionListener {

        void setMainUI(int fabVisible, boolean homeEnabled, boolean menuEnabled, String title);

        void refresh();


    }

}
