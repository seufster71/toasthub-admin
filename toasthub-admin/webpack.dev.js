const merge = require('webpack-merge');
const common = require('./webpack.common.js');
const path = require('path');
const BUILD_DIR = path.resolve(__dirname, 'src/main/resources/static/admin/build');

module.exports = merge(common, {
  devtool: 'inline-source-map',
  devServer: {
    inline: true,
    contentBase: './src/main/resources/static/admin',
    proxy: [{ context: ["/api/**","/libs/**"],target: 'http://localhost:8090' }]
  },
  output: {
    path: BUILD_DIR,
    filename: 'bundle.js',
    publicPath: 'build/'
  },
});
