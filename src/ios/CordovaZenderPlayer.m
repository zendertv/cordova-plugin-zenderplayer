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
    
    // Set the background
    if (backgroundColor) {
        player.view.backgroundColor = [CordovaZenderPlayer colorWithHexString:backgroundColor];
    } else {
        player.view.backgroundColor = [UIColor blackColor];
    }
    
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


+ (UIColor *)colorWithHexString:(NSString *)stringToConvert
{
    NSString *noHashString = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@""]; // remove the #
    NSScanner *scanner = [NSScanner scannerWithString:noHashString];
    [scanner setCharactersToBeSkipped:[NSCharacterSet symbolCharacterSet]]; // remove + and $
    
    unsigned hex;
    if (![scanner scanHexInt:&hex]) return nil;
    int r = (hex >> 16) & 0xFF;
    int g = (hex >> 8) & 0xFF;
    int b = (hex) & 0xFF;
    
    return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:1.0f];
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
    
    NSDictionary *jsonConfig = command.arguments[0];
    
    if (jsonConfig) {
        if ([jsonConfig objectForKey:@"debug"]) {
            debugEnabled = [[jsonConfig objectForKey:@"debug"] boolValue];
        }
        
        if ([jsonConfig objectForKey:@"deviceToken"]) {
            deviceToken = [[jsonConfig objectForKey:@"deviceToken"] stringValue];
        }

        if ([jsonConfig objectForKey:@"redeemCode"]) {
            redeemCode = [[jsonConfig objectForKey:@"redeemCode"] stringValue];
        }
        
        
        if ([jsonConfig objectForKey:@"backgroundColor"]) {
            backgroundColor = [NSString stringWithString:[jsonConfig objectForKey:@"backgroundColor"]];
        }
        
        if ([jsonConfig objectForKey:@"environment"]) {
            environment = [NSString stringWithString:[jsonConfig objectForKey:@"environment"]];
        }
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
    [zenderPlayer sharePayload:payload controller:self.viewController];
}

@end
