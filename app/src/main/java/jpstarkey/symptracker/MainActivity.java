package jpstarkey.symptracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.M;
import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity
        implements Daily.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener,
        Home.MyFragmentCallBack,
        Report.OnFragmentInteractionListener
{

    //Navigation drawer
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

     /**Google fitness API
     *  https://github.com/googlesamples/android-fit/blob/master/BasicHistoryApi/
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
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
        //Google fitness API:
        if(savedInstanceState != null)
        {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();

        createNotification(0, R.drawable.ic_accessibility, "Test", "Test body");

        //scheduleAlarm();


    }

    //region Google Fit API
    /**
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
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!
                                new InsertAndVerifyDataTask().execute();



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

    public float getDailySteps()
    {
        if (mClient.isConnected())
        {
            Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA)
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

    @Override
    public void theMethod()
    {
        scheduleAlarm();
    }

    /**
     *  Create a {@link DataSet} to insert data into the History API, and
     *  then create and execute a {@link DataReadRequest} to verify the insertion succeeded.
     *  By using an {@link AsyncTask}, we can schedule synchronous calls, so that we can query for
     *  data after confirming that our insert was successful. Using asynchronous calls and callbacks
     *  would not guarantee that the insertion had concluded before the read request was made.
     *  An example of an asynchronous call using a callback can be found in the example
     *  on deleting data below.
     */
    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... params) {
            // Create a new dataset and insertion request.
//            DataSet dataSet = insertFitnessData();
//
//            // [START insert_dataset]
//            // Then, invoke the History API to insert the data and await the result, which is
//            // possible here because of the {@link AsyncTask}. Always include a timeout when calling
//            // await() to prevent hanging that can occur from the service being shutdown because
//            // of low memory or other conditions.
//            Log.i(TAG, "Inserting the dataset in the History API.");
//            com.google.android.gms.common.api.Status insertStatus =
//                    Fitness.HistoryApi.insertData(mClient, dataSet)
//                            .await(1, TimeUnit.MINUTES);
//
//            // Before querying the data, check to see if the insertion succeeded.
//            if (!insertStatus.isSuccess()) {
//                Log.i(TAG, "There was a problem inserting the dataset.");
//                return null;
//            }
//
//            // At this point, the data has been inserted and can be read.
//           Log.i(TAG, "Data insert was successful!");
//            // [END insert_dataset]

            // Begin by creating the query.

            // Setting a start and end date using a range of 1 week before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_WEEK, -2);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = queryWeekFitnessData(startTime, endTime);
            // [START read_dataset]
            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            // [END read_dataset]

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            printData(dataReadResult);
//
//            Calendar cal = Calendar.getInstance();
//            Date now = new Date();
//            cal.setTime(now);
//            //Set calendar 1 week back
//            cal.add(Calendar.WEEK_OF_YEAR, -1);
//
//            ArrayList<Long> startTimes = new ArrayList<>();
//            ArrayList<Long> endTimes = new ArrayList<>();
//
//            for (int i = 1; i < 8; i++)
//            {
//                long endTime = cal.getTimeInMillis();
//                cal.add(Calendar.DAY_OF_WEEK, i);
//                long startTime = cal.getTimeInMillis();
//                startTimes.add(startTime);
//                endTimes.add(endTime);
//            }
//
//            for (int i = 0; i < startTimes.size(); i++)
//            {
//                DataReadRequest readRequest = queryWeekFitnessData(startTimes.get(i), endTimes.get(i));
//                // [START read_dataset]
//                // Invoke the History API to fetch the data with the query and await the result of
//                // the read request.
//                DataReadResult dataReadResult =
//                        Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
//                // [END read_dataset]
//
//                // For the sake of the sample, we'll print the data so we can see what we just added.
//                // In general, logging fitness information should be avoided for privacy reasons.
//                printData(dataReadResult);
//            }
            return null;
        }
    }

    /**
     * Create and return a {@link DataSet} of step count data for insertion using the History API.
     */
