package ru.com.penza.myfinalapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.com.penza.myfinalapp.fragments.AddCardFragment;

/**
 * Created by Константин on 07.03.2018.
 */

public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressIS";

    private ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra(AddCardFragment.RECEIVER);
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }
        Location location = intent.getParcelableExtra(AddCardFragment.LOCATION_DATA_EXTRA);
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(AddCardFragment.FAILURE_RESULT, errorMessage, 0, 0);
            return;
        }


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_lat_long_used);

        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
            }
            deliverResultToReceiver(AddCardFragment.FAILURE_RESULT, errorMessage, 0, 0);
        } else {
            String address = addresses.get(0).getLocality() + ", " + addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getSubThoroughfare();
            deliverResultToReceiver(AddCardFragment.SUCCESS_RESULT, address, location.getLatitude(), location.getLongitude());
        }
    }

    private void deliverResultToReceiver(int resultCode, String address, double latitude, double longitude) {
        Bundle bundle = new Bundle();
        bundle.putString(AddCardFragment.ADDRESS, address);
        bundle.putDouble(AddCardFragment.LATITUDE, latitude);
        bundle.putDouble(AddCardFragment.LONGTITUDE, longitude);
        mReceiver.send(resultCode, bundle);
    }
}

