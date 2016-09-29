package com.cloud4form.app.gms;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.cloud4form.app.R;
import com.cloud4form.app.AppController;
import com.cloud4form.app.db.ChatItem;
import com.cloud4form.app.db.User;
import com.cloud4form.app.pages.ChatProfileActivity;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    AppController appController;

    @Override
    public void onCreate() {
        super.onCreate();
        appController = AppController.getInstance(this);
    }

    private ArrayList<ChatItem> loadChatItem(){
        return appController.Filo.readArray(ChatItem.class);
    }

    private void saveChatItem(ArrayList arr){
        appController.Filo.saveArray(arr,ChatItem.class);
    }


    private String getFrom(String infoFrom){
        ArrayList<User> uList= appController.Filo.readArray(User.class);
        for(User u:uList){
            if(u.getServerId().equals(infoFrom)){
                return u.getFirstName()+" "+u.getLastName();
            }
        }
        return "C4F Message";
    }

    private boolean ProcessMessage(Bundle bundle){
        String event=bundle.getString("event");
        String action=bundle.getString("action");
        if(event.equals("USER_EVENT") && action.equals("USER_MESSAGE")) {
            String from=bundle.getString("from1");
            String message=bundle.getString("message1");

            ArrayList<ChatItem> list=loadChatItem();

            ChatItem ci=new ChatItem(from,message);
            ci.setTo("SELF");
            ci.setRead(false);
            ci.setType(ChatItem.item_type.DOWN);

            list.add(ci);
            saveChatItem(list);
            return true;
        }
        return false;
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (from.startsWith("/topics/"+ appController.getPref(AppController.PREE_APP_TENANT)+"/feeds")) {
            // message received from some topic.
        } else {

            data.putString("from",from);

            if(ProcessMessage(data)){

                Intent localIntent =new Intent("BROADCAST_ACTION_MSG")
                        .putExtra("data",data);


                if(AppController.isAppRunning){//.doNotify(data)){//forward to activity as it is open
                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
//                    try {
//                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                        r.play();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }



                }else{//Show notification
                    String message = data.getString("message1");
                    String infoFrom = data.getString("from1");
                    sendNotification(message,getFrom(infoFrom));
                }

            }else{
                //Other service
            }

        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
//        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message,String title) {

        Intent intent = new Intent(this, ChatProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_crop)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }


}