//    private DataSet insertFitnessData() {
//        //Log.i(TAG, "Creating a new data insert request.");
//
//        // [START build_insert_data_request]
//        // Set a start and end time for our data, using a start time of 1 hour before this moment.
//        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
//        cal.setTime(now);
//        long endTime = cal.getTimeInMillis();
//        cal.add(Calendar.HOUR_OF_DAY, -1);
//        long startTime = cal.getTimeInMillis();
//
//        // Create a data source
//        DataSource dataSource = new DataSource.Builder()
//                .setAppPackageName(this)
//                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
//                .setStreamName(TAG + " - step count")
//                .setType(DataSource.TYPE_RAW)
//                .build();
//
//        // Create a data set
//        int stepCountDelta = 950;
//        DataSet dataSet = DataSet.create(dataSource);
//        // For each data point, specify a start time, end time, and the data value -- in this case,
//        // the number of new steps.
//        DataPoint dataPoint = dataSet.createDataPoint()
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
//        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
//        dataSet.add(dataPoint);
//        // [END build_insert_data_request]
//
//        return dataSet;
//    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
//    public static DataReadRequest queryFitnessData() {
//        // [START build_read_data_request]
//        // Setting a start and end date using a range of 1 week before this moment.
//        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
//        cal.setTime(now);
//        long endTime = cal.getTimeInMillis();
//        cal.add(Calendar.WEEK_OF_YEAR, -1);
//        long startTime = cal.getTimeInMillis();
//
//        java.text.DateFormat dateFormat = getDateInstance();
//        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
//        Log.i(TAG, "Range End: " + dateFormat.format(endTime));
//
//        DataReadRequest readRequest = new DataReadRequest.Builder()
//                // The data request can specify multiple data types to return, effectively
//                // combining multiple data queries into one call.
//                // In this example, it's very unlikely that the request is for several hundred
//                // datapoints each consisting of a few steps and a timestamp.  The more likely
//                // scenario is wanting to see how many steps were walked per day, for 7 days.
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
//                // bucketByTime allows for a time span, whereas bucketBySession would allow
//                // bucketing by "sessions", which would need to be defined in code.
//                .bucketByTime(1, TimeUnit.DAYS)
//                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//                .build();
//        // [END build_read_data_request]
//
//        return readRequest;
//    }

    public static DataReadRequest queryWeekFitnessData(long startTime, long endTime)
    {
        // [START build_read_data_request]


        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Log a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would
     * dump all the data. In this sample, logging also prints to the device screen, so we can see
     * what the query returns, but your app should not log fitness information as a privacy
     * consideration. A better option would be to dump the data you receive to a local data
     * directory to avoid exposing it to other applications.
     */
    public static void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
    // [END parse_dataset]

    /**
     * Delete a {@link DataSet} from the History API. In this example, we delete all
     * step count data for the past 24 hours.
     */
    private void deleteData() {
        Log.i(TAG, "Deleting today's step count data.");

        // [START delete_dataset]
        // Set a start and end time for our data, using a start time of 1 day before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //  Create a delete request object, providing a data type and a time interval
        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        // Invoke the History API with the Google API client object and delete request, and then
        // specify a callback that will check the result.
        Fitness.HistoryApi.deleteData(mClient, request)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Successfully deleted today's step count data.");
                        } else {
                            // The deletion will fail if the requesting app tries to delete data
                            // that it did not insert.
                            Log.i(TAG, "Failed to delete today's step count data.");
                        }
                    }
                });
        // [END delete_dataset]
    }

    //endregion

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
                fragmentClass = Daily.class;
                break;
            case R.id.nav_activities_fragment:
                fragmentClass = Daily.class;
                break;
            case R.id.nav_medications_fragment:
                fragmentClass = Daily.class;
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
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
    public void launchBackgroundService()
    {
        //Construct the intent service
        Intent i = new Intent(this, MyIntentService.class);
        //Add extras? TODO
        i.putExtra("foo", "bar");
        //Start the service
        startService(i);
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

}
