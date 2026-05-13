export const COLORS = {
  light: {
    primary: '#7C6FF7',
    primaryLight: '#A99DF8',
    primaryDark: '#5B4FCC',
    secondary: '#FF85A2',
    secondaryLight: '#FFB3C6',
    accent: '#FFD93D',
    success: '#6BCB77',
    warning: '#FFB84D',
    danger: '#FF6B6B',
    info: '#4FC3F7',

    bgPrimary: '#FDF6FF',
    bgSecondary: '#FFFFFF',
    bgSidebar: '#F5F0FF',
    bgInput: '#F9F6FF',

    textPrimary: '#2D2640',
    textSecondary: '#6E6B7B',
    textMuted: '#B4B0C0',
    textOnPrimary: '#FFFFFF',

    border: '#E8E0F0',
    borderLight: '#F2ECF8',
  },
  dark: {
    primary: '#A99DF8',
    primaryLight: '#C4BCFC',
    primaryDark: '#7C6FF7',
    secondary: '#FFB3C6',
    secondaryLight: '#FFD0DC',
    accent: '#FFE566',
    success: '#8FD99A',
    warning: '#FFC87D',
    danger: '#FF8F8F',
    info: '#73D8FD',

    bgPrimary: '#1A1726',
    bgSecondary: '#242133',
    bgSidebar: '#1E1B2D',
    bgInput: '#2A2742',

    textPrimary: '#E8E4F0',
    textSecondary: '#A6A0B8',
    textMuted: '#6E6880',
    textOnPrimary: '#1A1726',

    border: '#3A3550',
    borderLight: '#2E2A42',
  },
}

export const RADII = {
  sm: '8px',
  md: '12px',
  lg: '16px',
  xl: '24px',
  full: '9999px',
}

export const SHADOWS = {
  sm: '0 1px 3px rgba(124, 111, 247, 0.08)',
  md: '0 4px 12px rgba(124, 111, 247, 0.12)',
  lg: '0 8px 24px rgba(124, 111, 247, 0.16)',
  glow: '0 0 20px rgba(124, 111, 247, 0.2)',
}

export const TRANSITIONS = {
  fast: '150ms ease',
  normal: '300ms ease',
  slow: '500ms ease',
}

export function applyTheme(mode: 'light' | 'dark'): void {
  const colors = COLORS[mode]
  const root = document.documentElement
  for (const [key, value] of Object.entries(colors)) {
    root.style.setProperty(`--color-${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`, value)
  }
  for (const [key, value] of Object.entries(RADII)) {
    root.style.setProperty(`--radius-${key}`, value)
  }
  for (const [key, value] of Object.entries(SHADOWS)) {
    root.style.setProperty(`--shadow-${key}`, value)
  }
  for (const [key, value] of Object.entries(TRANSITIONS)) {
    root.style.setProperty(`--transition-${key}`, value)
  }
}
