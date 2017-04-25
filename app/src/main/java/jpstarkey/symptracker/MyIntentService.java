package jpstarkey.symptracker;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService
{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "jpstarkey.symptracker.action.FOO";
    private static final String ACTION_BAZ = "jpstarkey.symptracker.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "jpstarkey.symptracker.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "jpstarkey.symptracker.extra.PARAM2";

    public MyIntentService()
    {
        super("MyIntentService");
    }


    public static void startActionFoo(Context context, String param1, String param2)
    {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2)
    {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i("MyIntentService", "Service running");

        if (intent != null)
        {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action))
            {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action))
            {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }


    private void handleActionFoo(String param1, String param2)
    {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void handleActionBaz(String param1, String param2)
    {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

