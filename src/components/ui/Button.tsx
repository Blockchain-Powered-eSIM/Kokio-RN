import React from 'react';
import {View, Text, Pressable, StyleSheet} from 'react-native';
import {icons} from 'lucide-react-native';

import {Icon} from '@/components/ui/Icon';

type PrimaryButtonProps = {
  icon?: keyof typeof icons;
  children: React.ReactNode;
};

function PrimaryButton({children, icon}: PrimaryButtonProps) {
  function pressHandler() {
    console.log('Button pressed');
  }
  return (
    <View style={styles.buttonOuterContainer}>
      <Pressable
        style={styles.buttonInnerContainer}
        onPress={pressHandler}
        android_ripple={{color: '#41454a'}}>
        {icon && <Icon name={icon} size={16} />}
        <Text style={styles.buttonText}>{children}</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  // ! Dedicated pressed style for iOS not applied
  buttonOuterContainer: {
    borderRadius: 40,
    margin: 4,
    overflow: 'hidden',
  },
  buttonInnerContainer: {
    backgroundColor: '#373A3E',
    paddingVertical: 16,
    paddingHorizontal: 24,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 4,
  },
  buttonText: {
    color: 'white',
    textAlign: 'center',
  },
});

export default PrimaryButton;
