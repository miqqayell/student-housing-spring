import js from "@eslint/js";
import globals from "globals";

export default [
  js.configs.recommended,
  {
    files: ["src/**/*.js"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "module",
      globals: {
        ...globals.browser,
      },
    },
    rules: {
      "no-unused-vars": "warn",
      "prefer-const": "warn",
      eqeqeq: "error",
    },
  },
  {
    ignores: ["node_modules/**", "../resources/static/built/**"],
  },
];
