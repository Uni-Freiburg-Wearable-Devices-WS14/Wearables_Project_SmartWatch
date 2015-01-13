package com.wearables.praktikum.wearablenfc;

import android.app.Activity;
import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.RemoteInput;
import android.graphics.*;
import android.util.Log;
import android.widget.TextView;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.*;


public class MainActivity extends Activity {

    private static String TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 2;
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private NfcAdapter mNFCadapter;
    // New Comment to test Git in Android Studio
    private TextView mMainTextView, mIdText, mReplyText;
    private PendingIntent mPending;
    private RemoteInput mRemoteInput;

    private Bitmap BgWear;

    private Intent mReplyIntent;
    private PendingIntent mPendingReplyIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        mMainTextView = (TextView) findViewById(R.id.main_text_view);
        mIdText = (TextView) findViewById(R.id.nfc_read_tag);
        mReplyText = (TextView) findViewById(R.id.txt_reply);

        mRemoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(getString(R.string.edit_text))
                .build();

        mReplyIntent = new Intent(this, this.getClass());
        mPendingReplyIntent = PendingIntent.getActivity(this, 0, mReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNFCadapter = NfcAdapter.getDefaultAdapter(this);

        mPending = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        resolveIntent(getIntent());
    }

    private void resolveIntent(Intent pIntent) {
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())){
            Log.i(TAG, "NFC_Intent");
            byte[] id = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID); // Obtain NFC Tag iD Array
            if (id != null) {
                long temp = getDec(id);
                String idTextHandheld = Long.toString(temp); // Why so many conversions?
                String hexTextNotification = Long.toHexString(temp);
                Log.i(TAG, Long.toString(temp));
                mMainTextView.setText("iD of Tag is:");
                mIdText.setText(idTextHandheld);
                makeNotifyWear(hexTextNotification);
            }
        }
        else if (pIntent.getClipData() != null)
        {
            mReplyText.setText(getReplyText(pIntent).toString());
        }
    }

    private long getDec(byte[] pBytes) {
        // Method to convert hexadecimal Tag number to decimal
        long result = 0;
        long factor = 1;
        for (int i = 0; i < pBytes.length; ++i) {
            long value = pBytes[i] & 0xffl; // ?
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private void makeNotifyWear(String pTemp) {

        // Second Action (Reply) Creation
        NotificationCompat.Action wAction = new NotificationCompat.Action.Builder(R.drawable.ic_full_reply,
                getString(R.string.edit_text), mPendingReplyIntent)
                .addRemoteInput(mRemoteInput)
                .build();

        // Style Creation for second page
        NotificationCompat.BigTextStyle secPage =  new NotificationCompat.BigTextStyle();
        secPage.setBigContentTitle("Page 2").bigText("Some big text ...");

        // Create a second page of notification
        Notification secPageNot = new NotificationCompat.Builder(this)
                .setStyle(secPage)
                .build();

        // Creation of Main Notification
        NotificationCompat.Builder wNotificationBuilder =
                new NotificationCompat.Builder(this) // getApplicationContext
                .setSmallIcon(R.drawable.ic_noti_nfc) // Find an ic_event icon
                .setContentTitle(getString(R.string.eventTitle))
                .setContentText(pTemp)
                .setContentIntent(mPending);
                //.addAction(R.drawable.ic_map, getString(R.string.map), pEditIntent); // Main content action defined by handheld, in this case the activity itself

        BgWear = BitmapFactory.decodeResource(getResources(), R.drawable.bg_wearable_nfc);

        NotificationCompat.WearableExtender wExtender =
                new WearableExtender()
                .setBackground(BgWear)
                .addPage(secPageNot)
                .addAction(wAction);

        wNotificationBuilder.extend(wExtender);

        NotificationManagerCompat notificationManagerWear = NotificationManagerCompat.from(this);

        notificationManagerWear.notify(NOTIFICATION_ID, wNotificationBuilder.build());
    }

    private CharSequence getReplyText (Intent pReplyIntent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(pReplyIntent);
        Log.i(TAG,"Char Sequenced!!!");
        if(remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    protected void onResume() {
        super.onResume();

        if(mNFCadapter != null)
            mMainTextView.setText(getString(R.string.tag_text));
        else
            mMainTextView.setText(getString(R.string.nfc_not_enabled));

        if(!mNFCadapter.isEnabled()){
            mMainTextView.setText(getString(R.string.nfc_disabled));
        }

        mNFCadapter.enableForegroundDispatch(this, mPending, null, null);
        Log.i(TAG, "onResume()!");
    }

    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        if(mNFCadapter != null) {
            mNFCadapter.disableForegroundDispatch(this);
        }
    }

    protected void onNewIntent(Intent pIntent){
        super.onNewIntent(pIntent);
        setIntent(pIntent);
        Log.i(TAG,"new Intent!");
        resolveIntent(pIntent);
    }

    //Options menu, till now not needed!
    /*@Override
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
    }*/
}
