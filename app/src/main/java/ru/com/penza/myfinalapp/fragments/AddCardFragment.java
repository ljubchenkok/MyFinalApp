package ru.com.penza.myfinalapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import ru.com.penza.myfinalapp.ColorTransformation;
import ru.com.penza.myfinalapp.FetchAddressIntentService;
import ru.com.penza.myfinalapp.MapsActivity;
import ru.com.penza.myfinalapp.R;
import ru.com.penza.myfinalapp.datamodel.Person;
import ru.com.penza.myfinalapp.datasources.MyDBHelper;
import yuku.ambilwarna.AmbilWarnaDialog;

import static android.app.Activity.RESULT_OK;


public class AddCardFragment extends Fragment {



    public static final String RECEIVER = "RECEIVER";
    public static final String ADDRESS = "ADDRESS";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGTITUDE = "LONGTITUDE";
    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;


    private static final String KEY_USE_GOOGLE_MAPS = "key_use_google_maps";
    private static final java.lang.String PERSON_ID = "id";
    private static final String FILE_NAME_BASE = "lesson11-";
    private static final String GOOGLE_MAPS_BASE_URL = "https://maps.google.com/?q=@" ;
    private final int CAMERA_RESULT = 57;
    public static final String TRANSITION_PHOTO_NAME = "transition_name_for_photo";
    public static final String TRANSITION_CONTAINER_NAME = "transition_name_for_container";
    private static final int RESULT_LOAD_IMG = 52;
    private OnFragmentInteractionListener listener;
    private Person person;

    AmbilWarnaDialog dialog;


    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private String addressOutput;
    private AddressResultReceiver resultReceiver;

    @BindView(R.id.positionLabel)
    TextView positionView;

    @BindView(R.id.btnSave)
    Button btnSave;

    @BindView(R.id.firstName)
    EditText editName;

    @BindView(R.id.lastName)
    EditText editSurName;

    @BindView(R.id.secondName)
    EditText editPatron;

    @BindView(R.id.phone)
    MaskedEditText editPhone;

    @BindView(R.id.person_photo)
    ImageView imageView;

    @BindView(R.id.image_container)
    RelativeLayout imageContainer;

    @BindInt(R.integer.alpha)
    int alpha;

