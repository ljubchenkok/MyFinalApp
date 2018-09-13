package ru.com.penza.myfinalapp.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import ru.com.penza.myfinalapp.R;
import ru.com.penza.myfinalapp.datamodel.Person;
import ru.com.penza.myfinalapp.fragments.AddCardFragment;

public class MyRecycleViewAdapter extends RecyclerView.Adapter {
    private int fontSize = 30;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;


    private List<Person> persons;
    private OnItemClickListener listener;
    private Context context;
    private Bitmap bitmap;

    public interface OnItemClickListener {
        void onItemClick(Person person, PersonViewHolder holder);
    }

    public MyRecycleViewAdapter(List<Person> persons, OnItemClickListener listener, Integer fontSize, Context context, RecyclerView recyclerView) {
        this.persons = persons;
        this.listener = listener;
        this.fontSize = fontSize;
        this.context = context;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.einstein);
    }

    @Override
    public int getItemViewType(int position) {
        return persons.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MyRecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.single_card, parent, false);

            viewHolder = new PersonViewHolder(view, fontSize);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            viewHolder = new ProgressViewHolder(view);
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof PersonViewHolder) {
            final PersonViewHolder personViewHolder = (PersonViewHolder) holder;
            personViewHolder.textViewName.setText(persons.get(position).getFirstName());
            personViewHolder.textViewSurname.setText(persons.get(position).getLastName());
            personViewHolder.textViewPatronic.setText(persons.get(position).getSecondName());
            personViewHolder.textViewPhone.setText(persons.get(position).getPhone());
            personViewHolder.itemView.setBackgroundColor(Color.parseColor(persons.get(position).getColor()));
            personViewHolder.imageView.setTransitionName(AddCardFragment.TRANSITION_PHOTO_NAME + String.valueOf(persons.get(position).getId()));
            personViewHolder.imageContainer.setTransitionName(AddCardFragment.TRANSITION_CONTAINER_NAME + String.valueOf(persons.get(position).getId()));
            personViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(persons.get(personViewHolder.getAdapterPosition()), personViewHolder);

                }
            });

            String imageURL = persons.get(position).getImageUrl();
            if (imageURL == null) {
                Picasso.with(context).load(R.drawable.einstein).transform(new CropCircleTransformation())
                        .into(personViewHolder.imageView);
            } else {
                Picasso.with(context).load(Uri.parse(imageURL)).transform(new CropCircleTransformation())
                        .into(personViewHolder.imageView);
            }


        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }


    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_view)
        CardView cardView;
        @BindView(R.id.lastName)
        TextView textViewSurname;
        @BindView(R.id.firstName)
        TextView textViewName;
        @BindView(R.id.secondName)
        TextView textViewPatronic;
        @BindView(R.id.person_phone)
        TextView textViewPhone;
        @BindView(R.id.person_photo)
        public ImageView imageView;
        @BindView(R.id.image_container)
        public RelativeLayout imageContainer;


        PersonViewHolder(View itemView, Integer fontSize) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            textViewSurname.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        }

    }
}
