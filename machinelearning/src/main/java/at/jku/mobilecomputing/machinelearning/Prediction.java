package at.jku.mobilecomputing.machinelearning;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public void loadTrainingSet(Context ctx, InputStream arffFile, double lat, double lng, String current_temp, String current_pressure, String current_humd, String current_wind) {


        Filter filter = new Normalize();

        this.context = ctx;
        ArffLoader loader;
        Instances dataSet;

        currentLatitude = lat;
        currentLongitude = lng;

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
            modelClassifier(classifier_result, current_temp, current_pressure, current_humd, current_wind);

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
    public void modelClassifier(Classifier classifier_result, String current_temp, String current_pressure, String current_humd, String current_wind) {
        try {
            ArrayList attributes = new ArrayList();
            ArrayList classVal = new ArrayList();
            Instances dataRaw;

            Attribute timestamp = new Attribute("timestamp");
            Attribute latitude = new Attribute("currentLatitude");
            Attribute longitude = new Attribute("currentLongitude");
            Attribute temperature = new Attribute("temperature");
            Attribute pressure = new Attribute("pressure");
            Attribute wind = new Attribute("wind");
            Attribute humidity = new Attribute("humidity");

            classVal.add("good");
            classVal.add("moderate");
            classVal.add("unhealthysensitive");
            classVal.add("unhealthy");
            classVal.add("veryunhealthy");
            classVal.add("hazardous");
            attributes.add(timestamp);
            attributes.add(latitude);
            attributes.add(longitude);
            attributes.add(temperature);
            attributes.add(pressure);
            attributes.add(wind);
            attributes.add(humidity);

            attributes.add(new Attribute("class", classVal));
            dataRaw = new Instances("TestInstances", attributes, 0);
            dataRaw.setClassIndex(dataRaw.numAttributes() - 1);

            //build current dataset
            if (dataRaw != null) dataRaw.clear();
            //Morning
            double[] instanceValue1 = new double[]{getTomorrowTimeStamp("09:00:00"), currentLatitude, currentLongitude, Double.parseDouble(current_temp), Double.parseDouble(current_pressure),
                    Double.parseDouble(current_wind), Double.parseDouble(current_humd)};//Current latitude & Current Longitude
            dataRaw.add(new DenseInstance(1.0, instanceValue1));

            //Afternoon
            instanceValue1 = new double[]{getTomorrowTimeStamp("01:00:00"), currentLatitude, currentLongitude, Double.parseDouble(current_temp), Double.parseDouble(current_pressure),
                    Double.parseDouble(current_wind), Double.parseDouble(current_humd + 80)};//Current latitude & Current Longitude
            dataRaw.add(new DenseInstance(1.0, instanceValue1));

            //Evening
            instanceValue1 = new double[]{getTomorrowTimeStamp("05:00:00"), currentLatitude, currentLongitude, Double.parseDouble(current_temp), Double.parseDouble(current_pressure),
                    Double.parseDouble(current_wind), Double.parseDouble(current_humd)};//Current latitude & Current Longitude
            dataRaw.add(new DenseInstance(1.0, instanceValue1));

            //Night
            instanceValue1 = new double[]{getTomorrowTimeStamp("09:00:00"), currentLatitude, currentLongitude, Double.parseDouble(current_temp), Double.parseDouble(current_pressure),
                    Double.parseDouble(current_wind), Double.parseDouble(current_humd)};//Current latitude & Current Longitude
            dataRaw.add(new DenseInstance(1.0, instanceValue1));

            //classify
            Classifier cls = null;
            try {
                for (int i = 0; i < dataRaw.size(); i++) {
                    cls = classifier_result;
                    String result = (String) classVal.get((int) cls.classifyInstance(dataRaw.instance(i)));
                    Log.e("Final prediction result: ", result);
                    Toast.makeText(context, "results:" + result, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception ex) {
                Log.e("modelClassifier", ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getTomorrowTimeStamp(String time) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String dateString = tomorrowDate() + " " + time;
        //formatting the dateString to convert it into a Date
        Date date = sdf.parse(dateString);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        System.out.println("Given Time in milliseconds : " + calendar.getTimeInMillis());
        return calendar.getTimeInMillis();
    }

    public String tomorrowDate() {
        Date todayDate = new Date();
        Date tomorrowDate = null;
        if (todayDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(todayDate); // Setting the today date
            c.add(Calendar.DATE, 1); // Increasing 1 day
            tomorrowDate = c.getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formatedDate = null;
        if (tomorrowDate != null) {
            formatedDate = sdf.format(tomorrowDate);
        }
        System.out.println(formatedDate);
        return formatedDate;
    }

    //**************************************************************************************************


}


