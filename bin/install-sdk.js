var https = require('https');
var request = require('request');
var path = require('path');
var unzipper = require('unzipper');

var sdks = {
  android: {
    url: 'https://repo.zender.tv/android/zender-android-sdk-v2.0.5.zip',
  },
  ios: {
    url: 'https://repo.zender.tv/ios/zender-ios-sdk-v2.1.0.zip'
  },
};

if (process.argv.length < 3) {
  throw new Error('Please specify SDK');
}

var sdk = sdks[process.argv[2]];

if (!sdk) {
  throw new Error('Unknown SDK specified, valid options: ' + Object.keys(sdks).join(', '));
}

request = require('request');

var stream = request({url: sdk.url, pool: new https.Agent({keepAlive: false})});
stream.pipe(unzipper.Extract({path: path.join(__dirname, '..')}));
