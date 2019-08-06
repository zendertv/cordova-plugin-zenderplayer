var Player = {}

var exec = require('cordova/exec');
var utils = require('cordova/utils');

var playerVisible = false;
var queue = Promise.resolve();

var execQueue = function() {
    var execArgs = Array.prototype.slice.call(arguments);
    queue = queue.then(function() {
        return new Promise(resolve => {
            var userCb = execArgs.shift();
            var userEb = execArgs.shift();
            execArgs.unshift(function() {
                // Custom errback that resolves the queue promise before triggering actual errback
                resolve();
                if (userEb) userEb.apply(null, arguments);
            });
            execArgs.unshift(function() {
                // Custom callback that resolves the queue promise before triggering actual callback
                resolve();
                if (userCb) userCb.apply(null, arguments);
            });
            exec.apply(null, execArgs);
        });
    });
};

Player.setTargetId = function(targetId, successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    if (!targetId) {
        errorCallback('A targetId is required');
        return res;
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'setTargetId', [targetId]);
    return res;
}

Player.setChannelId = function(channelId, successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    if (!channelId) {
        errorCallback('A channelId is required');
        return res;
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'setChannelId', [channelId]);
    return res;
}

Player.setConfig = function(config, successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    if (!config) {
        errorCallback('Config is required');
        return res;
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'setConfig', [config]);
    return res;
}

Player.setAuthentication = function(provider,authentication, successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    if (!authentication) {
        errorCallback('Authentication is required');
        return res;
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'setAuthentication', [provider, authentication]);
    return res;
}

Player.start = function(successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'start', []);
    return res;
};

Player.stop = function(successCallback, errorCallback) {
    var res;
    if (!successCallback) {
        res = new Promise(function (resolve, reject) { successCallback = resolve; errorCallback = reject; });
    }
    execQueue(successCallback, errorCallback, 'CordovaZenderPlayer', 'stop', []);
    return res;
};

Player._eventListeners = {};

Player.addEventListener = function(event, successCallback, errorCallback) {
    var id = utils.createUUID();
    Player._eventListeners[id] = {
        event: event,
        callback: successCallback,
    };
    Player._ensureSubscribed();
    return id;
};

Player.removeEventListener = function(id) {
    delete Player._eventListeners[id];
}

Player._emitEvent = function(eventName, payload) {
    Object.keys(Player._eventListeners).forEach(function(id) {
        if (typeof Player._eventListeners[id] === 'object') {
            var listener = Player._eventListeners[id];
            if (listener.event === eventName) {
                listener.callback(payload);
            }
        }
    });
}

Player._isSubscribed = false;

Player._ensureSubscribed = function() {
    if (Player._isSubscribed) return;
    Player._isSubscribed = true;

    exec(function(event) {
        console.log('playerclose');
        console.log(event);
        Player._emitEvent('onZenderPlayerClose', event);
    }, function(e) {
        console.log('ZenderPlayer: failed to subscribe to onZenderPlayerClose', e);
    }, 'CordovaZenderPlayer', 'onZenderPlayerClose', []);

};


module.exports = Player;
