package com.kokio.DeviceInfoBridge;

import static android.content.Context.EUICC_SERVICE;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.euicc.EuiccManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.euicc.DownloadableSubscription;
import android.content.IntentFilter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.BroadcastReceiver;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.module.annotations.ReactModule;

import java.util.List;

public class simDataModule extends ReactContextBaseJavaModule {
  private final static String TAG = simDataModule.class.getCanonicalName();
  private String ACTION_DOWNLOAD_SUBSCRIPTION = "download_subscription";
  private ReactContext mReactContext;

  @RequiresApi(api = Build.VERSION_CODES.P)
  private EuiccManager mEuiccManager;

  public simDataModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
  }

  @RequiresApi(api = Build.VERSION_CODES.P)
  private void initEuiccManager() {
    if (mEuiccManager == null) {
      mEuiccManager = (EuiccManager) mReactContext.getSystemService(EUICC_SERVICE);
    }
  }

  @Override
  @NonNull
  public String getName() {
    return "simDataModule";
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
  @ReactMethod
  public void getSimCardsNative(Promise promise) {
    WritableArray simCardsList = new WritableNativeArray();

    TelephonyManager mTelephonyManager = (TelephonyManager) mReactContext.getSystemService(Context.TELEPHONY_SERVICE);
    try {
      SubscriptionManager mSubscriptionManager = (SubscriptionManager) mReactContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
        int activeSubscriptionInfoCount = mSubscriptionManager.getActiveSubscriptionInfoCount();
        int activeSubscriptionInfoCountMax = mSubscriptionManager.getActiveSubscriptionInfoCountMax();

        List<SubscriptionInfo> subscriptionInfos = mSubscriptionManager.getActiveSubscriptionInfoList();

        for (SubscriptionInfo subInfo : subscriptionInfos) {
          WritableMap simCard = Arguments.createMap();

          String number = "";
          if(android.os.Build.VERSION.SDK_INT >= 33) {
            number = mSubscriptionManager.getPhoneNumber(subInfo.getSubscriptionId());
          } else {
            number = subInfo.getNumber();
          } 

          CharSequence carrierName = subInfo.getCarrierName();
          String countryIso = subInfo.getCountryIso();
          int dataRoaming = subInfo.getDataRoaming(); // 1 is enabled ; 0 is disabled
          CharSequence displayName = subInfo.getDisplayName();
          String iccId = subInfo.getIccId();
          int mcc = subInfo.getMcc();
          int mnc = subInfo.getMnc();
          int simSlotIndex = subInfo.getSimSlotIndex();
          int subscriptionId = subInfo.getSubscriptionId();
          int networkRoaming = mTelephonyManager.isNetworkRoaming() ? 1 : 0;

          simCard.putString("carrierName", carrierName.toString());
          simCard.putString("displayName", displayName.toString());
          simCard.putString("isoCountryCode", countryIso);
          simCard.putInt("mobileCountryCode", mcc);
          simCard.putInt("mobileNetworkCode", mnc);
          simCard.putInt("isNetworkRoaming", networkRoaming);
          simCard.putInt("isDataRoaming", dataRoaming);
          simCard.putInt("simSlotIndex", simSlotIndex);
          simCard.putString("phoneNumber", number);
          simCard.putString("simSerialNumber", iccId);
          simCard.putInt("subscriptionId", subscriptionId);

          simCardsList.pushMap(simCard);
        }
      } else {
        promise.reject("0", "This functionality is not supported before Android 5.1 (22)");
      }
    } catch (Exception e) {
      promise.reject("1", "Something goes wrong to fetch simcards: " + e.getLocalizedMessage());
    }
    promise.resolve(simCardsList);
  }

  @RequiresApi(api = Build.VERSION_CODES.P)
  @ReactMethod
  public void isEsimSupported(Promise promise) {
    initEuiccManager();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mEuiccManager != null) {
      promise.resolve(mEuiccManager.isEnabled());
    } else {
      promise.resolve(false);
    }
    return;
  }

  private boolean checkCarrierPrivileges() {
    TelephonyManager mTelephonyManager = (TelephonyManager) mReactContext.getSystemService(Context.TELEPHONY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      return mTelephonyManager.hasCarrierPrivileges();
    } else {
      return false;
    }
  }

}
