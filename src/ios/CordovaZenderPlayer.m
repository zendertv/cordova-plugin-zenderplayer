#import "CordovaZenderPlayer.h"

@import Zender;

@interface CordovaZenderPlayer () 
@end

@implementation CordovaZenderPlayer {
    NSString *targetId;
    NSString *channelId;
    ZenderPlayer *player; 
    ZenderAuthentication *authentication;

    NSString *onZenderPlayerCloseCallbackId;

}

- (id) init {
    if (self = [super init]) {
        targetId = nil;
        channelId = nil;
	authentication = nil;
	player = nil;
    }
    return self;
}

- (void) start: (CDVInvokedUrlCommand*) command {

    // Create a Zender Player
    if (player == nil) {
    	player= [ZenderPlayer new];
    }
    
    // Create a player configuration
    ZenderPlayerConfig* settingsConfig = [ZenderPlayerConfig configWithTargetId:targetId channelId:channelId];
    player.config = settingsConfig;

    // Use authentication
    player.authentication = authentication;

    // Set this class as a ZenderPlayerDelegate
    player.delegate = self;
    
    player.view.frame = self.webView.frame;
    player.view.hidden = false;
    
    [player start];
    
    [self.webView.superview insertSubview: player.view aboveSubview:self.webView];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];


}

- (void) stop: (CDVInvokedUrlCommand*) command {
    [player stop];
    [player.view removeFromSuperview];
    player.delegate = nil;
    player = nil;
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) setTargetId: (CDVInvokedUrlCommand*) command {
    targetId = command.arguments[0];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) setChannelId: (CDVInvokedUrlCommand*) command {
    channelId = command.arguments[0];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) setConfig: (CDVInvokedUrlCommand*) command {
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) setAuthentication: (CDVInvokedUrlCommand*) command {
     NSString *provider = command.arguments[0];
     NSDictionary *jsonAuth = command.arguments[1];

     authentication = [ZenderAuthentication authenticationWith:jsonAuth provider:provider];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) onZenderPlayerClose: (CDVInvokedUrlCommand*) command {
    onZenderPlayerCloseCallbackId = command.callbackId;
}

- (void)zenderPlayer:(ZenderPlayer *)zenderPlayer onZenderPlayerClose:(NSDictionary *)payload {

    if (onZenderPlayerCloseCallbackId != nil) {
        NSString *res = @"closed";
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:(CDVCommandStatus)CDVCommandStatus_OK messageAsString:res];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult: result callbackId: onZenderPlayerCloseCallbackId];
   } 
}

@end
