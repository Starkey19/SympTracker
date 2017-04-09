package jpstarkey.symptracker;

/**
 * Created by Joshs on 07/04/2017.
 */
import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class GlobalState extends Application
{
    public static final String TAG = "BasicHistoryApi";
    private static final String AUTH_PENDING = "auth_state_pending";
    private static boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;
    private float totalSteps = 0.0f;

    //Constructor
    public GlobalState()
    {

    }

    public void setMClient(GoogleApiClient mClient)
    {
        this.mClient = mClient;
    }

    public GoogleApiClient getClient()
    {
        return mClient;
    }

    //Just gets the daily total steps for current day
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
                                totalSteps = ((totalSet == null) || totalSet.isEmpty())
                                        ? 0
                                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                            }
                        }
                    });
             return totalSteps;
        } else if (!mClient.isConnecting())
        {
            mClient.connect();
        }

        return getDailySteps();
    }

}
