# cordova-plugin-zenderplayer

Note: This is work in progress.

## Installation
In your Cordova project, run:
	`cordova plugin add https://github.com/zendertv/cordova-plugin-zenderplayer#v0.0.1`

## Usage

Use the Cordova-style callback-based methods of window.ZenderPlayer from within your Cordova application.

Callback-based example:
```
var targetId  = "<your targetId>";
var channelId = "<your channelId>";

// Using device authentication
var authProvider = "device";
var authPayload = {
     'token': 'test-user',
     'name': 'patrick'
};

// Add the callback
var listenerId=ZenderPlayer.addEventListener('onZenderPlayerClose',function(s) {
     console.log("received a close");

     ZenderPlayer.stop(function() {}, function() {});
     ZenderPlayer.removeEventListener(listenerId);
}, function(e) {
});

ZenderPlayer.setAuthentication(authProvider, authPayload , function() {
	ZenderPlayer.setTargetId(targetId, function() {
		ZenderPlayer.setChannelId(channelId, function() {
			ZenderPlayer.start(function() { } , function(err) { console.log(err)});
		} , function(err) { console.log(err)});
	} , function(err) { console.log(err)});
} , function(err) { console.log(err)});
```

## TODOs
- expose more callbacks
- issue with android keyboard not appearing correctly
- need to add run-script for iOS manually to strip the frameworks

## Thanks
This plugin was inspired by the [cordova plugin for Bambuser](https://github.com/bambuser/cordova-plugin-bambuser)
