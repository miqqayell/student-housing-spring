const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
  entry: "./src/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "bundle.[contenthash].js",
    clean: true,
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: "./src/index.html",
    }),
  ],
  module: {
    rules: [
      {
        // Plain CSS (e.g. bootstrap-icons) injected into the page
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
      {
        // Font files referenced by bootstrap-icons
        test: /\.(woff2?|ttf|eot|svg)$/,
        type: "asset/resource",
      },
      {
        // SCSS -> CSS injected into the page (style-loader)
        test: /\.scss$/,
        use: [
          "style-loader",
          "css-loader",
          {
            loader: "sass-loader",
            options: {
              // Silence the deprecation warnings emitted by Bootstrap 5.3 when
              // compiled with modern Dart Sass (@import rule + legacy color funcs).
              sassOptions: {
                quietDeps: true,
                silenceDeprecations: [
                  "import",
                  "global-builtin",
                  "color-functions",
                ],
              },
            },
          },
        ],
      },
    ],
  },
  devServer: {
    static: path.resolve(__dirname, "dist"),
    port: 9000, // must match the CORS allowed origin in the Spring backend
    open: true,
    hot: true,
    client: {
      // don't block the UI with an overlay for (non-fatal) warnings
      overlay: { errors: true, warnings: false },
    },
  },
};
