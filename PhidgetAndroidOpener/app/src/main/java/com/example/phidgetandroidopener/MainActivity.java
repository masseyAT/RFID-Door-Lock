package com.example.phidgetandroidopener;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class MainActivity extends AppCompatActivity {


    public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";


    String userid = "16061659";
    //We have to generate a unique Client id.
    String clientId = userid + "-sub";

    // Default sensor to listen for -
    // Change to another if you are broadcasting a different sensor name

    // Hard coded motor id to test when marking
    String motorID = "306613";
    public final String TOPIC_MOTOR = userid +"/motor" + motorID;


    private MqttClient mqttClient;

    Button openDoor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final String email_sent = "Door open";
        // Message to display to user to when door unlocked - wanted to implement push notifications as this would
        // only display when app is open.
        final Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myLayout), "Door open", 1800);
        final Snackbar mySnackbarClosed = Snackbar.make(findViewById(R.id.myLayout), "Door closed", 1800);
        openDoor = (Button) findViewById(R.id.openLockButton);
        openDoor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

// Code here executes on main thread after user presses button
                System.out.println("PUBLISHING");
                runOnUiThread(new Runnable() {
                    public void run() {
                        publishMQTTMotor();
                    }
                });
                mySnackbar.show();
            }
        });


        // start new
        // Create MQTT client and start subscribing to message queue
        try {
            // change from original. Messages in "null" are not stored
            mqttClient = new MqttClient(BROKER_URL, clientId,null);
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    //This is called when the connection is lost. We could reconnect here.
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("DEBUG: Message arrived. Topic: " + topic + "  Message: " + message.toString());
                    // get message data
                    final String messageStr = message.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            System.out.println("Updating UI");

                            mySnackbarClosed.show();
                            // Update UI elements
                         //   TextView sensorValueTV = (TextView) findViewById(R.id.sensorValueTV);
                          //  sensorValueTV.setText(messageStr);
                        }
                    });

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //no-op
                }

                @Override
                public void connectComplete(boolean b , String s) {
                    //no-op
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }

        startSubscribing();
        // temp use of ThreadPolicy until use AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }


    private void startSubscribing() {
        try {
            mqttClient.connect();

            //Subscribe to all subtopics of home

            mqttClient.subscribe(TOPIC_MOTOR);

            System.out.println("Subscriber is now listening to "+TOPIC_MOTOR);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void publishMQTTMotor() {
        try {
            //mqttClient.connect();
            final MqttTopic motorTopic = mqttClient.getTopic(TOPIC_MOTOR);

            final String messageToOpenDoor = "150";

            motorTopic.publish(new MqttMessage(messageToOpenDoor.getBytes()));

            System.out.println("Published data. Topic: " + motorTopic.getName() + "  Message: Door Opened" );

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}





