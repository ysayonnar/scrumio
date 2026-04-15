/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans:    ['"DM Sans"', 'system-ui', 'sans-serif'],
        display: ['"Syne"', 'sans-serif'],
        mono:    ['"DM Mono"', 'Menlo', 'monospace'],
      },
      colors: {
        parchment: {
          50:  '#faf9f6',
          100: '#f4f2ec',
          200: '#ece9e0',
          300: '#e0dcd3',
          400: '#ccc8be',
          500: '#a8a49a',
        },
        brand: {
          DEFAULT: '#e8450a',
          dark:    '#c63b08',
          light:   '#fef0ea',
          mid:     'rgba(232,69,10,0.12)',
        },
      },
      boxShadow: {
        'card':    '0 1px 4px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)',
        'card-md': '0 4px 16px rgba(0,0,0,0.09), 0 2px 6px rgba(0,0,0,0.05)',
        'card-lg': '0 12px 40px rgba(0,0,0,0.11), 0 4px 12px rgba(0,0,0,0.06)',
        'modal':   '0 20px 60px rgba(0,0,0,0.14), 0 6px 20px rgba(0,0,0,0.08)',
      },
    },
  },
  plugins: [],
}
