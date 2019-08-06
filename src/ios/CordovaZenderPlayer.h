#import <Cordova/CDVPlugin.h>
@import Zender;

@interface CordovaZenderPlayer : CDVPlugin<ZenderPlayerDelegate> {
}

- (void) setTargetId: (CDVInvokedUrlCommand*) command;
- (void) setChannelId: (CDVInvokedUrlCommand*) command;
- (void) setAuthentication: (CDVInvokedUrlCommand*) command;
- (void) setConfig: (CDVInvokedUrlCommand*) command;
- (void) start: (CDVInvokedUrlCommand*) command;
- (void) stop: (CDVInvokedUrlCommand*) command;
- (void) onZenderPlayerClose: (CDVInvokedUrlCommand*) command;

@end
