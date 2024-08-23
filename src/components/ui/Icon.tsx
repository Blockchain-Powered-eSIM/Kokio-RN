import React from 'react';
import {icons} from 'lucide-react-native';

type IconProps = {
  name: keyof typeof icons; // Ensures that name is a valid key of the icons object
  color?: string;
  size?: number; // Made size optional with a default value, similar to how size is fixed in TabBarIcon
};

const Icon = ({name, color = 'white', size = 24}: IconProps) => {
  const LucideIcon = icons[name];
  if (!LucideIcon) {
    // Optionally handle the case where the icon name does not exist in the icons object
    console.error(`Icon "${name}" not found in lucide-icons`);
    return null;
  }

  return <LucideIcon color={color} size={size} style={{marginBottom: -3}} />;
};

export {Icon};
