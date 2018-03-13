package ru.com.penza.myfinalapp.datasources;


public class ApiUtils {

    public static final String BASE_URL = "https://my.api.mockaroo.com";

    public static SOService getSOService() {
        return RetrofitClient.getClient(BASE_URL).create(SOService.class);
    }

    public static RxService getRxService() {
        return RetrofitClient.getRxClient(BASE_URL).create(RxService.class);
    }
}

