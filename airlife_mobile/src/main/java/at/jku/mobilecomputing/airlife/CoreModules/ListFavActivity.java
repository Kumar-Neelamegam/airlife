package at.jku.mobilecomputing.airlife.CoreModules;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ftoslab.openweatherretrieverz.CurrentWeatherInfo;
import com.ftoslab.openweatherretrieverz.OpenWeatherRetrieverZ;
import com.ftoslab.openweatherretrieverz.WeatherCallback;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Adapters.FavouriteListAdapter;
import at.jku.mobilecomputing.airlife.BuildConfig;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.NetworkUtils.APIResponse;
import at.jku.mobilecomputing.airlife.NetworkUtils.RetrofitHelper;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.CustomDialog;
import at.jku.mobilecomputing.airlife.Utilities.RecyclerItemTouchHelper;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFavActivity extends AppCompatActivity implements FavouriteListAdapter.ItemClickListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, View.OnClickListener {

    private SharedPrefUtils sharedPrefUtils;
    FavouriteListAdapter favouriteListAdapter;
    RecyclerView recyclerView;
    LinearLayout parentLayout;

    private final String apiKey = BuildConfig.ApiKey;
    List<FavouriteListDataSet> result = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritelocation);

        try {
            Init();
            generateList();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void LoadData(List<FavouriteListDataSet> result) {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouriteListAdapter = new FavouriteListAdapter(this, result);
        favouriteListAdapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(favouriteListAdapter);

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


    }

    private void generateList() {

        List<FavouriteListDataSet> favouriteListObjects = new ArrayList<>();
        favouriteListObjects = Common.getAllFavouriteDataSet(ListFavActivity.this);
        showDialog(getResources().getString(R.string.fav_loading_msg));

        for (FavouriteListDataSet favouriteListObject : favouriteListObjects) {
            getAqiDataFromLatitudeLongitude(favouriteListObject.getId(), favouriteListObject.getLatitude(), favouriteListObject.getLongitude(), favouriteListObject.getLocation(), favouriteListObjects.size());
        }

    }

    private void Init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.favTitle));
        recyclerView = findViewById(R.id.recycler_favourite);
        parentLayout = findViewById(R.id.parent_layout);

    }

    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.isDarkMode()) {
            setTheme(R.style.AppTheme_Dark);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeDarkPrimary)));
        } else {
            setTheme(R.style.AppTheme_Light);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeLightPrimary)));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "clicked on:"+ position, Toast.LENGTH_SHORT).show();

    }

    private void getAqiDataFromLatitudeLongitude(int id, double latitude, double longitude, String location, int totalSize) {
        try {

            String geo = "geo:" + latitude + ";" + longitude;
            Log.e("Geo information:", geo);

            RetrofitHelper.getInstance().getApiInterface().getLocationAQI(geo, apiKey).enqueue(new Callback<APIResponse>() {
                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {

                    Data data = new Data();
                    data = response.body().getData();
                    FavouriteListDataSet favouriteListDataSet = new FavouriteListDataSet();
                    favouriteListDataSet.setId(id);
                    favouriteListDataSet.setLocation(location);
                    favouriteListDataSet.setLocationInfo(data.getCity().getName());
                    favouriteListDataSet.setQualityScale(data.getAqi());
                    if (data.getWaqi().getCo() != null)
                        favouriteListDataSet.setCo(data.getWaqi().getCo().getV());
                    if (data.getWaqi().getNo2() != null)
                        favouriteListDataSet.setNo2(data.getWaqi().getNo2().getV());
                    if (data.getWaqi().getO3() != null)
                        favouriteListDataSet.setO3(data.getWaqi().getO3().getV());
                    if (data.getWaqi().getPm10() != null)
                        favouriteListDataSet.setPm10(data.getWaqi().getPm10().getV());
                    if (data.getWaqi().getPm2_5() != null)
                        favouriteListDataSet.setPm25(data.getWaqi().getPm2_5().getV());
                    if (data.getWaqi().getSo2() != null)
                        favouriteListDataSet.setSo2(data.getWaqi().getSo2().getV());
                    setWeatherInfo(favouriteListDataSet, totalSize, latitude, longitude);
                }


                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {
                    Toast.makeText(ListFavActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                    dismissDialog();

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWeatherInfo(FavouriteListDataSet favouriteListDataSet, int totalSize, double latitude, double longitude) {

        // Initialize OpenWeatherRetrieverZ by passing in  your openweathermap api key
        OpenWeatherRetrieverZ retriever = new OpenWeatherRetrieverZ(Common.openWeatherKey);
            /*
            You can retrieve weather information with either OpenWeatherMap cityID or geolocation(Latitude, Logitude)
            */
        retriever.updateCurrentWeatherInfo(latitude, longitude, new WeatherCallback() {
            @Override
            public void onReceiveWeatherInfo(CurrentWeatherInfo currentWeatherInfo) {
                if (currentWeatherInfo.getHumidity() != null)
                    favouriteListDataSet.setHumidity(Double.parseDouble(currentWeatherInfo.getHumidity()));
                if (currentWeatherInfo.getCurrentTemperature() != null)
                    favouriteListDataSet.setTemperature(Double.parseDouble(currentWeatherInfo.getCurrentTemperature()) - Common.KelvinToCelcius);
                if (currentWeatherInfo.getPressure() != null)
                    favouriteListDataSet.setPressure(Double.parseDouble(currentWeatherInfo.getPressure()));
                if (currentWeatherInfo.getWindSpeed() != null)
                    favouriteListDataSet.setWind(Double.parseDouble(currentWeatherInfo.getWindSpeed()));

                result.add(favouriteListDataSet);
                if (totalSize == result.size()) {
                    LoadData(result);
                    dismissDialog();
                }

            }

            @Override
            public void onFailure(String error) {
                // Your code here
                Log.e("WeatherInfo-onFailure: ", error);
            }
        });

    }
    CustomDialog customDialog;
    private void showDialog(String s) {
        //RetrofitHelper.getInstance().showProgressDialog(this, s);
        customDialog=new CustomDialog(this)
                .setImage(R.drawable.ic_list)
                .setTitle("Information")
                .setNegativeButtonVisible(View.GONE)
                .setDescription(s)
                .setPositiveButtonVisible(View.GONE);
    }

    private void dismissDialog() {
        if(customDialog!=null)
            customDialog.dismiss();
        //RetrofitHelper.getInstance().dismissProgressDialog();
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof FavouriteListAdapter.ViewHolder) {

            boolean status = Common.deleteFavouriteItem(result.get(viewHolder.getAdapterPosition()).getId(), this);
            Toast.makeText(this, status == true ? "Deleted successfully.." : "", Toast.LENGTH_SHORT).show();

            if (status) {
                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar.make(parentLayout, result.get(viewHolder.getAdapterPosition()).getLocation() + " removed from favourite list!", Snackbar.LENGTH_LONG);
                snackbar.show();

                // remove the item from recycler view
                Log.e("getAdapterPosition: ", String.valueOf(viewHolder.getAdapterPosition()));
                Log.e("onSwiped: ", String.valueOf(result.get(viewHolder.getAdapterPosition()).getId()));
                favouriteListAdapter.removeItem(viewHolder.getAdapterPosition());//remove in adapter

            }

        }

    }
}