    Unbinder unbinder;
    private int personId;
    private Uri imageUri;
    private Uri myPhotoUri;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);
        unbinder = ButterKnife.bind(this, view);
        getPersoninInfo();
        editPhone.setOnEditorActionListener(myListener);
        return view;
    }


    private TextView.OnEditorActionListener myListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideSoftKeyboard();
            }
            return false;
        }
    };

    public static AddCardFragment newInstance(int id) {
        AddCardFragment addCardFragment = new AddCardFragment();
        Bundle args = new Bundle();
        args.putInt(PERSON_ID, id);
        addCardFragment.setArguments(args);
        return addCardFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        personId = getArguments().getInt(PERSON_ID, 0);
    }

    private void getPersoninInfo() {
        if (personId == -1) {
            person = new Person();
            Picasso.with(getContext()).load(R.drawable.einstein).transform(new CropCircleTransformation())
                    .into(imageView);


        } else {
            MyDBHelper myDBHelper = new MyDBHelper(getActivity());
            person = myDBHelper.getPersonById(personId);
            imageView.setTransitionName(TRANSITION_PHOTO_NAME + personId);
            imageContainer.setBackgroundColor(Color.parseColor(person.getColor()));
            imageContainer.setTransitionName(TRANSITION_CONTAINER_NAME + personId);
            editName.setText(person.getFirstName());
            editSurName.setText(person.getLastName());
            editPatron.setText(person.getSecondName());
            editPhone.setText(person.getPhone());
            String address = person.getAddress();
            if(address != null) {
                positionView.setText(person.getAddress());
                makeTextViewHyperlink(positionView, positionListener);

            }
            showImage();
            myDBHelper.close();

        }
    }

    private void showImage() {
        int color = Color.parseColor(person.getColor());
        int colorWithAlpha = ColorUtils.setAlphaComponent(color, alpha);
        if (person.getImageUrl() == null) {
            Picasso.with(getContext()).load(R.drawable.einstein)
                    .transform(new ColorTransformation(colorWithAlpha))
                    .transform(new CropCircleTransformation())
                    .into(imageView);
        } else {
            Picasso.with(getContext()).load(Uri.parse(person.getImageUrl()))
                    .transform(new ColorTransformation(colorWithAlpha))
                    .transform(new CropCircleTransformation())
                    .into(imageView);
        }
    }


    @OnClick(R.id.person_photo)
    public void showImageDialog() {
        final Context context = getContext();
        if (!isCameraAvailable(context)) {
            changeImage();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.image_dialog_title));
        builder.setMessage(getString(R.string.image_dialog_message));
        builder.setPositiveButton(R.string.gallery, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeImage();
            }
        });
        builder.setNegativeButton(R.string.camera, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createPhoto();

            }
        });
        builder.setNeutralButton(R.string.change_bkg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseColor();
            }
        });
        builder.show();
    }


    private void changeImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG) {
                imageUri = data.getData();
                Picasso.with(getContext()).load(imageUri).transform(new CropCircleTransformation())
                        .into(imageView);
            }
            if (requestCode == CAMERA_RESULT) {
                Picasso.with(getContext()).load(myPhotoUri).transform(new CropCircleTransformation())
                        .into(imageView);
                person.setImageURL(myPhotoUri.toString());

            }
        }
    }


    private void createPhoto() {
        MyDBHelper myDBHelper = new MyDBHelper(getActivity());
        int id = myDBHelper.getPersonCount() + 1;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = FILE_NAME_BASE + String.valueOf(id) + "-" + timeStamp + ".jpg";
        File file = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                fileName
        );
        myPhotoUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, myPhotoUri);
        startActivityForResult(intent, CAMERA_RESULT);
    }

    public static boolean isCameraAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    private void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, lastLocation);
        getActivity().startService(intent);
    }

    @OnClick(R.id.btnSave)
    public void onSave(View view) {
        hideSoftKeyboard();
        person.setFirstName(editName.getText().toString());
        person.setLastName(editSurName.getText().toString());
        person.setSecondName(editPatron.getText().toString());
        String phone = editPhone.getText().toString();
        if (imageUri != null) {
            person.setImageURL(imageUri.toString());
        }
        person.setPhone(phone);
        if (person.isEmpty()) return;
        MyDBHelper myDBHelper = new MyDBHelper(getActivity());
        if (personId == -1) {
            myDBHelper.addPerson(person);
        } else {
            person.setId(this.person.getId());
            myDBHelper.updatePerson(person);
        }
        listener.setMainUI(View.VISIBLE, false, true, getString(R.string.list_title));
        getFragmentManager().popBackStack();

    }

    public int getPersonId() {
        return personId;
    }


    public void chooseColor() {
        dialog = new AmbilWarnaDialog(getContext(), Color.parseColor(person.getColor()), true, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                person.setColor(String.format("#%06X", (0xFFFFFF & color)));
                imageContainer.setBackgroundColor(color);
                showImage();

            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

        });
        dialog.show();

    }

    private void hideSoftKeyboard() {
        View currentFocus = getActivity().getWindow().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnPosition)
    public void getAddress() {
        resultReceiver = new AddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lastLocation = location;
                        startIntentService();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onPositionFailure();


                    }
                });


    }

    private void onPositionFailure() {
        addressOutput = getString(R.string.position_failure);
        positionView.setText(addressOutput);
    }


    public interface OnFragmentInteractionListener {

        void setMainUI(int fabVisible, boolean homeEnabled, boolean menuEnabled, String title);
    }


    private View.OnClickListener positionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent;
            if(needGoogleMapsFromWEB()) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_MAPS_BASE_URL +
                        person.getLatitude().toString() + "," + person.getLongtitude().toString()));


            }else {
                intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra(LATITUDE, person.getLatitude());
                intent.putExtra(LONGTITUDE, person.getLongtitude());
            }
            startActivity(intent);

        }
    };

    public void makeTextViewHyperlink(TextView tv, View.OnClickListener listener) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
        tv.setClickable(true);
        tv.setOnClickListener(listener);
    }

    private boolean needGoogleMapsFromWEB() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(KEY_USE_GOOGLE_MAPS, true);
    }


    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode != SUCCESS_RESULT) {
                onPositionFailure();
            } else {
                positionView.setText(resultData.getString(ADDRESS));
                person.setAddress(resultData.getString(ADDRESS));
                person.setLongtitude(resultData.getDouble(LONGTITUDE));
                person.setLatitude(resultData.getDouble(LATITUDE));
                makeTextViewHyperlink(positionView, positionListener);



            }

        }
    }
}
