package com.kokio.SecurityBridge;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class screenLockModule extends ReactContextBaseJavaModule implements ActivityEventListener {
  private final static String TAG = screenLockModule.class.getCanonicalName();
  private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL = 1;
  private final static String E_UNSUPPORTED_API_LEVEL = "android_version_unsupported";
  private final static String E_SCREEN_LOCK_ERROR = "screen_lock_error";
  private final static String E_BIOMETRIC_LOCK_ERROR = "biometric_lock_error";
  private final static String E_USER_DENIAL = "user_denied_intent";
  private Promise authenticationPromise;

  public screenLockModule(@NonNull ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addActivityEventListener(this);
  }

  @NonNull
  @Override
  public String getName() {
    return "screenLockModule";
  }

  @ReactMethod
  public void isDeviceSecure(Promise promise) throws Exception {
    try {
      KeyguardManager keyguardManager = (KeyguardManager) getReactApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
      boolean isSecure = keyguardManager.isDeviceSecure();
      promise.resolve(isSecure);
    } catch (Exception e) {
      Log.e(TAG, "Error: " + e.getMessage());
      e.printStackTrace();
      promise.reject(e);
    }
  }

  @ReactMethod
  public void authenticate(Promise promise) {
    // Reserve the activity thread for authetication
    UiThreadUtil.runOnUiThread(() -> {
      this.authenticationPromise = promise;

      Activity currentActivity = getCurrentActivity();
      if (currentActivity == null) {
        promise.reject("Activity doesn't exist");
        return;
      }
  
      KeyguardManager keyguardManager = (KeyguardManager) currentActivity.getSystemService(Context.KEYGUARD_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Check if device has a existing screen lock method registered
        if (keyguardManager.isDeviceSecure()) {
          Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Authentication Required", "Please authenticate to continue");
          if (intent != null) {
            currentActivity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL);
          } else {
            promise.reject(E_SCREEN_LOCK_ERROR, "Could not create intent for device credentials.");
          }
        } else {
          // @TODO Prompt user to register a screen lock method
          promise.reject(E_SCREEN_LOCK_ERROR, "Device is not secured.");
        }
      } else {
        promise.reject(E_UNSUPPORTED_API_LEVEL, "Device authentication is not supported on this Android version.");
      }
    });
  }

  @ReactMethod
  // BiometricManager API not available for Android 9 (API 28) and below
  @RequiresApi(api = Build.VERSION_CODES.P)
  public void authenticateWithBiometrics(Promise promise) {
    // Initialize the BiometricManager
    Context reactContext = getReactApplicationContext();
    BiometricManager biometricManager = reactContext.getSystemService(BiometricManager.class);

    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
      Executor executor = ContextCompat.getMainExecutor(getReactApplicationContext());

      // Initialize BiometricPrompt and its callback
      BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(reactContext)
              .setTitle("Biometric Authentication")
              .setSubtitle("Authenticate using Biometric Credentials")
              .setNegativeButton("Cancel", executor, (dialogInterface, i) -> {
                  // Handle the cancel button click
                  promise.reject(E_USER_DENIAL, "User canceled biometric authentication.");
              })
              .build();

      // Create a CancellationSignal to handle the cancel event
      CancellationSignal cancellationSignal = new CancellationSignal();
      cancellationSignal.setOnCancelListener(() -> {
          promise.reject(E_USER_DENIAL, "Biometric authentication cancelled.");
      });

      // Set the BiometricPrompt AuthenticationCallback
      biometricPrompt.authenticate(cancellationSignal, executor, new BiometricPrompt.AuthenticationCallback() {
          @Override
          public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
              super.onAuthenticationSucceeded(result);
              // Authentication was successful
              promise.resolve(true);
          }

          @Override
          public void onAuthenticationFailed() {
              super.onAuthenticationFailed();
              // Authentication failed
              promise.reject(E_BIOMETRIC_LOCK_ERROR, "Authentication failed.");
          }

          @Override
          public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
              super.onAuthenticationError(errorCode, errString);
              // Authentication error occurred
              promise.reject(E_BIOMETRIC_LOCK_ERROR, errString.toString());
          }
      });
    } else {
      // @TODO optionally create prompt to create a new biometric login
      promise.reject(E_BIOMETRIC_LOCK_ERROR, "Biometric authentication is not supported or not registered.");
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
