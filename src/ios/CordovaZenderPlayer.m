#import "CordovaZenderPlayer.h"
#import <Cordova/CDVPlugin.h>


@import Zender;

@interface CordovaZenderPlayer ()
@end

@implementation CordovaZenderPlayer {
    NSString *targetId;
    NSString *channelId;

    NSString *environment;
    NSString *deviceToken;
    NSString *redeemCode;
    NSString *backgroundColor;
    BOOL debugEnabled;
    
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
    environment = @"production";
         debugEnabled = FALSE;
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
    
    // Override endpoints if needed
    if ([environment isEqualToString:@"staging"]) {
        NSString *playerEndpoint=@"https://player2-native.staging.zender.tv";
        NSString *apiEndpoint=@"https://api.staging.zender.tv";
        NSString *logEndpoint=@"https://logs.staging.zender.tv/v1/ingest/batch";
        
        [settingsConfig overridePlayerEndpointPrefix:playerEndpoint];
        [settingsConfig overrideApiEndpointUrl:apiEndpoint];
        [[ZenderLogger sharedInstance] overrideEndpoint:logEndpoint];
        
    }
    
    // Set debug
    if (debugEnabled) {
        [settingsConfig enableDebug:debugEnabled];
    }
    
    // Register device
    if (deviceToken) {
        ZenderUserDevice *device = [[ZenderUserDevice alloc] init];
        [device setToken:deviceToken];
        settingsConfig.userDevice=device;
    }
    
    // Redeem quiz code if needed
    if (redeemCode) {
        [player redeemCodeQuiz:redeemCode];
    }
        
    player.config = settingsConfig;
    
    // Use authentication
    player.authentication = authentication;
    
    // Set this class as a ZenderPlayerDelegate
    player.delegate = self;
    
    player.view.frame = self.webView.frame;
    player.view.hidden = false;
    
    if (backgroundColor) {
        
    } else {
        player.view.backgroundColor = [UIColor blackColor];
    }
    
    [player start];
    
    [self.webView.superview insertSubview: player.view aboveSubview:self.webView];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
    
    
}

#pragma mark Player Getters/Setters

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
    
    // debugEnabled
    // deviceToken
    // backgroundColor
    // redeemCode
    
    NSDictionary *jsonConfig = command.arguments[0];

    if (jsonConfig) {
        // needs check key
       // debugEnabled = [[jsonConfig objectForKey:@"debug"] boolValue];
       // deviceToken = [[jsonConfig objectForKey:@"deviceToken"] string];
       // redeemCode = [[jsonConfig objectForKey:@"redeemCode"] string];
        // environment = [[jsonConfig objectForKey:@"environment"] string];

    }
    
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

- (void) setAuthentication: (CDVInvokedUrlCommand*) command {
    NSString *provider = command.arguments[0];
    NSDictionary *jsonAuth = command.arguments[1];
    
    authentication = [ZenderAuthentication authenticationWith:jsonAuth provider:provider];
    [self.commandDelegate sendPluginResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId: command.callbackId];
}

#pragma mark Player Delegate Functions
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

- (void)zenderPlayer:(ZenderPlayer *)zenderPlayer onZenderQuizShareCode:(NSDictionary *)payload {
    [zenderPlayer sharePayload:payload controller:self];
}

@end
