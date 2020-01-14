package at.jku.mobilecomputing.machinelearning;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Prediction {

    public void loadTrainingSet(Context ctx, InputStream arffFile) {


        ArffLoader loader;
        Instances dataSet;

        try {
            loader = new ArffLoader();
            loader.setSource(arffFile);
            dataSet = loader.getDataSet();
            dataSet.setClassIndex(dataSet.numAttributes() - 1);

            //2. building the classifier
            J48 j48 = new J48();
            j48.buildClassifier(dataSet);
            Classifier classifier_result = j48;
            Log.e("Classifier details: ", classifier_result.toString());

            // Load classifer model
            AssetManager assetManager = ctx.getAssets();
            Classifier wekaClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("airlife.model"));

            // Predict
            final Attribute id = new Attribute("id");
            final Attribute timestamp  = new Attribute("timestamp");
            final Attribute airquality  = new Attribute("airquality");
            final Attribute currentLatitude  = new Attribute("currentLatitude");
            final Attribute currentLongitude  = new Attribute("currentLongitude");
            final Attribute temperature  = new Attribute("temperature");
            final Attribute humidity  = new Attribute("humidity");
            final Attribute pressure  = new Attribute("pressure");
            final Attribute wind  = new Attribute("wind");

            final List<String> classes = new ArrayList<String>() {
                {
                    add("good");
                    add("moderate");
                    add("unhealthysensitive");
                    add("unhealthy");
                    add("veryunhealthy");
                    add("hazardous");
                }
            };

            // Instances(...) requires ArrayList<> instead of List<>...
            ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
                {
                    add(timestamp);
                    add(airquality);
                    add(currentLatitude);
                    add(currentLongitude);
                    Attribute attributeClass = new Attribute("@@class@@", classes);
                    add(attributeClass);
                }
            };

            // unpredicted data sets (reference to sample structure for new instances)
            Instances dataUnpredicted = new Instances("TestInstances",attributeList, 1);
            // last feature is target variable
            dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);


            // create new instance: this one should fall into the setosa domain
            DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
                {
                    setValue(timestamp, 1579032166);
                    setValue(airquality, 310);
                    setValue(currentLatitude, 62.60);
                    setValue(currentLongitude, 94.56);
                }
            };
            // reference to dataset
            newInstance.setDataset(dataUnpredicted);


            // predict new sample
            try {
                double result = wekaClassifier.classifyInstance(newInstance);
                String className = classes.get(new Double(result).intValue());
                String msg = "timestamp: " + 1 + ", airquality: " + className + ", actual: " + 1;
                Log.d(WEKA_TEST, msg);
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static final String WEKA_TEST = "WekaTest";

    private Random mRandom = new Random();

    private Sample[] mSamples = new Sample[]{
            new Sample(1, 0, new double[]{5, 3.5, 2, 0.4}), // should be in the setosa domain
            new Sample(2, 1, new double[]{5.6, 3, 3.5, 1.2}), // should be in the versicolor domain
            new Sample(3, 2, new double[]{7, 3, 6.8, 2.1}) // should be in the virginica domain
    };

    public void machineLearning(Context ctx)
    {
        Classifier mClassifier=null;

        // show samples
        StringBuilder sb = new StringBuilder("Samples:\n");
        for(Sample s : mSamples) {
            sb.append(s.toString() + "\n");
        }

        Log.e("machineLearning", sb.toString());
        Log.e(WEKA_TEST, "onCreate() finished.");


        //Load model
        AssetManager assetManager = ctx.getAssets();
        try {
           mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("iris_model_logistic_allfeatures.model"));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Weka "catch'em all!"
            e.printStackTrace();
        }
        Toast.makeText(ctx, "Model loaded.", Toast.LENGTH_SHORT).show();



        //Predict model
        Log.e(WEKA_TEST, "onClickButtonPredict()");

        if(mClassifier==null){
            Toast.makeText(ctx, "Model not loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        // we need those for creating new instances later
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
            Log.e(WEKA_TEST, msg);
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}


