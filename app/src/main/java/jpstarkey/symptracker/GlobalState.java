package jpstarkey.symptracker;

/**
 * Created by Joshs on 07/04/2017.
 */
import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;

public class GlobalState extends Application
{
    public static final String TAG = "BasicHistoryApi";
    private static final String AUTH_PENDING = "auth_state_pending";
    private static boolean authInProgress = false;
    private GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;

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


}
