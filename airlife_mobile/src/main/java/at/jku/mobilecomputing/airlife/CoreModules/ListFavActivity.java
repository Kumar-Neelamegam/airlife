package at.jku.mobilecomputing.airlife.CoreModules;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Adapters.FavouriteListAdapter;
import at.jku.mobilecomputing.airlife.BuildConfig;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Constants.Status;
import at.jku.mobilecomputing.airlife.Database.AirLifeDatabaseClient;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDAO;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.DomainObjects.WAQI;
import at.jku.mobilecomputing.airlife.DomainObjects.properties.Co;
import at.jku.mobilecomputing.airlife.NetworkUtils.APIInterface;
import at.jku.mobilecomputing.airlife.NetworkUtils.APIResponse;
import at.jku.mobilecomputing.airlife.NetworkUtils.AqiViewModel;
import at.jku.mobilecomputing.airlife.NetworkUtils.RetrofitHelper;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.RecyclerItemTouchHelper;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFavActivity extends AppCompatActivity implements FavouriteListAdapter.ItemClickListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, View.OnClickListener {

    private SharedPrefUtils sharedPrefUtils;
    FavouriteListAdapter favouriteListAdapter;
    RecyclerView recyclerView;
    LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritelocation);

        try {
            Init();
            generateDummyList();
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

    List<FavouriteListDataSet> favouriteListObjects;
    private void generateDummyList() {

        favouriteListObjects = new ArrayList<>();
        favouriteListObjects = Common.getAllFavouriteDataSet(ListFavActivity.this);
        showDialog("Loading data from nearest station...");

        for (FavouriteListDataSet favouriteListObject : favouriteListObjects) {
            getAqiDataFromLatitudeLongitude(favouriteListObject.getLatitude(), favouriteListObject.getLongitude(),favouriteListObject.getLocation(), favouriteListObjects.size());
        }

    }


    private void Init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Favourite List");
        recyclerView = findViewById(R.id.recycler_favourite);
        parentLayout = findViewById(R.id.parent_layout);

    }

    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.isDarkMode()){
            setTheme(R.style.AppTheme_Dark);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeDarkPrimary)));
        }
        else {
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

    private RetrofitHelper mRetrofitHelper;
    private APIInterface mApiInterface;
    private final String apiKey = BuildConfig.ApiKey;
    List<FavouriteListDataSet> result=new ArrayList<>();
    private void getAqiDataFromLatitudeLongitude(double latitude, double longitude, String location, int totalSize) {
        try {

            String geo = "geo:" + latitude + ";" + longitude;
            Log.e("Geo information:", geo);

            RetrofitHelper.getInstance().getApiInterface().getLocationAQI(geo, apiKey).enqueue(new Callback<APIResponse>() {
                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    dismissDialog();
                    Data data = new Data();
                    data = response.body().getData();
                    FavouriteListDataSet favouriteListDataSet = new FavouriteListDataSet();
                    favouriteListDataSet.setId(data.getIdx());
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
                    if (data.getWaqi().getHumidity() != null)
                        favouriteListDataSet.setHumidity(data.getWaqi().getHumidity().getV());
                    if (data.getWaqi().getTemperature() != null)
                        favouriteListDataSet.setTemperature(data.getWaqi().getTemperature().getV());
                    if (data.getWaqi().getPressure() != null)
                        favouriteListDataSet.setPressure(data.getWaqi().getPressure().getV());
                    if (data.getWaqi().getWind() != null)
                        favouriteListDataSet.setWind(data.getWaqi().getWind().getV());

                    result.add(favouriteListDataSet);
                    if (totalSize == result.size()) {
                        LoadData(result);
                    }
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

    private void showDialog(String s) {
        RetrofitHelper.getInstance().showProgressDialog(this, s);
    }

    private void dismissDialog() {
        RetrofitHelper.getInstance().dismissProgressDialog();
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof FavouriteListAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = favouriteListObjects.get(viewHolder.getAdapterPosition()).getLocation();

             boolean status = Common.deleteFavouriteItem(favouriteListObjects.get(viewHolder.getAdapterPosition()).getId(), this);
             //Toast.makeText(this, status == true ? "Deleted successfully.." : "", Toast.LENGTH_SHORT).show();


            // remove the item from recycler view
            favouriteListAdapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar.make(parentLayout, name + " removed from favourite list!", Snackbar.LENGTH_LONG);
            snackbar.show();

        }

    }
}
