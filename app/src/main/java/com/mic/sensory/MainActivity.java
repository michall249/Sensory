package com.mic.sensory;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private final static String NOT_SUPPORTED_MESSAGE = "Sorry, sensor not available for this device.";

    Sensor accelerometer;
    SensorManager sm;
    TextView acceleration;

    Sensor thermometer;
    TextView temperature;

    Sensor proxSensor;
    TextView proxText;

    Sensor mPressure;
    TextView pressure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm=(SensorManager)getSystemService(SENSOR_SERVICE);


// accelerometer
        accelerometer=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration=(TextView)findViewById(R.id.acceleration);



//temperature
       //      sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        thermometer=sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
       //      sm.registerListener(this, thermometer, SensorManager.SENSOR_DELAY_FASTEST);
        temperature=(TextView)findViewById(R.id.temperature);
        //  temperature.setText(""+temperature.getPower());

//PROXIMITY
        //     sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor=sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxText=(TextView)findViewById(R.id.proximityTextView);

//pressure
        mPressure=sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        pressure=(TextView)findViewById(R.id.pressure);
    }

    protected void onResume() {

        super.onResume();
        sm.registerListener((SensorEventListener) this, (Sensor) accelerometer, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener((SensorEventListener)this,(Sensor) thermometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener((SensorEventListener)this,(Sensor) proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        sm.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //     float ambient_temperature = event.values[0];
        //      temperaturelabel.setText("Ambient Temperature:\n " + ambient_temperature);


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleration.setText("X: " + event.values[0] +
                    "\nY: " + event.values[1] +
                    "\nZ: " + event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            temperature.setText("st C: " + event.values[0]);


            // int sensor = event.type;
            //   float[] values = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proxText.setText(String.valueOf(event.values[0]));

        }
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure.setText(String.valueOf(event.values[0]));
        }
    }
}




