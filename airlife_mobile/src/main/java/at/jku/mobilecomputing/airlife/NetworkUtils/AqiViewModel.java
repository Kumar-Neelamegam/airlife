package at.jku.mobilecomputing.airlife.NetworkUtils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import at.jku.mobilecomputing.airlife.BuildConfig;
import at.jku.mobilecomputing.airlife.Constants.Status;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqiViewModel extends ViewModel {
    private RetrofitHelper mRetrofitHelper;
    private APIInterface mApiInterface;
    private MutableLiveData<APIResponse> mApiResponse;
    private MutableLiveData<Status> mStatus = new MutableLiveData<>();
    private final String apiKey = BuildConfig.ApiKey;

    public AqiViewModel() {
        super();
        mRetrofitHelper = RetrofitHelper.getInstance();
    }


    public LiveData<APIResponse> getApiResponse() {
        if (mApiResponse == null) {
            mApiResponse = new MutableLiveData<>();
            loadApiResponse();
        }
        return mApiResponse;
    }

    private void loadApiResponse() {
        mApiInterface = mRetrofitHelper.getApiInterface();
        mStatus.setValue(Status.FETCHING);
        Call<APIResponse> mApiResponseCall = mApiInterface.getAQI(apiKey);
        mApiResponseCall.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }
                
                mApiResponse.setValue(response.body());
                mStatus.setValue(Status.DONE);
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                Log.d("Error", "error");
            }
        });
    }

    public LiveData<APIResponse> getGPSApiResponse(String geo) {
        if (mApiResponse == null) {
            mApiResponse = new MutableLiveData<>();
            loadGPSBasedApiResponse(geo);
        }
        return mApiResponse;
    }

    private void loadGPSBasedApiResponse(String geo) {
        mApiInterface = mRetrofitHelper.getApiInterface();
        mStatus.setValue(Status.FETCHING);
        Call<APIResponse> mApiResponseCall = mApiInterface.getLocationAQI(geo, apiKey);
        mApiResponseCall.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                mApiResponse.setValue(response.body());
                mStatus.setValue(Status.DONE);
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                Log.d("Error", "error");
            }
        });
    }


    public LiveData<Status> getStatus() {
        return mStatus;
    }
}
