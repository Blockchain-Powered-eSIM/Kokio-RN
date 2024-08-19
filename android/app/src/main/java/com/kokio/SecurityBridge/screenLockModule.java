package com.kokio.SecurityBridge;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//@TODO add Biometric authetication aupport

public class screenLockModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL = 1;
    private Promise authenticationPromise;

    public screenLockModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "screenLockModule";
    }

    @ReactMethod
    public void authenticate(Promise promise) {
        this.authenticationPromise = promise;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("Activity doesn't exist");
            return;
        }

        KeyguardManager keyguardManager = (KeyguardManager) currentActivity.getSystemService(Context.KEYGUARD_SERVICE);
				// Check if device has a existing screen lock method registered
        if (keyguardManager.isDeviceSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Authentication Required", "Please authenticate to continue");
            if (intent != null) {
                currentActivity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL);
            }
        } else {
						// No active screen lock on device
						// @TODO Prompt user to register a screen lock
            promise.reject("No secure lock screen found");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL) {
            if (resultCode == Activity.RESULT_OK) {
                authenticationPromise.resolve(true);
            } else {
                authenticationPromise.reject("Authentication failed or canceled");
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // Not used in this scenario
    }
}
