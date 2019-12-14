package at.jku.mobilecomputing.airlife.CoreModules;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Adapters.FavouriteListAdapter;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;

public class ListFavActivity extends AppCompatActivity implements FavouriteListAdapter.ItemClickListener, View.OnClickListener {

    private SharedPrefUtils sharedPrefUtils;
    ArrayList<FavouriteListDataSet> favouriteListObjects = new ArrayList<>();
    FavouriteListAdapter favouriteListAdapter;
    RecyclerView recyclerView;

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
        favouriteListAdapter = new FavouriteListAdapter(this, result, recyclerView);
        favouriteListAdapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(favouriteListAdapter);
    }

    private void generateDummyList() {
        AsynkTaskCustom asynkTaskCustom = new AsynkTaskCustom(ListFavActivity.this, "Please wait...Loading...");
        asynkTaskCustom.execute(new onWriteCode<List<FavouriteListDataSet>>() {
            @Override
            public List<FavouriteListDataSet> onExecuteCode() {
                List<FavouriteListDataSet> favouriteListObjects = new ArrayList<>();
                favouriteListObjects = Common.getAllFavouriteDataSet(ListFavActivity.this);
                return favouriteListObjects;
            }

            @Override
            public List<FavouriteListDataSet> onSuccess(List<FavouriteListDataSet> result) {
                //Toast.makeText(ListFavActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                LoadData(result);
                return null;
            }
        });
    }

    private void Init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Favourite List");
        recyclerView = findViewById(R.id.recycler_favourite);

    }

    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.isDarkMode())
            setTheme(R.style.AppTheme_Dark);
        else setTheme(R.style.AppTheme_Light);
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

    }
}
