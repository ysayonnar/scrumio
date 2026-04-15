/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        void: {
          950: '#04040a',
          900: '#080810',
          800: '#0f0f1a',
          750: '#131322',
          700: '#181828',
          650: '#1c1c30',
          600: '#202038',
          500: '#282840',
          400: '#383858',
          300: '#525278',
          200: '#7878a0',
          100: '#9898c0',
          50:  '#c8c8e0',
        },
        lime: {
          DEFAULT: '#c8ff4a',
          dim: '#8ab832',
        },
        ember: '#ff6b35',
      },
    },
  },
  plugins: [],
}
