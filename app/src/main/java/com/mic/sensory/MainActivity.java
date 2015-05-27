package com.mic.sensory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity implements SensorEventListener, MicrophoneInputListener {

    //private final static String NOT_SUPPORTED_MESSAGE = "Sorry, sensor not available for this device.";
    //private final static String TAG = "SensorsList";
    Sensor accelerometer;
    SensorManager sm;
    TextView acceleration;

    Sensor thermometer;
    TextView temperature;

    Sensor proxSensor;
    TextView proxText;

    Sensor mPressure;
    TextView pressure;

    Sensor mLight;
    TextView mLightTxt;


    MicrophoneInput micInput;  // The micInput object provides real time audio.
    TextView mdBTextView;
    TextView mdBFractionTextView;
    TextView test;

    private TextView mGainTextView;


    // The Google ASR input requirements state that audio input sensitivity
    // should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
    // 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
    // For displaying error in calibration.
    double mDifferenceFromNominal = 0.0;
    double mRmsSmoothed;  // Temporally filtered version of RMS.
    double mAlpha = 0.9;  // Coefficient of IIR smoothing filter for RMS.
    private int mSampleRate;  // The audio sampling rate to use.
    private int mAudioSource;  // The audio source to use.

    // Variables to monitor UI update and check for slow updates.
    private volatile boolean mDrawing;

    private static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        micInput = new MicrophoneInput((MicrophoneInputListener) this);

        mdBTextView = (TextView) findViewById(R.id.dBTextView);
        mdBFractionTextView = (TextView) findViewById(R.id.dBFractionTextView);
        mGainTextView = (TextView) findViewById(R.id.gain);

        // Toggle Button handler.

        final ToggleButton onOffButton=(ToggleButton)findViewById(
                R.id.on_off_toggle_button);

        ToggleButton.OnClickListener tbListener =
                new ToggleButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onOffButton.isChecked()) {
                            readPreferences();
                            micInput.setSampleRate(mSampleRate);
                            micInput.setAudioSource(mAudioSource);
                            micInput.start();
                        } else {
                            micInput.stop();
                        }
                    }
                };
        onOffButton.setOnClickListener(tbListener);

        // Level adjustment buttons.


// Minus 5 dB button event handler.
        Button minus5dbButton = (Button)findViewById(R.id.minus_5_db_button);
        DbClickListener minus5dBButtonListener = new DbClickListener(-5.0);
        minus5dbButton.setOnClickListener(minus5dBButtonListener);

        // Minus 1 dB button event handler.
        Button minus1dbButton = (Button)findViewById(R.id.minus_1_db_button);
        DbClickListener minus1dBButtonListener = new DbClickListener(-1.0);
        minus1dbButton.setOnClickListener(minus1dBButtonListener);

        // Plus 1 dB button event handler.
        Button plus1dbButton = (Button)findViewById(R.id.plus_1_db_button);
        DbClickListener plus1dBButtonListener = new DbClickListener(1.0);
        plus1dbButton.setOnClickListener(plus1dBButtonListener);

        // Plus 5 dB button event handler.
        Button plus5dbButton = (Button)findViewById(R.id.plus_5_db_button);
        DbClickListener plus5dBButtonListener = new DbClickListener(5.0);
        plus5dbButton.setOnClickListener(plus5dBButtonListener);

        Button settingsButton=(Button)findViewById(R.id.settingsButton);
        Button.OnClickListener settingsBtnListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final ToggleButton onOffButton=(ToggleButton)findViewById(
                                R.id.on_off_toggle_button);
                        onOffButton.setChecked(false);
                        MainActivity.this.micInput.stop();

                        Intent settingsIntent = new Intent(MainActivity.this,
                                Settings.class);
                        MainActivity.this.startActivity(settingsIntent);
                    }
                };
        settingsButton.setOnClickListener(settingsBtnListener);



        sm=(SensorManager)getSystemService(SENSOR_SERVICE);


// accelerometer
        accelerometer=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration=(TextView)findViewById(R.id.acceleration);



//temperature
        thermometer=sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        temperature=(TextView)findViewById(R.id.temperature);

//PROXIMITY
        proxSensor=sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxText=(TextView)findViewById(R.id.proximityTextView);

//pressure
        mPressure=sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        pressure=(TextView)findViewById(R.id.pressure);

//Light
//       mLight=sm.getDefaultSensor(Sensor.TYPE_LIGHT);
//        mLightTxt=(TextView)findViewById(R.id.mLightTxt);







    }








    protected void onResume() {

        super.onResume();
        sm.registerListener((SensorEventListener) this, (Sensor) accelerometer, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener((SensorEventListener)this,(Sensor) thermometer, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener((SensorEventListener)this,(Sensor) proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        //      sm.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);



    }

    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

 //   float accel;
    @Override
    public void onSensorChanged(SensorEvent event) {
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
            float millibars_of_pressure = event.values[0];
            pressure.setText(String.valueOf(millibars_of_pressure));
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            mLightTxt.setText(String.valueOf(lux));


        }
    }

    @Override
    public void processAudioFrame(short[] audioFrame) {
        if (!mDrawing) {
            mDrawing = true;
            // Compute the RMS value. (Note that this does not remove DC).
            double rms = 0;
            for (int i = 0; i < audioFrame.length; i++) {
                rms += audioFrame[i]*audioFrame[i];
            }
            rms = Math.sqrt(rms/audioFrame.length);

            // Compute a smoothed version for less flickering of the display.
            mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
            final double rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed);

            // Set up a method that runs on the UI thread to update of the LED bar
            // and numerical display.
            mdBTextView.post(new Runnable() {
                @Override
                public void run() {
                    // The bar has an input range of [0.0 ; 1.0] and 10 segments.
                    // Each LED corresponds to 6 dB.
                    //         test.setLevel((mOffsetdB + rmsdB) / 60);

                    DecimalFormat df = new DecimalFormat("##");
                    mdBTextView.setText(df.format(20 + rmsdB));

                    //         DecimalFormat df_fraction = new DecimalFormat("#");
                    int one_decimal = (int) (Math.round(Math.abs(rmsdB * 10))) % 10;
                    mdBFractionTextView.setText(Integer.toString(one_decimal));
                    mDrawing = false;
                }
            });
        }


    }

    public class DbClickListener implements Button.OnClickListener {
        private double gainIncrement;

        public DbClickListener(double gainIncrement) {
            this.gainIncrement = gainIncrement;
        }

        @Override
        public void onClick(View v) {
            MainActivity.this.mGain *= Math.pow(10, gainIncrement / 20.0);
            mDifferenceFromNominal -= gainIncrement;
            DecimalFormat df = new DecimalFormat("##.# dB");
            mGainTextView.setText(df.format(mDifferenceFromNominal));
        }
    }

}


//    Log.i(TAG, "Sensor registered");
//   sensorListenersList.add(sel);
//    Log.d(TAG, "Sensor type is: " + s.getType());

