/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/**/*.cljs"],
  theme: {
    extend: {},
  },
  plugins: [],
  corePlugins: {
    preflight: false, // Disables header styling!
  }
}
