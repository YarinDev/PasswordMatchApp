package com.example.passwordmatchapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorMagneticField implements SensorEventListener {


    private final OnSensorMagneticFieldCallBack onSensorMagneticFieldCallBack;
    private int mAzimuth;
    private final SensorManager mSensorManager;
    private float[] rMat;
    private float[] orientation;
    private float[] mLastAccelerometer;
    private float[] mLastMagnetometer;
    private boolean mLastAccelerometerSet;
    private boolean mLastMagnetometerSet;


    public SensorMagneticField(Context context, OnSensorMagneticFieldCallBack onSensorMagneticFieldCallBack) {
        initValues();
        this.onSensorMagneticFieldCallBack = onSensorMagneticFieldCallBack;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        start();


    }

    private void initValues() {
        rMat = new float[9];
        orientation = new float[3];
        mLastAccelerometer = new float[3];
        mLastMagnetometer = new float[3];
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
    }

    //activate the Sensor MagneticField
    private void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (!(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || !(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            Sensor mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        String directionAzimuth = checkDirection();
        onSensorMagneticFieldCallBack.onSensorMagneticFieldCallBack(directionAzimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String checkDirection() {
        String direction = "NW";

        if (mAzimuth >= 350 || mAzimuth <= 10)
            direction = "N";
        if (mAzimuth < 350 && mAzimuth > 280)
            direction = "NW";
        if (mAzimuth <= 280 && mAzimuth > 260)
            direction = "W";
        if (mAzimuth <= 260 && mAzimuth > 190)
            direction = "SW";
        if (mAzimuth <= 190 && mAzimuth > 170)
            direction = "S";
        if (mAzimuth <= 170 && mAzimuth > 100)
            direction = "SE";
        if (mAzimuth <= 100 && mAzimuth > 80)
            direction = "E";
        if (mAzimuth <= 80 && mAzimuth > 10)
            direction = "NE";
        return direction;
    }


}
