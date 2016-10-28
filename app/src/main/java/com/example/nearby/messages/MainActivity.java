package com.example.nearby.messages;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.internal.PublishRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, View.OnClickListener {

    GoogleApiClient mGoogleApiClient;
    Message mActiveMessage;
    private static final String TAG = "Messages";
    MessageListener mMessageListener;
    Button mConnect, mUnsubscribe, mPublish;
    EditText etPublishMessage;
    ListView lvPublishMessages, lvSubscribedMessages;
    static Boolean isConnected = false;
    ArrayList<String> publishedMessages, subscribedMessages;
    ArrayAdapter<String> publishedMessagesAdapter, subscribedMessagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnect = (Button) findViewById(R.id.connect);
        mUnsubscribe = (Button) findViewById(R.id.unsubscribe);
        mPublish = (Button) findViewById(R.id.publish);

        etPublishMessage = (EditText) findViewById(R.id.etPublishedMessages);

        lvPublishMessages = (ListView) findViewById(R.id.lvPublishedMessages);
        lvSubscribedMessages =(ListView) findViewById(R.id.lvSubscribedMessages);


        mConnect.setOnClickListener(this);
        mUnsubscribe.setOnClickListener(this);
        mPublish.setOnClickListener(this);


        publishedMessages = new ArrayList<>();
        subscribedMessages = new ArrayList<>();

        publishedMessagesAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, publishedMessages);
        subscribedMessagesAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, subscribedMessages);

        lvPublishMessages.setAdapter(publishedMessagesAdapter);
        lvSubscribedMessages.setAdapter(subscribedMessagesAdapter);

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String messageAsString = new String(message.getContent());
                Log.d(TAG, "Found message as :" + messageAsString);
                subscribedMessages.add(messageAsString);
                subscribedMessagesAdapter.notifyDataSetChanged();

            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                String messageAsString = new String(message.getContent());
                Log.d(TAG, "Lost sight of message :" + messageAsString);

            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mUnsubscribe.setEnabled(true);
        mPublish.setEnabled(true);
        subscribe();
    }


    @Override
    public void onConnectionSuspended(int i) {
        mUnsubscribe.setEnabled(false);
        mPublish.setEnabled(false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mUnsubscribe.setEnabled(false);
        mPublish.setEnabled(false);
    }


    private void publish(final String message){
        Log.i(TAG,"Publishing message :" + message);
        mActiveMessage = new Message(message.getBytes());
        Nearby.Messages
                .publish(mGoogleApiClient,mActiveMessage,
                        new PublishOptions.
                                Builder()
                                .setCallback(new PublishCallback() {
                                    @Override
                                    public void onExpired() {
                                        super.onExpired();

                                    }
                                }).build())
                .setResultCallback(this);
    }

    private void unpublish(){
        Log.i(TAG, "Unpublishing");
        if(mActiveMessage != null){
            Nearby.Messages.unpublish(mGoogleApiClient,mActiveMessage);
            mActiveMessage = null;
        }
    }

    private void subscribe(){
        Log.i(TAG, "Subscribing");
        Nearby.Messages.subscribe(mGoogleApiClient,mMessageListener);
    }

    private void unsubsribe(){

            Log.i(TAG, "Unsubscribing");
            Nearby.Messages.unsubscribe(mGoogleApiClient,mMessageListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            unpublish();
            unsubsribe();
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void onStart() {
        super.onStart();

       /* // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       *//* mGoogleApiClient.connect();*//*
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.nearby.messages/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);*/
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch(id){
            case R.id.connect:
                // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
                    unsubsribe();
                    unpublish();
                    mGoogleApiClient.disconnect();
                    mPublish.setEnabled(false);
                    mUnsubscribe.setEnabled(false);
                }else if(mGoogleApiClient != null && !(mGoogleApiClient.isConnected())){
                    mGoogleApiClient.connect();
                }else if(mGoogleApiClient == null){
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Nearby.MESSAGES_API).addConnectionCallbacks(this)
                            .enableAutoManage(this, this)
                            .addApi(AppIndex.API).build();
                    mGoogleApiClient.connect();
                }
                break;
            case R.id.unsubscribe:
                unsubsribe();
                break;
            case R.id.publish:
                String message = String.valueOf(etPublishMessage.getText());
                publishedMessages.add(message);
                publishedMessagesAdapter.notifyDataSetChanged();
                publish(message);
                etPublishMessage.setText("");
                break;
        }
    }
}
