module.exports = {
    content: [
        './components/**/*.{vue,js,ts}',
        './layouts/**/*.vue',
        './pages/**/*.vue',
        './plugins/**/*.{js,ts}',
        './nuxt.config.{js,ts}',
    ],
    theme: {
        extend: {
          colors: {
            primary: '#03C75A',
            'primary-dark': '#02AF4F',
            sidebar: '#222',
            'sidebar-dark': '#2d3748'
          },
          fontFamily: {
            sans: ['Pretendard', 'Inter', 'sans-serif']
          },
          boxShadow: {
            card: '0 2px 12px 0 rgba(0,0,0,0.08)'
          },
          borderRadius: {
            xl: '1rem'
          }
        }
      },
    plugins: [],
}