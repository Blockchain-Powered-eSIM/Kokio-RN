# Security Bridge for React Native

## ScreenLockModule
The `screenLock` module provides secure authentication methods for React Native applications on Android. It enables the app to enforce device security by verifying screen locks and biometric authentication (e.g., fingerprint or face recognition). This module ensures that only authenticated users can access sensitive parts of the application, making it ideal high security standard compliance.

### Dependencies
This module depends on the React Native framework and utilises Android's native `KeyGaurdManager`, `BiometricManager` and `BiometricPrompt` API's.
- Requires `Manifest.permission.SUBSCRIBE_TO_KEYGUARD_LOCKED_STATE`
- Requires `Manifest.permission.USE_BIOMETRIC`
- Android API level 23 `(Marshmallow)` or higher is required for screen lock authentication
- Android API level 28 `(Pie)` or higher is required for biometric authentication

### Methods
#### isDeviceSecure
Checks if the device has any form of secure lock screen (PIN, pattern, password, etc.) enabled. Takes as a parameter a React Native Promise that resolves to a boolean value indicating whether the device is secured.

#### authenticate
Prompts the user to authenticate using their device’s screen lock method (PIN, pattern, password, etc.). This method is suitable for devices running Android API level 23 (Marshmallow) and above. Input parameter is a React Native Promise that resolves to true if authentication is successful or rejects with an error if authentication fails.

#### authenticateWithBiometrics
Prompts the user to authenticate using their biometric credentials (fingerprint, face recognition, etc.). This method is available for devices running Android API level 28 (Pie) and above. Input parameter is a React Native Promise that resolves to true if biometric authentication is successful or rejects with an error if authentication fails.

### Example usage
```java
import React from 'react';
import { View, Button, Alert } from 'react-native';
import { NativeModules } from 'react-native';

const { screenLockModule } = NativeModules;

const App = () => {
  
  const handleScreenLockAuth = () => {
    screenLockModule.authenticate()
      .then(() => {
        Alert.alert('Success', 'Screen lock authentication successful.');
      })
      .catch(error => {
        Alert.alert('Error', `Screen lock authentication failed: ${error.message}`);
      });
  };

  const handleBiometricAuth = () => {
    screenLockModule.authenticateWithBiometrics()
      .then(() => {
        Alert.alert('Success', 'Biometric authentication successful.');
      })
      .catch(error => {
        Alert.alert('Error', `Biometric authentication failed: ${error.message}`);
      });
  };

  return (
    <View>
      <Button title="Authenticate with Screen Lock" onPress={handleScreenLockAuth} />
      <Button title="Authenticate with Biometrics" onPress={handleBiometricAuth} />
    </View>
  );
};

export default App;
```

The module uses promises to handle asynchronous operations and will reject these promises with an error code and message when something goes wrong. Handle these errors appropriately in your application to ensure reliable functionality.
