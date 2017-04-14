package jpstarkey.symptracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.renderscript.Element;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BaseDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;

import static android.R.attr.data;
import static android.R.attr.format;
import static android.R.attr.fragment;
import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.M;
import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;
import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;
import static jpstarkey.symptracker.R.id.seekPainLevel;
import static jpstarkey.symptracker.R.id.view;

import com.google.android.gms.common.api.GoogleApiClient;



public class MainActivity extends AppCompatActivity
        implements Daily.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener,
        Home.MyFragmentCallBack,
        Report.OnFragmentInteractionListener,
        Medications.OnFragmentInteractionListener,
        Symptoms.OnFragmentInteractionListener,
        AddDialog.OnFragmentInteractionListener,
        EditDialog.OnFragmentInteractionListener
{

    //Navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private TextView currentTotalSteps;
    private Context mContext;

    private PendingIntent pendingIntent;

     /**Google fitness API
 *  https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/
 **/
    public static final String TAG = "BasicHistoryApi";
    private static final String AUTH_PENDING = "auth_state_pending";
    private static boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;
    private float scaledStepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Replaces the actionbar with my toolbar layout:
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Finds the layout for the navigation drawer:
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Set the toggle for open/close of navbar
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        //Setup the drawer view:
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);

        //Set the home page on first load
        MenuItem homeItem = nvDrawer.getMenu().findItem(R.id.nav_home_fragment);
        selectDrawerItem(homeItem);

        //Google fitness API Auth:
        if(savedInstanceState != null)
        {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        //Initialize and build the google fit API client
        buildFitnessClient();

        //Store the application context
        this.mContext = getApplicationContext();

        //Store the Fitness Client in the applicationContext for use in other fragments
        if (mClient != null)
        {
            GlobalState state = ((GlobalState) getApplicationContext());
            state.setMClient(mClient);

        }

        //createNotification(0, R.drawable.ic_accessibility, "Test", "Test body");

        Intent alarmIntent = new Intent(MainActivity.this, myAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);


    }

    //region Google Fit API
    /**
     *  https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or
     *  having multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                GlobalState state = ((GlobalState) getApplicationContext());
                                state.setDailySteps();
                                //Update the currentTotalSteps on the homepage
                                currentTotalSteps = (TextView) findViewById(R.id.tvTotalSteps);
                                if (currentTotalSteps != null)
                                {
                                    currentTotalSteps.setText(Float.toString(state.getDailySteps()));

                                }
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                               result.toString());
                        Snackbar.make(
                                MainActivity.this.findViewById(R.id.flContent),
                                "Exception while connecting to Google Play services: " +
                                        result.getErrorMessage(),
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();
    }

    //Just gets the daily total steps for current  day (could be used on homeScreen TODO)
    public float getDailySteps()
    {
        if (mClient.isConnected())
        {
            Fitness.HistoryApi.readDailyTotal(mClient, TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<DailyTotalResult>()
                    {
                        @Override
                        public void onResult(@NonNull DailyTotalResult totalResult)
                        {
                            if (totalResult.getStatus().isSuccess())
                            {
                                DataSet totalSet = totalResult.getTotal();
                                long total = (totalSet == null) || totalSet.isEmpty()
                                        ? 0
                                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                scaledStepCount = total; /// 100;
                                String strStepCount = String.valueOf(scaledStepCount);
                                Log.i(TAG, "Scaled Step count:" + strStepCount);
                            }
                            else
                            {

                            }
                        }

                    });
           // return scaledStepCount;
        } else if (!mClient.isConnecting())
        {
            mClient.connect();
        }

        return scaledStepCount;
    }

    public int getStepsCount(long startTime, long endTime) {
        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms").build();


        PendingResult<DataReadResult> pendingResult = Fitness.HistoryApi
                .readData(
                        mClient,
                        new DataReadRequest.Builder()
                                .aggregate(TYPE_STEP_COUNT_DELTA,
                                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                                .bucketByTime(1, TimeUnit.DAYS)
                                .setTimeRange(startTime, endTime,
                                        TimeUnit.MILLISECONDS).build());
        int steps = 0;
        DataReadResult dataReadResult = pendingResult.await();
        if (dataReadResult.getBuckets().size() > 0) {
            //Log.e("TAG", "Number of returned buckets of DataSets is: "
            //+ dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            steps += dp.getValue(field).asInt();
                        }
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
                    for (Field field : dp.getDataType().getFields()) {
                        steps += dp.getValue(field).asInt();
                    }
                }
            }
        }
        return steps;

//region old
        //        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
//                .setDataType(TYPE_STEP_COUNT_DELTA)
//                .setType(DataSource.TYPE_DERIVED)
//                .setStreamName("estimated_steps")
//                .setAppPackageName("com.google.android.gms").build();
//        PendingResult<DataReadResult> pendingResult = Fitness.HistoryApi
//                .readData(
//                        mClient,
//                        new DataReadRequest.Builder()
//                                .aggregate(TYPE_STEP_COUNT_DELTA,
//                                        DataType.AGGREGATE_STEP_COUNT_DELTA)
//                                .bucketByTime(1, TimeUnit.DAYS)
//                                .setTimeRange(startTime, endTime,
//                                        TimeUnit.MILLISECONDS).build());
//        int steps = 0;
//        DataReadResult dataReadResult = pendingResult.await();
//        if (dataReadResult.getBuckets().size() > 0) {
//            //Log.e("TAG", "Number of returned buckets of DataSets is: "
//            //+ dataReadResult.getBuckets().size());
//            for (Bucket bucket : dataReadResult.getBuckets()) {
//                List<DataSet> dataSets = bucket.getDataSets();
//                for (DataSet dataSet : dataSets) {
//                    for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
//                        for (Field field : dp.getDataType().getFields()) {
//                            steps += dp.getValue(field).asInt();
//                        }
//                    }
//                }
//            }
//        } else if (dataReadResult.getDataSets().size() > 0) {
//            for (DataSet dataSet : dataReadResult.getDataSets()) {
//                for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
//                    for (Field field : dp.getDataType().getFields()) {
//                        steps += dp.getValue(field).asInt();
//                    }
//                }
//            }
//        }
//        return steps;
        //endregion
    }
    //endregion

    @Override
    public void theMethod()
    {
        scheduleAlarm();
    }

    //Background task to return the weekly number of steps for the connected google account,
    //to be used when graphing the weekly report of steps/symptoms
    public class getWeeklyDataTask extends AsyncTask<Void, Void, LinkedHashMap<Long, Integer>>
    {
        CombinedChart mChart;
        Activity context;

        public getWeeklyDataTask(GoogleApiClient client, CombinedChart chart, Activity context)
        {
            this.mChart = chart;
            mClient = client;
            this.context = context;

            if(context != null)
            {
                mContext = context;
            }
        }

        protected LinkedHashMap<Long, Integer> doInBackground(Void ...params) {
            LinkedHashMap<Long, Integer> days = new LinkedHashMap<>();

            Calendar C = Calendar.getInstance();
            C.setTime(new Date());
            long end = 0;
            long start = 0;
            for (int i = 1; i < 8; i++)
            {
                //If we just started, get the current day as end
                if (i == 1)
                {
                    end = C.getTimeInMillis();
                }

                //Go back a day
                C.add(Calendar.DAY_OF_WEEK, -1);
                start = C.getTimeInMillis();

                days.put(end, getStepsCount(start,end));
                end = start;
            }

            for (Map.Entry<Long, Integer> entry : days.entrySet())
            {
                Log.i("DoInBackground", "Date: " + entry.getKey() + " Steps: " + entry.getValue());
            }

            return days;
        }

        //After we receive step data render the graph
        protected void onPostExecute(LinkedHashMap<Long, Integer> result)
        {
            if (mChart != null)
            {
                CombinedData data = new CombinedData();

                data.setData(generateBarData(result));
                data.setData(generateLineData(result));

                IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mChart);
                XAxis xAxis = mChart.getXAxis();
                xAxis.setValueFormatter(xAxisFormatter);


                xAxis.setAxisMaximum(data.getXMax() + 0.25f);
                mChart.setData(data);
                mChart.invalidate();
            }
        }

        public int getStepsCount(long startTime, long endTime) {
            DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                    .setDataType(TYPE_STEP_COUNT_DELTA)
                    .setType(DataSource.TYPE_DERIVED)
                    .setStreamName("estimated_steps")
                    .setAppPackageName("com.google.android.gms").build();
            PendingResult<DataReadResult> pendingResult = Fitness.HistoryApi
                    .readData(
                            mClient,
                            new DataReadRequest.Builder()
                                    .aggregate(TYPE_STEP_COUNT_DELTA,
                                            DataType.AGGREGATE_STEP_COUNT_DELTA)
                                    .bucketByTime(1, TimeUnit.DAYS)
                                    .setTimeRange(startTime, endTime,
                                            TimeUnit.MILLISECONDS).build());
            int steps = 0;
            DataReadResult dataReadResult = pendingResult.await();
            if (dataReadResult.getBuckets().size() > 0) {
                //Log.e("TAG", "Number of returned buckets of DataSets is: "
                //+ dataReadResult.getBuckets().size());
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
                            for (Field field : dp.getDataType().getFields()) {
                                steps += dp.getValue(field).asInt();
                            }
                        }
                    }
                }
            } else if (dataReadResult.getDataSets().size() > 0) {
                for (DataSet dataSet : dataReadResult.getDataSets()) {
                    for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
                        for (Field field : dp.getDataType().getFields()) {
                            steps += dp.getValue(field).asInt();
                        }
                    }
                }
            }
            return steps;
        }
    }

    //region generate bar and line data from results
    private BarData generateBarData(LinkedHashMap<Long, Integer> result) {

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();

        List<Long> keyList = new ArrayList<Long>(result.keySet());
        Calendar C = Calendar.getInstance();

        for (int i=keyList.size() - 1; i >= 0; i--)
        {
            Long key = keyList.get(i);
            C.setTimeInMillis(key);
            int dValue = C.get(Calendar.DAY_OF_MONTH);
            entries1.add(new BarEntry(dValue, result.get(key)));
        }
        BarDataSet set1 = new BarDataSet(entries1, "Steps");
        set1.setColor(Color.rgb(60, 220, 78));
        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        float barWidth = 1f;
        BarData d = new BarData(set1);
        d.setBarWidth(barWidth);
        set1.setBarBorderWidth(1f);
        return d;
    }

    private LineData generateLineData(LinkedHashMap<Long, Integer> result) {
        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();
        List<Long> keyList = new ArrayList<Long>(result.keySet());

        Calendar C = Calendar.getInstance();

        for (int i=keyList.size() - 1; i >=0; i--)
        {
            Long key = keyList.get(i);
            C.setTimeInMillis(key);
            int dValue = C.get(Calendar.DAY_OF_MONTH);
            entries.add(new BarEntry(dValue, getPainLevelForDay(key)));
        }

        LineDataSet set = new LineDataSet(entries, "Pain level");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        d.addDataSet(set);

        return d;
    }
//endregion

    public int getPainLevelForDay(long date)
    {
        int painLevel = 0;
        //Retrieve the pain level from the database based on date
        //If nothing exists, return 0 as user likely hasn't inserted a painLevel for this date
        Calendar C = Calendar.getInstance();
        C.setTimeInMillis(date);
        int day = C.get(Calendar.DAY_OF_MONTH);
        int month = C.get(Calendar.MONTH) + 1; //index of month starts at 0
        int year = C.get(Calendar.YEAR);



        if(mContext != null)
        {
            DatabaseHelper db = DatabaseHelper.getInstance(mContext);

            painLevel = db.getDailyLog(date).getPain();

            //List<DailyLog> debug = db.getAllDailyLogs();
            //Log.i("TAG", Integer.toString(debug.size()));
        }



        //Insert daily log into DB



        Log.i("PAINLEVEL", "painlevel = " + Integer.toString(painLevel));
        return painLevel;
    }


    //region Hamburger icon animations
    private ActionBarDrawerToggle setupDrawerToggle()
    {
        //Make sure you pass in a valid toolbar reference
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        //Sync the toggle state
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        //Pass config change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Navigation drawer business logic
    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem)
                    {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }

    //Business logic to determine which fragment to show after clicking an item on the
    //navigation drawer.
    public void selectDrawerItem(MenuItem menuItem)
    {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_home_fragment:
                fragmentClass = Home.class;
                break;
            case R.id.nav_daily_fragment:
                fragmentClass = Daily.class;
                break;
            case R.id.nav_report_fragment:
                fragmentClass = Report.class;
                break;
            case R.id.nav_symptoms_fragment:
                fragmentClass = Symptoms.class;
                break;
            case R.id.nav_activities_fragment:
                fragmentClass = Daily.class; //TODO
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.nav_medications_fragment:
                fragmentClass = Medications.class;
                break;

            default:
                fragmentClass = MainActivity.class;
                break;
        }

        if (fragmentClass == null)
        {
            fragmentClass = MainActivity.class;
            Log.e("Fragment", "Fragment is null in navigation drawer");
        }

       // if (fragmentClass.getSuperclass() == PreferenceFragment.())
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
    //endregion

    //For fragment interaction:
    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }

    //region Background service
    public void start()
    {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 8000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }

    public void startAt10() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * 20;

        /* Set the alarm to start at 10:30 AM */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);

        /* Repeating on every 20 minutes interval */
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 20, pendingIntent);
    }

    //Sets a recurring alarm every half hour
    public void scheduleAlarm()
    {
        //Intent to execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), myAlarmReceiver.class);
        //Create a pendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, myAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis();

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    public void cancelAlarm()
    {
        Intent intent = new Intent(getApplicationContext(), myAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, myAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    //endregion

    //region Notifications
    private void createNotification(int nId, int iconRes, String title, String body) {

        Intent intent = new Intent(this, Daily.class);
        int requestID = (int) System.currentTimeMillis();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pIntent = PendingIntent.getActivity(this, requestID, intent, flags);

        Notification noti = new NotificationCompat.Builder(this)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pIntent)
                .setAutoCancel(true) //Hides notification after selected
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(nId, noti);
    }
    //endregion

    //Seperate class to format the X-axis on the graph so that appropriate dates are shown
    //Otherwise labels are useless to user
    public class DayAxisValueFormatter implements IAxisValueFormatter
    {
        private BarLineChartBase<?> chart;

        public DayAxisValueFormatter(BarLineChartBase<?> chart) {
            this.chart = chart;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            //Value will be day of current month, so we need to append the month to it
            int convertedInt = (int)value;

            Calendar C = Calendar.getInstance();
            C.setTime(new Date());

            int month = C.get(Calendar.MONTH) + 1; //Months are indexed from 1...

            return convertedInt + "/" + month;

        }
    }
}
