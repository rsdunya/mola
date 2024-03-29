import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Example of using the time series forecasting API. To compile and run the
 * CLASSPATH will need to contain:
 * <p/>
 * weka.jar (from your weka distribution)
 * pdm-timeseriesforecasting-ce-TRUNK-SNAPSHOT.jar (from the time series
 * package) jcommon-1.0.14.jar (from the time series package lib directory)
 * jfreechart-1.0.13.jar (from the time series package lib directory)
 */
public class TimeSeriesTest {

    public static void main(String[] args) {
        try {
            // path to the Australian wine data included with the time series
            // forecasting
            // package
            String pathToWineData = weka.core.WekaPackageManager.PACKAGES_DIR
                    .toString()
                    + File.separator
                    + "timeseriesForecasting"
                    + File.separator
                    + "sample-data"
                    + File.separator
                    + "appleStocks2011.arff";

            pathToWineData = weka.core.WekaPackageManager.PACKAGES_DIR
                    .toString() + File.separator + "forex-train.arff";

            // load the wine data
            Instances wine = new Instances(new BufferedReader(new FileReader(
                    pathToWineData)));

            // new forecaster
            WekaForecaster forecaster = new WekaForecaster();

            // set the targets we want to forecast. This method calls
            // setFieldsToLag() on the lag maker object for us
            // forecaster.setFieldsToForecast("Fortified,Dry-white");
            forecaster.setFieldsToForecast("close");

            // default underlying classifier is SMOreg (SVM) - we'll use
            // gaussian processes for regression instead
            forecaster.setBaseForecaster(new GaussianProcesses());

            forecaster.getTSLagMaker().setTimeStampField("time"); // date time
            // stamp
            forecaster.getTSLagMaker().setMinLag(1);
            forecaster.getTSLagMaker().setMaxLag(1); // monthly data

            // add a month of the year indicator field
            forecaster.getTSLagMaker().setAddMonthOfYear(true);

            // add a quarter of the year indicator field
            forecaster.getTSLagMaker().setAddQuarterOfYear(false);

            // build the model
            forecaster.buildForecaster(wine, System.out);

            // prime the forecaster with enough recent historical data
            // to cover up to the maximum lag. In our case, we could just supply
            // the 12 most recent historical instances, as this covers our
            // maximum
            // lag period
            forecaster.primeForecaster(wine);

            // forecast for 12 units (months) beyond the end of the
            // training data
            List<List<NumericPrediction>> forecast = forecaster.forecast(5,
                    System.out);

            // output the predictions. Outer list is over the steps; inner list
            // is over
            // the targets
            for (int i = 0; i < forecast.size(); i++) {
                List<NumericPrediction> predsAtStep = forecast.get(i);
                for (int j = 0; j < predsAtStep.size(); j++) {
                    NumericPrediction predForTarget = predsAtStep.get(j);
                    System.out.print("" + predForTarget.predicted() + " ");
                }
                System.out.println();
            }

            // we can continue to use the trained forecaster for further
            // forecasting
            // by priming with the most recent historical data (as it becomes
            // available).
            // At some stage it becomes prudent to re-build the model using
            // current
            // historical data.

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}