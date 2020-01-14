package at.jku.mobilecomputing.machinelearning;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class Prediction {


    Context context;
    double currentLatitude, currentLongitude;
    String ModelPath = "";

    public void loadTrainingSet(Context ctx, InputStream arffFile, double lat, double lng) {


        Filter filter = new Normalize();

        this.context = ctx;
        ArffLoader loader;
        Instances dataSet;

        try {
            //1. Loading training dataset

            loader = new ArffLoader();
            loader.setSource(arffFile);
            dataSet = loader.getDataSet();
            dataSet.setClassIndex(dataSet.numAttributes() - 1);

            //2. building the classifier
            Classifier classifier_result = buildClassifier(dataSet);
            Log.e("Classifier details: ", classifier_result.toString());

            //3. Evaluate the model
            String evaluate_result = evaluateModel(classifier_result, dataSet, dataSet);
            Log.e("Evaluation details: ", evaluate_result);

            //4. Save the model
            //File root = Environment.getExternalStorageDirectory();
            //saveModel(classifier_result, root.getPath() + "/generated.model");
            //ModelPath = root.getPath() + "/generated.model";

            //5. classification using the generated model
            modelClassifier(classifier_result);

           /* // Load classifer model
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
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //**************************************************************************************************
    //2. Building classifier for training set
    public Classifier buildClassifier(Instances traindataset) {

        //Print capabilities
        String[] options = new String[]{"-U"};

        J48 j48 = new J48();

        try {
            j48.setOptions(options);
            j48.buildClassifier(traindataset);
            Log.e("buildClassifier (J48): ", j48.getCapabilities().toString());
            Log.e("buildClassifier Tree (J48): ", j48.graph());

        } catch (Exception ex) {
            Log.e("buildClassifier: ", ex.getMessage());
        }
        return j48;
    }
    //**************************************************************************************************

    //3. Evaluating the accuracy for the generated model with test set
    public String evaluateModel(Classifier model, Instances traindataset, Instances testdataset) {
        Evaluation eval = null;
        try {
            // Evaluate classifier with test dataset
            eval = new Evaluation(traindataset);
            eval.evaluateModel(model, testdataset);
        } catch (Exception ex) {
            Log.e("evaluateModel: ", ex.getMessage());
        }
        return eval.toSummaryString("", true);
    }

    //**************************************************************************************************

    //4. Saving the generated model to a path to use it for future prediction
    public void saveModel(Classifier model, String modelpath) {

        try {
            SerializationHelper.write(modelpath, model);
        } catch (Exception ex) {
            Log.e("saveModel: ", ex.getMessage());
        }
    }

    //5. classification using the generated model
    public void modelClassifier(Classifier classifier_result) {
        try {
            ArrayList attributes = new ArrayList();
            ArrayList classVal = new ArrayList();
            Instances dataRaw;

            Attribute timestamp = new Attribute("timestamp");
            Attribute latitude = new Attribute("currentLatitude");
            Attribute longitude = new Attribute("currentLongitude");

            classVal.add("good");
            classVal.add("moderate");
            classVal.add("unhealthysensitive");
            classVal.add("unhealthy");
            classVal.add("veryunhealthy");
            classVal.add("hazardous");

            attributes.add(timestamp);
            attributes.add(latitude);
            attributes.add(longitude);

            attributes.add(new Attribute("class", classVal));
            dataRaw = new Instances("TestInstances", attributes, 0);
            dataRaw.setClassIndex(dataRaw.numAttributes() - 1);

            //build current dataset
            if (dataRaw != null) dataRaw.clear();
            double[] instanceValue1 = new double[]{new Date().getTime(), currentLatitude, currentLongitude};
            dataRaw.add(new DenseInstance(1.0, instanceValue1));

            //classify
            Classifier cls = null;
            try {
                cls = classifier_result;
                String result = (String) classVal.get((int) cls.classifyInstance(dataRaw.firstInstance()));
                Log.e("Final prediction result: ", result);
                Log.e("Final prediction result: ", result);
            } catch (Exception ex) {
                Log.e("modelClassifier", ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //**************************************************************************************************


}


