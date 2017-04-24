package jpstarkey.symptracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class myAlarmReceiver extends BroadcastReceiver
{
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "jpstarkey.symptracker";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date resultdate = new Date(yourmilliseconds);

        createNotification(context, 0, R.drawable.ic_accessibility, "It's time to input your daily pain level! - ", sdf.format(resultdate));
    }

    private void createNotification(Context context, int nId, int iconRes, String title, String body)
    {
        Intent intent = new Intent(context, Daily.class);
        intent.putExtra("menuFragment", "nav_daily_fragment");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();

        int flags = PendingIntent.FLAG_CANCEL_CURRENT;

//        PendingIntent pIntent = PendingIntent.getActivity(context, requestID, intent, flags);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification noti = new NotificationCompat.Builder(context)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pIntent)
                .build();

        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(nId, noti);
    }

}
