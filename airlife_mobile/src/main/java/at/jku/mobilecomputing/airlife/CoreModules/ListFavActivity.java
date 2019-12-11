package at.jku.mobilecomputing.airlife.CoreModules;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Adapters.FavouriteListAdapter;
import at.jku.mobilecomputing.airlife.Adapters.FavouriteListObject;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;

public class ListFavActivity extends AppCompatActivity implements FavouriteListAdapter.ItemClickListener, View.OnClickListener{

    private SharedPrefUtils sharedPrefUtils;
    ArrayList<FavouriteListObject> favouriteListObjects = new ArrayList<>();
    FavouriteListAdapter favouriteListAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritelocation);

        Init();
        LoadDummyData();

    }

    private void LoadDummyData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouriteListAdapter = new FavouriteListAdapter(this, generateDummyList());
        favouriteListAdapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(favouriteListAdapter);
    }

    private List<FavouriteListObject> generateDummyList() {

        List<FavouriteListObject> favouriteListObjects=new ArrayList<>();
        FavouriteListObject favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(1);
        favouriteListObject.setLocation("University");
        favouriteListObject.setLocationInfo("Linz, 4040");
        favouriteListObject.setQualityScale(1);
        favouriteListObject.setQualityValue("50");
        favouriteListObjects.add(favouriteListObject);

        favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(1);
        favouriteListObject.setLocation("Hostel");
        favouriteListObject.setLocationInfo("Linz, 4040");
        favouriteListObject.setQualityScale(1);
        favouriteListObject.setQualityValue("50");
        favouriteListObjects.add(favouriteListObject);


        favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(2);
        favouriteListObject.setLocation("Home");
        favouriteListObject.setLocationInfo("Linz, 4020");
        favouriteListObject.setQualityScale(1);
        favouriteListObject.setQualityValue("50");
        favouriteListObjects.add(favouriteListObject);

        favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(3);
        favouriteListObject.setLocation("Church");
        favouriteListObject.setLocationInfo("Linz, 4040");
        favouriteListObject.setQualityScale(2);
        favouriteListObject.setQualityValue("20");
        favouriteListObjects.add(favouriteListObject);

        favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(4);
        favouriteListObject.setLocation("Playground");
        favouriteListObject.setLocationInfo("Graz, 4080");
        favouriteListObject.setQualityScale(2);
        favouriteListObject.setQualityValue("50");
        favouriteListObjects.add(favouriteListObject);

        favouriteListObject=new FavouriteListObject();
        favouriteListObject.setSno(5);
        favouriteListObject.setLocation("Restaurant");
        favouriteListObject.setLocationInfo("Wels, 4070");
        favouriteListObject.setQualityScale(4);
        favouriteListObject.setQualityValue("50");
        favouriteListObjects.add(favouriteListObject);


        return favouriteListObjects;
    }

    private void Init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Favourite List");
        recyclerView=findViewById(R.id.recycler_favourite);

    }

    public void setUPTheme()
    {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.getAppInstallTime() == 0)
            sharedPrefUtils.setAppInstallTime(System.currentTimeMillis());
        if (sharedPrefUtils.isDarkMode()) setTheme(R.style.AppTheme_Dark);
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
