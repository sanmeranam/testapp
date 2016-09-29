package com.cloud4form.app.other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {
    public BootUpReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, FormUploadService.class);
        context.startService(myIntent);
    }
}
