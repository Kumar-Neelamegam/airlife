package at.jku.mobilecomputing.airlife.CoreModules;

import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;
import weka.classifiers.Classifier;
import weka.core.pmml.jaxbbindings.KNNInput;

public class PredictionActivity extends AppCompatActivity {

    private SharedPrefUtils sharedPrefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        Init();
        getAllDataSet();



    }


    //Machine learning

    Random mRandom = new Random();

    Classifier mClassifier = null;

    private void callMachineLearning(List<AqiDataSet>  aqiDataSets) {


        // show samples
        StringBuilder sb = new StringBuilder("Samples:\n");
        for (AqiDataSet aqiDataSet : aqiDataSets) {
            sb.append(aqiDataSet.getAirquality() + "\n");
        }


        generateModel();

        loadModel();

        runPrediction();


    }

    private void runPrediction() {
        if(mClassifier==null){
            Toast.makeText(this, "Model not loaded!", Toast.LENGTH_SHORT).show();
            return;
        }


        /*// we need those for creating new instances later
        // order of attributes/classes needs to be exactly equal to those used for training
        final Attribute attributeSepalLength = new Attribute("sepallength");
        final Attribute attributeSepalWidth = new Attribute("sepalwidth");
        final Attribute attributePetalLength = new Attribute("petallength");
        final Attribute attributePetalWidth = new Attribute("petalwidth");
        final List<String> classes = new ArrayList<String>() {
            {
                add("Iris-setosa"); // cls nr 1
                add("Iris-versicolor"); // cls nr 2
                add("Iris-virginica"); // cls nr 3
            }
        };

        // Instances(...) requires ArrayList<> instead of List<>...
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
            {
                add(attributeSepalLength);
                add(attributeSepalWidth);
                add(attributePetalLength);
                add(attributePetalWidth);
                Attribute attributeClass = new Attribute("@@class@@", classes);
                add(attributeClass);
            }
        };
        // unpredicted data sets (reference to sample structure for new instances)
        Instances dataUnpredicted = new Instances("TestInstances",
                attributeList, 1);
        // last feature is target variable
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

        // create new instance: this one should fall into the setosa domain
        final Sample s = mSamples[mRandom.nextInt(mSamples.length)];
        DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
            {
                setValue(attributeSepalLength, s.features[0]);
                setValue(attributeSepalWidth, s.features[1]);
                setValue(attributePetalLength, s.features[2]);
                setValue(attributePetalWidth, s.features[3]);
            }
        };
        // reference to dataset
        newInstance.setDataset(dataUnpredicted);

        // predict new sample
        try {
            double result = mClassifier.classifyInstance(newInstance);
            String className = classes.get(new Double(result).intValue());
            String msg = "Nr: " + s.nr + ", predicted: " + className + ", actual: " + classes.get(s.label);
            Log.d(WEKA_TEST, msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }




    public class Sample {
        public int nr;
        public int label;
        public double [] features;

        public Sample(int _nr, int _label, double[] _features) {
            this.nr = _nr;
            this.label = _label;
            this.features = _features;
        }

        @Override
        public String toString() {
            return "Nr " +
                    nr +
                    ", cls " + label +
                    ", feat: " + Arrays.toString(features);
        }
    }


*/

    }

    private void loadModel() {
        AssetManager assetManager = getAssets();
        try {
            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("iris_model_logistic_allfeatures.model"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Weka "catch'em all!"
            e.printStackTrace();
        }
        Toast.makeText(this, "Model loaded.", Toast.LENGTH_SHORT).show();
    }

    private void generateModel() {


    }

    private void getAllDataSet() {
        AsynkTaskCustom asynkTaskCustom = new AsynkTaskCustom(PredictionActivity.this, "Please wait...Loading...");
        asynkTaskCustom.execute(new onWriteCode<List<AqiDataSet>>() {
            @Override
            public List<AqiDataSet> onExecuteCode() {
                List<AqiDataSet>   predictionListObjects = new ArrayList<>();
                predictionListObjects = Common.getAllAQIDataSet(PredictionActivity.this);
                return predictionListObjects;
            }

            @Override
            public List<AqiDataSet> onSuccess(List<AqiDataSet> result) {
                //Toast.makeText(PredictionActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                callMachineLearning(result);
                return result;
            }
        });
    }

    private void Init()
    {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Prediction");
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
}

