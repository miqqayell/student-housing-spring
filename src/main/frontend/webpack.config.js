const path = require("path");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

// Bundle the embedded frontend into Spring Boot's static resources so the
// templates can load it from /built/bundle.{js,css}.
const OUTPUT_DIR = path.resolve(__dirname, "../resources/static/built");

module.exports = {
  entry: "./src/index.js",
  output: {
    path: OUTPUT_DIR,
    filename: "bundle.js",
    assetModuleFilename: "fonts/[name][ext]",
    clean: true,
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: "bundle.css" }),
  ],
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          "css-loader",
          {
            loader: "sass-loader",
            options: {
              sassOptions: {
                quietDeps: true,
                silenceDeprecations: ["import", "global-builtin", "color-functions"],
              },
            },
          },
        ],
      },
      {
        test: /\.css$/,
        use: [MiniCssExtractPlugin.loader, "css-loader"],
      },
      {
        test: /\.(woff2?|ttf|eot|svg)$/,
        type: "asset/resource",
      },
    ],
  },
  performance: {
    // Bootstrap + icons fonts inflate the bundle; the warnings are noisy in dev.
    hints: false,
  },
};
