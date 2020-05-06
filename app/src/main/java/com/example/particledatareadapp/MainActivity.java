package com.example.particledatareadapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Locale;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

import static com.example.particledatareadapp.App.CHANNEL_1_ID;
import static com.example.particledatareadapp.App.CHANNEL_2_ID;

public class MainActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;

    static String status = "Opened";
    String TAG = "Event:";
    Uri soundtest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ParticleCloudSDK.init(this);
        TextView payload;
        payload = findViewById(R.id.firstTextView);

        notificationManager = NotificationManagerCompat.from(this);


        soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.testsound);

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, String>() {
            @Override
            public String callApi(ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                try {
                    // Login
                    ParticleCloudSDK.getCloud().logIn("madhuram@andrew.cmu.edu", "cmu553010b");
                    ParticleDevice myDevice = ParticleCloudSDK.getCloud().getDevice("e00fce68f1a4efc2d2735fdc");

                    // Subscribe to an event
                    long subscriptionId = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(
                            "window_todo",
                            new ParticleEventHandler() {
                                public void onEvent(String eventName, final ParticleEvent event) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            status = event.dataPayload;
                                            Log.d(TAG, event.dataPayload);
                                            if (event.dataPayload.equals("Opened: close the window")) {
                                                //Log.d(TAG, event.dataPayload);
                                                soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.window);
                                                notifyWindow("It is going to rain. Close the window.");
                                            }
                                            else if (event.dataPayload.equals("Opened: winter")) {
                                                soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.window_winter);
                                                notifyWindow("It is freezing outside. Close the window.");
                                            }
                                            else {
                                                notificationManager.cancel(1);
                                            }
                                        }
                                    });
                                }

                                public void onEventError(Exception e) {
                                    Log.e(TAG, "Event error: ", e);
                                }
                            });

                    // Subscribe to clothing status
                    long subscriptionId1 = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(
                            "clothing",
                            new ParticleEventHandler() {
                                public void onEvent(String eventName, final ParticleEvent event) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, event.dataPayload);
                                            switch (event.dataPayload)
                                            {
                                                case "1":
                                                    soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cold);
                                                    notifyClothing("It's freezing outside. Wear thick jacket and boots!");
                                                    break;
                                                case "2":
                                                    soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chilly);
                                                    notifyClothing("It's a bit chilly. Take your jacket.");
                                                    break;
                                                case "3":
                                                    soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.windy);
                                                    notifyClothing("It's very windy outside. Take your jacket.");
                                                    break;
                                                case "4":
                                                    soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sunny);
                                                    notifyClothing("Great weather outside. Perfect to wear light clothes.");
                                                    break;
                                                case "6":
                                                    notificationManager.cancel(1);
                                                    break;
                                                default:
                                                    break;

                                            }
                                        }
                                    });
                                }

                                public void onEventError(Exception e) {
                                    Log.e(TAG, "Event error: ", e);
                                }
                            });

                    // Subscribe to umbrella status
                    long subscriptionId2 = ParticleCloudSDK.getCloud().subscribeToMyDevicesEvents(
                            "umbrella found",
                            new ParticleEventHandler() {
                                public void onEvent(String eventName, final ParticleEvent event) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, event.dataPayload);
                                            if (event.dataPayload.equals("0")) {
                                                soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rain_absent);
                                                notifyClothing("It's going to rain today. Umbrella is not in the closet. Wear a raincoat and don't forget your umbrella.");
                                            }
                                            else if (event.dataPayload.equals("1")) {
                                                soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rain_present);
                                                notifyClothing("It's going to rain today. Umbrella is hanging in the closet. Wear a raincoat and take your umbrella.");
                                            }
                                            else if (event.dataPayload.equals("2")) {
                                                    notificationManager.cancel(1);
                                            }
                                        }
                                    });
                                }

                                public void onEventError(Exception e) {
                                    Log.e(TAG, "Event error: ", e);
                                }
                            });



                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(String value) {
                Log.d(TAG, "Success: " + value);
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Log.e(TAG, "Something went wrong making an SDK call: ", e);
            }

        });
    }

    public void refresh (View v) {
        TextView payload;
        payload = findViewById(R.id.firstTextView);
        payload.setText(status);

        //soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.world);
    }

    public void notifyWindow(String window_suggestion) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        //broadcastIntent.putExtra("toastMessage", message);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.window);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                //.setContentTitle(title)
                //.setContentText(message)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(window_suggestion)
                        .setBigContentTitle("Window alert!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();


        //soundtest = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.window);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundtest);
        r.play();
        notificationManager.notify(1, notification);
    }

    public void notifyClothing(String suggestion) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        //broadcastIntent.putExtra("toastMessage", message);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.window);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                //.setContentTitle(title)
                //.setContentText(message)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(suggestion)
                        .setBigContentTitle("Clothing suggestions"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();



        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundtest);
        r.play();
        notificationManager.notify(1, notification);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
