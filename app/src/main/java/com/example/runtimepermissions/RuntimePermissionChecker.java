package com.example.runtimepermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class RuntimePermissionChecker extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_APPS_SETTINGS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
    };
    private Context context;
    private Activity activity;

    private enum RuntimePermissionsResult {
        ALL_PERMISSION_ACCEPTED,
        ALL_PERMISSION_DENIED,
        SOME_PERMISSION_ACCEPTED_AND_DENIED
    }

    private enum RuntimePermissionDenied {
        ALL_PERMISSION_DENIED_WITH_DO_NOT_ASK_AGAIN,
        ALL_PERMISSION_DENIED_WITHOUT_DO_NOT_ASK_AGAIN,
        SOME_PERMISSION_DENIED_WITH_AND_WITHOUT_DO_NOT_ASK_AGAIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionValidator();
        } else {
            resultOk();
        }
    }

    private void resultOk() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void resultCancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void permissionValidator() {
        switch (hasPermissions(context, REQUIRED_PERMISSIONS)) {
            case ALL_PERMISSION_ACCEPTED:
                resultOk();
                break;
            case ALL_PERMISSION_DENIED:
            case SOME_PERMISSION_ACCEPTED_AND_DENIED:
                ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            switch (hasPermissions(context, REQUIRED_PERMISSIONS)) {
                case ALL_PERMISSION_ACCEPTED:
                    resultOk();
                    break;
                case SOME_PERMISSION_ACCEPTED_AND_DENIED:
                    switch (hasPermissionsDenied(context, permissions)) {
                        case ALL_PERMISSION_DENIED_WITH_DO_NOT_ASK_AGAIN:
                            settingsDialog();
                            break;
                        case SOME_PERMISSION_DENIED_WITH_AND_WITHOUT_DO_NOT_ASK_AGAIN:
                        case ALL_PERMISSION_DENIED_WITHOUT_DO_NOT_ASK_AGAIN:
                            mandatoryPermissionDialog();
                            break;
                    }
                    break;
                case ALL_PERMISSION_DENIED:
                    switch (hasPermissionsDenied(context, permissions)) {
                        case ALL_PERMISSION_DENIED_WITH_DO_NOT_ASK_AGAIN:
                        case SOME_PERMISSION_DENIED_WITH_AND_WITHOUT_DO_NOT_ASK_AGAIN:
                            settingsDialog();
                            break;
                        case ALL_PERMISSION_DENIED_WITHOUT_DO_NOT_ASK_AGAIN:
                            mandatoryPermissionDialog();
                            break;
                    }
                    break;
            }
        }
    }

    private void mandatoryPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.mandatory_permission_title))
                .setMessage(getString(R.string.mandatory_permission_message))
                .setPositiveButton(getString(R.string.mandatory_permission_positive_button_name), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionValidator();
                    }
                })
                .setNegativeButton(getString(R.string.mandatory_permission_negative_button_name), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resultCancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void settingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Settings?")
                .setMessage("Go to settings and enable permissions for the denied.")
                .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPermissionSettings((Activity) context);
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static RuntimePermissionsResult hasPermissions(Context context, String... permissions) {
        int count = 0;
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                }
            }
            if (count == permissions.length) {
                return RuntimePermissionsResult.ALL_PERMISSION_DENIED;
            } else if (count == 0) {
                return RuntimePermissionsResult.ALL_PERMISSION_ACCEPTED;
            } else {
                return RuntimePermissionsResult.SOME_PERMISSION_ACCEPTED_AND_DENIED;
            }
        }
        return null;
    }

    public static RuntimePermissionDenied hasPermissionsDenied(Context context, String... permissions) {
        int count = 0;
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                    count++;
                }
            }
            if (count == permissions.length) {
                return RuntimePermissionDenied.ALL_PERMISSION_DENIED_WITH_DO_NOT_ASK_AGAIN;
            } else if (count == 0) {
                return RuntimePermissionDenied.ALL_PERMISSION_DENIED_WITHOUT_DO_NOT_ASK_AGAIN;
            } else {
                return RuntimePermissionDenied.SOME_PERMISSION_DENIED_WITH_AND_WITHOUT_DO_NOT_ASK_AGAIN;
            }
        }
        return null;
    }

    public static void openPermissionSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        activity.startActivity(intent);
        activity.startActivityForResult(intent,REQUEST_APPS_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APPS_SETTINGS) {
            permissionValidator();
        }
    }


}
