package com.example.maychatapplication.Fcm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.maychatapplication.R;
import com.example.maychatapplication.Utilities.Constants;
import com.example.maychatapplication.Utilities.MyApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String status = remoteMessage.getData().get(Constants.REMOTE_MSG_STATUS);

        if (status != null) {
            if (status.equals("false")){
                String msg =  remoteMessage.getData().get(Constants.KEY_MESSAGE);
                String name =  remoteMessage.getData().get(Constants.KEY_NAME);
                String avatar = remoteMessage.getData().get(Constants.KEY_AVATAR);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_list_message)
                        .setColor(Color.BLUE)
                        .setContentTitle(name)
                        .setContentText(msg)
                        .setLargeIcon(getBitmapFromURL(avatar))
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
                notificationManager.notify(1, builder.build());
                super.onMessageReceived(remoteMessage);
            }else {
                Log.d("API", "ERROR");
            }
        }

    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

}
