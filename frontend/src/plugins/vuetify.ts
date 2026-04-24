import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

export default createVuetify({
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: { mdi },
  },
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        dark: false,
        colors: {
          primary:    '#1D4ED8',
          secondary:  '#4F46E5',
          accent:     '#F59E0B',
          error:      '#DC2626',
          warning:    '#D97706',
          info:       '#0284C7',
          success:    '#059669',
          surface:    '#FFFFFF',
          background: '#F1F5F9',
        },
      },
      dark: {
        dark: true,
        colors: {
          primary:    '#60A5FA',   // Blue-400  — pops on dark bg
          secondary:  '#818CF8',   // Indigo-400
          accent:     '#FCD34D',   // Amber-300
          error:      '#F87171',   // Red-400
          warning:    '#FCD34D',   // Amber-300
          info:       '#38BDF8',   // Sky-400
          success:    '#34D399',   // Emerald-400
          surface:    '#1E293B',   // Slate-800  — cards / dialogs
          background: '#0F172A',   // Slate-900  — page bg
        },
      },
    },
  },
})
