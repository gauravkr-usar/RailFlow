/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        rail: {
          orange: '#FB8C00',
          'orange-dark': '#E65100',
          'orange-light': '#FFE0B2',
          navy: '#1A365D',
          'navy-light': '#2A4365',
          'navy-dark': '#0F172A',
          blue: '#0284C7',
          gray: '#F8FAFC',
          card: '#FFFFFF',
          border: '#E2E8F0',
          success: '#10B981',
          warning: '#F59E0B',
          danger: '#EF4444',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
