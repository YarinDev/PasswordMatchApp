package com.example.passwordmatchapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final static String EMPTY = "";
    private final static int ZERO = 0;
    private final static String SOUTH = "S";

    private TextInputLayout password;
    private MaterialButton main_BTN_logIn;
    private RelativeLayout main_RAY_layout;
    private String lastCallNumber;
    private String positionMagneticField;
    private int BatteryLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initValues();
        initClickListeners();
        BatteryLevel = getBatteryLevel();

        handleReadCallLogPermission();
        // activate the current magnetic field
        new SensorMagneticField(getApplicationContext(), onSensorMagneticFieldCallBack);


    }

    private void initClickListeners() {
        main_BTN_logIn.setOnClickListener(logInClickListener);

    }

    private void initValues() {
        lastCallNumber = EMPTY;
        positionMagneticField = EMPTY;
        BatteryLevel = ZERO;


    }


    private final View.OnClickListener logInClickListener = v -> handleLogIn();

    private void handleLogIn() {
        if (getIfUserCanLogIn()) {
            disableErrorMsg();
            Snackbar.make(main_RAY_layout, "Password is Correct!!", Snackbar.LENGTH_LONG).show();
        } else {
            enabledErrorMsg("Wrong Password!");
        }
    }


    private boolean getIfUserCanLogIn() {
        String passwordNeedType = lastCallNumber + BatteryLevel;
        String passwordUserType = getPasswordFromUser();
        boolean canLogIn = false;

        // if user directed to South
        if (positionMagneticField.equals(SOUTH)) {
            // and if the passwords is equal (doesn't matter order password or also lowercase)
            if (passwordNeedType.length() == passwordUserType.length()) {
                if (passwordUserType.toUpperCase().contains(passwordNeedType.toUpperCase())) {
                    // user can log in to the system
                    canLogIn = true;
                }

            }
        }
        return canLogIn;
    }

    private String getPasswordFromUser() {
        return Objects.requireNonNull(password.getEditText()).getText().toString().trim();
    }

    // get the direction magnetic field
    private final OnSensorMagneticFieldCallBack onSensorMagneticFieldCallBack = new OnSensorMagneticFieldCallBack() {
        @Override
        public void onSensorMagneticFieldCallBack(String position) {
            positionMagneticField = position;
        }
    };

    private void openPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);


    }

    private void handleReadCallLogPermission() {
        int checkIfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);

        if (checkIfPermission == PackageManager.PERMISSION_GRANTED) {
            getLastPhoneNumberCall();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALL_LOG);
        }
    }

    private void getLastPhoneNumberCall() {
        //fields to select.
        String[] strFields = {
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.CACHED_NAME,
                android.provider.CallLog.Calls.CACHED_NUMBER_TYPE
        };
        //only incoming.
        String strSelection = android.provider.CallLog.Calls.TYPE + " = " + android.provider.CallLog.Calls.INCOMING_TYPE;

        //most recent first
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";

        //get a cursor.
        Cursor mCallCursor = getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI, //content provider URI
                strFields, //project (fields to get)
                strSelection, //selection
                null, //selection args
                strOrder //sortorder.
        );

        mCallCursor.moveToFirst();

        if (mCallCursor.getCount() > 0) {
            lastCallNumber = mCallCursor.getString(0);
        }
        mCallCursor.close();


    }


    private void findViews() {
        password = findViewById(R.id.password);
        main_BTN_logIn = findViewById(R.id.main_BTN_logIn);
        main_RAY_layout = findViewById(R.id.main_RAY_layout);
    }

    private int getBatteryLevel() {
        BatteryManager bm = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getLastPhoneNumberCall();
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
                    Snackbar.make(main_RAY_layout, "Approval Permission", Snackbar.LENGTH_LONG)
                            .setAction("Settings", v -> openPermissionSettings()).show();

                }
            });


    private void enabledErrorMsg(String msg) {
        password.setError(msg);
        password.setErrorEnabled(true);


    }

    private void disableErrorMsg() {
        password.setErrorEnabled(false);
    }

}

