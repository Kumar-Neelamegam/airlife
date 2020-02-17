package at.jku.mobilecomputing.airlife.NetworkUtils;

import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("feed/here/")
    Call<APIResponse> getAQI(@Query("token") String token);

    @GET("feed/{geo}/")
    Call<APIResponse> getLocationAQI(@Path("geo") String geo, @Query("token") String token);

}
