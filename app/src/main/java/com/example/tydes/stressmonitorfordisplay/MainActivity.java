package com.example.tydes.stressmonitorfordisplay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Debug_MA";
    private String bvpLine;
    private String edaLine;
    private String ibiLine;
    private int syncBvpEda;
    private float loopCount;
    private float currentTime;
    private float ibiSum;
    private float ibiAvg;
    private float hrAvg;
    private float ibiStd;
    private float currentIbi;
    private float ibiSampleSize;
    private float edaSum;
    private float edaAvg;
    private String edaAvgString;
    private String edaStdString;
    private String ibiAvgString;
    private String hrAvgString;
    private String ibiVarienceString;
    private String hrvString;
    private String pNN50String;
    private String tonicSlopeStinge;
    private String phasicSlopeString;
    private String estimationString;
    private String stressMonitorText;
    private TextView ibiUnitsText;
    private TextView edaUnitsText;
    private TextView hrUnitsText;
    private float edaStd;
    private float hrStd;
    private float currentEda;
    private float edaSampleSize;
    private float tonicSlope;
    private float ibiStdSum;
    private linearRegression edaTonicLinReg;
    private float phasicSlope;
    private int nn50Count;
    private float pNN50;
    private linearRegression edaPhasicLinReg;

    private List<Float> ibi60sSample;
    private List<Float> bvp60sSample;
    private List<Float> eda60sSample;
    private List<Float> ibiTimeSample;
    private List<Float> bvpTimeSample;
    private List<Float> edaTimeSample;
    private LineGraphSeries<DataPoint> bvpSeries;
    private LineGraphSeries<DataPoint> edaSeries;
    private GraphView graph;
    private boolean streamData = true;
    private boolean getIBI = true;
    private boolean hrShow = true;
    private boolean edaShow = true;
    private boolean hrvShow = false;
    private boolean edaStdShow = false;
    private boolean ibiShow = true;
    private boolean ibiVarienceShow = false;
    private boolean pNN50Show = false;
    private boolean bvpGraphShow = true;
    private boolean edaGraphShow = false;
    private boolean stressed = false;
    private TextView heartRateTextView;
    private TextView edaTextView;
    private TextView heartRateTitle;
    private TextView ibiTextView;
    private TextView edaTitle;
    private TextView ibiTitle;
    private TextView stressIndicatorDsiplay;
    private String[] svmData;
    private double[] coefficients;
    private double intercepts;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Connect to all of the xml layout components
        ibi60sSample = new ArrayList<Float>();
        bvp60sSample = new ArrayList<Float>();
        eda60sSample = new ArrayList<Float>();
        ibiTimeSample = new ArrayList<Float>();
        bvpTimeSample = new ArrayList<Float>();
        edaTimeSample = new ArrayList<Float>();
        heartRateTextView = (TextView) findViewById(R.id.hrTV);
        edaTextView = (TextView) findViewById(R.id.edaTV);
        heartRateTitle = (TextView) findViewById(R.id.hrTitle);
        ibiTextView = (TextView) findViewById(R.id.ibiTV);
        ibiTitle = (TextView) findViewById(R.id.ibiTitle);
        edaTitle = (TextView) findViewById(R.id.edaTitle);
        stressIndicatorDsiplay = (TextView) findViewById(R.id.stressIndicatorTV);
        ibiUnitsText = (TextView) findViewById(R.id.ibiUnitTV);
        edaUnitsText = (TextView) findViewById(R.id.edaUnitTv);
        hrUnitsText = (TextView) findViewById(R.id.hrUnitTV);

        //Connect and Configure the graph
        bvpSeries = new LineGraphSeries<DataPoint>();
        graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(bvpSeries);
        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(6);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.setTitle("BVP");
        graph.setTitleTextSize(100);
        graph.setTitleColor(Color.WHITE);

        readCSVdata();
    }


    private void readCSVdata() {

        //Read the Data from a Csv file
        InputStream bvpIS = getResources().openRawResource(R.raw.bvp);
        final BufferedReader bvpReader = new BufferedReader(
                new InputStreamReader(bvpIS ,Charset.forName("utf-8"))
        );
        InputStream edaIS = getResources().openRawResource(R.raw.eda);
        final BufferedReader edaReader = new BufferedReader(
                new InputStreamReader(edaIS ,Charset.forName("utf-8"))
        );
        InputStream ibiIS = getResources().openRawResource(R.raw.ibi);
        final BufferedReader ibiReader = new BufferedReader(
                new InputStreamReader(ibiIS ,Charset.forName("utf-8"))
        );

        syncBvpEda = 1;
        loopCount = 1;
        currentTime=0;
        bvpLine="";
        edaLine="";
        ibiLine="";

        //Begin a thread for csv reading
        //In this thread the csv files are synchronised as they have different sample rates
        new Thread(new Runnable() {
            public void run() {
                try {
                    bvpReader.readLine();
                    ibiReader.readLine();
                    edaReader.readLine();
                    edaReader.readLine();
                    edaReader.readLine();

                    final CSVdata ibiSample = new CSVdata();
                    final CSVdata bvpSample = new CSVdata();
                    final CSVdata edaSample = new CSVdata();
                    while ((bvpLine = bvpReader.readLine()) != null && streamData){
                        syncBvpEda ++;
                        currentTime = ((float) 1/64) * loopCount;

                        //delay 16ms to achieve sample rate close to 64Hz
                        Thread.sleep(16);

                        //Split the csv ","
                        String [] tokenBVP = bvpLine.split(",");
                        bvpSample.setCol_1(Float.parseFloat(tokenBVP[0]));

                        //Print bvp to log to ensure no error
                        Log.d(TAG, "BVP: " +  bvpSample.getCol_1() + " " + currentTime);

                        //Only get the IBI when its time scale matches the time scale of the bvp
                        //Puts the IBI in a stCol placeholder
                        if (getIBI){
                            if ((ibiLine = ibiReader.readLine()) != null) {
                                String [] tokenIbi = ibiLine.split(",");
                                ibiSample.setCol_1(Float.parseFloat(tokenIbi[0]));
                                ibiSample.setCol_2(Float.parseFloat(tokenIbi[1]));
                                getIBI = false;
                            }
                        }

                        //Checking if we read the ibi and put it in the set_col1
                        if (ibiSample.getCol_1() != 0){
                            if (ibiSample.getCol_1()<=currentTime){
                                Log.d("Reader", "IBI: " + ibiSample.getCol_1() + " At time: " + ibiSample.getCol_2());
                                ibi60sSample.add(ibiSample.getCol_2());
                                ibiTimeSample.add(ibiSample.getCol_1());
                                while ((currentTime - 60) > ibiTimeSample.get(0)) {
                                    ibiTimeSample.remove(0);
                                    ibi60sSample.remove(0);
                                }
                                getIBI = true;
                            }
                        }

                        //Only pull data from the eda when its time scale matched the bvp
                        if(syncBvpEda==16 ){
                            if((edaLine = edaReader.readLine()) != null ) {
                                String[] tokenEDA = edaLine.split(",");
                                edaSample.setCol_1(Float.parseFloat(tokenEDA[0]));
//                                Log.d(TAG, "EDA: " + edaSample.getCol_1() + " " + currentTime);
                                eda60sSample.add((edaSample.getCol_1()));
                                edaTimeSample.add(currentTime);
                                while ((currentTime-60) > edaTimeSample.get(0)){
                                    edaTimeSample.remove(0);
                                    eda60sSample.remove(0);
                                }
                                syncBvpEda = 0;
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                  Log.d("UIThread:  ", "Gc: " + bvpSample.getCol_1() + "Time: " +currentTime);
                                //Update the plot with the bvp data
                                bvpSeries.appendData(new DataPoint(currentTime, bvpSample.getCol_1()), true, 512);
                                bvpSeries.setDrawBackground(true);
                                bvpSeries.setAnimated(true);
                                bvpSeries.setColor(Color.WHITE);


                            }
                        });

                        //Run calculation of statistical features if the time sample is at least greater than 15 seconds
                        if(currentTime%1.5 == 0 && currentTime > 15) {
                            calcStatisticalFeatures(eda60sSample, edaTimeSample, ibi60sSample, ibiTimeSample);
                        }

                        loopCount++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }

    private void calcStatisticalFeatures(final List<Float> eda60sSample, final List<Float> edaTimeSample,
                                         final List<Float> ibi60sSample, final List<Float> ibiTimeSample) {
        ibiSum = 0;
        hrAvg = 0;
        currentIbi = 0;
        ibiSampleSize = (float) ibi60sSample.size();
        edaSum = 0;
        edaAvg = 0;
        nn50Count= 0;
        ibiStdSum = 0;
        currentEda = 0;
        edaSampleSize = (float) eda60sSample.size();


        new Thread(new Runnable() {
            public void run() {
                if(ibiSampleSize>0) {
                    for (int i = 0; i < (ibiSampleSize - 1); i++) {
                        currentIbi = ibi60sSample.get(i);
                        ibiSum += currentIbi;
                    }

                    ibiAvg = ibiSum / ibiSampleSize;
                    hrAvg = Math.round(((float) 60/ibiAvg) * 10);
                    hrAvg = hrAvg/10;
                }


                if(edaSampleSize>0) {
                    edaTonicLinReg = new linearRegression(edaTimeSample, eda60sSample);
                    tonicSlope = (float) edaTonicLinReg.slope();
                    edaPhasicLinReg = new linearRegression(edaTimeSample, eda60sSample);
                    phasicSlope = (float) edaTonicLinReg.slope();
                    Log.d("Tonic", " " + tonicSlope + " " + phasicSlope +
                            " time " + edaTimeSample.get(edaTimeSample.size()-1));
                    for (int i = 0; i < (edaSampleSize - 1); i++) {
                        currentEda = eda60sSample.get(i);
                        edaSum += currentEda;
                    }
                    for (int i = 0; i < ibiSampleSize - 1; i++) {
                        ibiStdSum += Math.pow((ibi60sSample.get(i) - ibiAvg), 2);
                        if (i < ibiSampleSize) {
                            if (Math.abs(ibi60sSample.get(i) - ibi60sSample.get(i + 1)) > 0.05) {
                                nn50Count++;
                            }
                        }
                    }

                    edaAvg = edaSum / edaSampleSize;
                    for (int i = 0; i < edaSampleSize - 1; i++) {
                        edaStd += Math.pow((eda60sSample.get(i) - edaAvg), 2);
                    }


                    ibiStd = (float) Math.sqrt(ibiStdSum / (ibiSampleSize));
                    edaStd = (float) Math.sqrt(edaStd / (edaSampleSize));
                    pNN50 = (nn50Count / (ibiSampleSize)) * 100;
                    hrStd = ibiStd * 60;

                    //Fort mat the xml text to be a string
                    //.3f limits the value to 3 decimal places
                    //This was used becuase there an be problems in rounding float values
                    edaAvgString = String.format("%.3f", edaAvg);
                    edaStdString = String.format("%.3f", edaStd);
                    ibiAvgString = String.format("%.3f", ibiAvg);
                    hrAvgString = String.format("%.2f", hrAvg);
                    ibiVarienceString = String.format("%.3f", ibiStd);
                    hrvString = String.format("%.2f", hrStd);
                    pNN50String = String.format("%.2f", pNN50);
                    tonicSlopeStinge = Float.toString(tonicSlope);
                    phasicSlopeString = Float.toString(phasicSlope);
                    Log.d("STD: ", " " + edaStd);

                }

                //Update xml layout with the new data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(hrAvg != 0 && hrShow) {
                            heartRateTextView.setText(hrAvgString);
                            heartRateTitle.setText("Heart Rate");

                        }else if(hrStd != 0 && hrvShow) {
                            heartRateTextView.setText(hrvString);
                            heartRateTitle.setText("HRV");
                        }

                        if(ibiAvg != 0 && ibiShow) {
                            ibiTextView.setText(ibiAvgString);
                            ibiTitle.setText("IBI");
                            ibiUnitsText.setText("(s)");
                        }else if(ibiAvg != 0 && ibiVarienceShow) {
                            ibiTextView.setText(ibiVarienceString);
                            ibiTitle.setText("IBI Std.");
                        }else if(pNN50Show) {
                            ibiTextView.setText(pNN50String);
                            ibiTitle.setText("pNN50");
                            ibiUnitsText.setText("(%)");
                        }

                        if(edaAvg != 0 && edaShow) {
                            edaTextView.setText(edaAvgString);
                            edaTitle.setText("EDA");
                        }else if(edaStd != 0 && edaStdShow) {
                            edaTextView.setText(edaStdString);
                            edaTitle.setText("EDA Std.");
                        }

                    }
                });

                //Run classifier to detect stress
                svmData = new String[]{ibiAvgString, ibiVarienceString, pNN50String,
                        edaAvgString, edaStdString, tonicSlopeStinge, phasicSlopeString};
//                svmData = new String[]{"0.517396825", "0.028052977", "6.451612903", "1.139908333", "0.058088581", "0", "0"};

                svmClassifier(svmData);

            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.disconnectItem:
                Toast.makeText(getApplicationContext(), "Disconnecting...", Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);startActivity(intent);
                        streamData = false;
                    }
                }, 2500);   //2.5 seconds
        }
        return super.onOptionsItemSelected(item);
    }

    public void hrViewSwitch(View view){
        if (hrShow){
            hrShow = false;
            hrvShow = true;
            heartRateTextView.setText(" ");
            heartRateTitle.setText("Heart Rate");
        }else if (hrvShow){
            hrShow = true;
            hrvShow = false;
            heartRateTextView.setText(" ");
            heartRateTitle.setText("HRV");
        }
    }
    public void ibiViewSwitch(View view){
        if (ibiShow){
            ibiShow = false;
            ibiVarienceShow = true;
            ibiTextView.setText(" ");
            ibiTitle.setText("IBI");
        }else if (ibiVarienceShow){
            pNN50Show = true;
            ibiVarienceShow = false;
            ibiTextView.setText(" ");
            ibiTitle.setText("IBI Std.");
        }else if (pNN50Show){
            ibiShow = true;
            pNN50Show = false;
            ibiTextView.setText(" ");
            ibiTitle.setText("pNN50");
        }
    }

    public void edaViewSwitch(View view){
        if (edaShow){
            edaShow = false;
            edaStdShow = true;
            edaTextView.setText(" ");
            edaTitle.setText("EDA");
        }else if (edaStdShow){
            edaShow = true;
            edaStdShow = false;
            edaTextView.setText(" ");
            edaTitle.setText("EDA Std.");
        }
    }

    public void graphSwitchView(View view){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (bvpGraphShow) {
//                    bvpGraphShow = false;
//                    edaGraphShow = true;
//                    graph.removeAllSeries();
//                } else if (edaGraphShow) {
//                    bvpGraphShow = true;
//                    edaGraphShow = false;
//                    graph.removeAllSeries();
//                }
            }
        });

    }

    public int predict(double[] features) {
        double prob = 0.;
        for (int i = 0, il = this.coefficients.length; i < il; i++) {
            prob += this.coefficients[i] * features[i];
        }
        if (prob + this.intercepts > 0) {
            return 1;
        }
        return 0;
    }

    public void svmClassifier(String[] args) {
        if (args.length == 7) {

            // Features:
            double[] features = new double[args.length];
            for (int i = 0, l = args.length; i < l; i++) {
                features[i] = Double.parseDouble(args[i]);
            }

            // Parameters:
//            double[] coefficients = {-1.36738488082, -17.6755236949, 0.0336051987855, -1.03336182252, -0.277909744711, 0.46407919792, -2.39928849844};
//            double intercepts = 2.55030905175;
//            double[] coefficients = {1.25705438351, 0.479368017079, 0.00558641889707, 1.33439600244, -0.344226352829, -0.488332775586, 2.09159849548};
//            double intercepts = -2.86943398256;
            double[] coefficients = {4.46556575459, -4.46593776833, 0.00288702791778, 1.16625631036, 1.98910956778, 2.48670995174, 1.15357577775};
            double intercepts = -4.55439333174;


            // Prediction:
            LinearSVC clf = new LinearSVC(coefficients, intercepts);
            final int estimation = clf.predict(features);
//            System.out.println(estimation);
            Log.d("Result: ", " " + estimation);

            estimationString = Integer.toString(estimation);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (estimation==1){
                        stressMonitorText = "Stress Detected";
                        stressIndicatorDsiplay.setBackgroundResource(R.drawable.stressed_background);
                        stressed=true;
                    }else{
                        stressMonitorText = "No Stress detected";
                        stressIndicatorDsiplay.setBackgroundResource(R.drawable.relaxed_background);
                        stressed=false;
                    }
                    stressIndicatorDsiplay.setText(stressMonitorText);
                }
            });

        }
    }


}
