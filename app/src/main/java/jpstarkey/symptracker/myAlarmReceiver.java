package jpstarkey.symptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class myAlarmReceiver extends BroadcastReceiver
{
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "jpstarkey.symptracker";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("foo", "bar");
        context.startService(i);
    }
}
