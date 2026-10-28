/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          orange: '#FF5E00',
          orangeLight: '#FF8533',
          orangeDark: '#D64D00',
          turquoise: '#00ADB5',
          cyan: '#00D2D3',
          navy: '#0A0E1A',
          surface: '#111827',
          surfaceLight: '#FFFFFF',
          cardDark: '#1E293B',
          cardLight: '#F8FAFC',
        }
      },
      fontFamily: {
        cairo: ['Cairo', 'Tajawal', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
