package tv.zender.cordova;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.view.WindowManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import tv.zender.ZenderAuthentication;
import tv.zender.player.ZenderPlayerConfig;
import tv.zender.player.ZenderPlayerListener;
import tv.zender.player.ZenderPlayerView;
import tv.zender.player.video.ZenderMediaPlayerView;
import tv.zender.player.video.ZenderPhenixVideoView;

import java.util.HashMap;
import java.util.Map;



import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CordovaZenderPlayer extends CordovaPlugin implements ZenderPlayerListener{
    
    private ZenderPlayerView zenderPlayerView = null;
    private ZenderAuthentication zenderAuthentication;
    private HashMap<String, Object> mAuthentication;
    private String mProvider = null;
    private String mTargetId = null;
    private String mChannelId = null;    
    private CordovaZenderPlayer self;
    private int mSoftInputMode;

    private CallbackContext onZenderPlayerCloseCallbackContext;
  
  
    /* CordovaPlugin methods */
    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);
        self=this;
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        //log("Executing Cordova plugin action: " + action);

        if ("setTargetId".equals(action)) {
            mTargetId = args.getString(0);
            callbackContext.success("targetId set");
        }

        if ("setChannelId".equals(action)) {
            mChannelId = args.getString(0);
            callbackContext.success("channelId set");
        }

        if ("setAuthentication".equals(action)) {
            mProvider = args.getString(0);
            // Prepare zenderAuthentication
            HashMap<String, Object> authenticationMap = new com.google.gson.Gson().fromJson(args.getJSONObject(1).toString(), HashMap.class);
            zenderAuthentication = new ZenderAuthentication(authenticationMap, mProvider);
            callbackContext.success("authentication set");
        }

        if ("setConfig".equals(action)) {
            // TODO read config params
            callbackContext.success("config set");
        }

        if ("start".equals(action)) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // Store the current softInputMode
                    mSoftInputMode = self.cordova.getActivity().getWindow().getAttributes().softInputMode;
                    // Set the input mode to AdjustPan so the keyboard works correctly
                    self.cordova.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    
                    // this is the default for cordova
                    // cordova.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    ViewGroup parentView = (ViewGroup) webView.getView().getParent();
                    Context context = parentView.getContext();
                    
                    zenderPlayerView = new ZenderPlayerView(context);
                    
                    ZenderPlayerConfig playerConfig = new ZenderPlayerConfig(mTargetId, mChannelId);
                    String playerEndpointPrefix = "https://player2-native.zender.tv";
                    playerConfig.overridePlayerEndpointPrefix(playerEndpointPrefix);

                    // Register video players
                    ZenderPhenixVideoView phenixVideoView = new ZenderPhenixVideoView(context);
                    playerConfig.registerVideoView(phenixVideoView);

                    ZenderMediaPlayerView mediaPlayerView = new ZenderMediaPlayerView(context);
                    playerConfig.registerVideoView(mediaPlayerView);

                    // Prepare for final configuration
                    zenderPlayerView.setConfig(playerConfig);
                    zenderPlayerView.setAuthentication(zenderAuthentication);
                    
                    // Registering as listener for player events
                    zenderPlayerView.registerZenderPlayerListener(self);

                    webView.getView().setBackgroundColor(android.R.color.transparent);
                    RelativeLayout.LayoutParams previewLayoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    parentView.addView(zenderPlayerView, 0, previewLayoutParams);
                    parentView.bringChildToFront(zenderPlayerView);
                    
                    //    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
                
                    /*
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        final WebSettings settings = ((WebView)webView.getView()).getSettings();
                        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                        }
                        */

                    
                    zenderPlayerView.start();
                    
                    callbackContext.success("Zender Player started");
                }
            });
            return true;
        }

        if ("stop".equals(action)) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Restore softInputMode
                    self.cordova.getActivity().getWindow().setSoftInputMode(mSoftInputMode);
                    zenderPlayerView.stop();
                    zenderPlayerView.unregisterZenderPlayerListener(self);
                    zenderPlayerView.release();
                    callbackContext.success("Zender Player stopped");
                }
            });
            return true;
        }
        
        // Register the callbacks
        if ("onZenderPlayerClose".equals(action)) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    self.onZenderPlayerCloseCallbackContext = callbackContext;
                }
            });
            return true;
        }

        return false;
    }

    /**
        * Called when the system is about to start resuming a previous activity.
        */
        @Override
    public void onPause(boolean multitasking) {
        //super.onPause();
        this.zenderPlayerView.pause();
    }

    /**
        * Called when the activity will start interacting with the user.
        */
        @Override
    public void onResume(boolean multitasking) {
        //super.onResume();
        this.zenderPlayerView.resume();
    }

    /**
        * Called when the activity is becoming visible to the user.
        */
        @Override
    public void onStart() {
    }

    /**
        * Called when the activity is no longer visible to the user.
        */
        @Override
    public void onStop() {
    }

    /**
        * The final call you receive before your activity is destroyed.
        */
        @Override
    public void onDestroy() {
        // super.onDestroy();
        this.zenderPlayerView.stop();
        this.zenderPlayerView.unregisterZenderPlayerListener(this);
        this.zenderPlayerView.release();
    }

    /**
        * Called when the WebView does a top-level navigation or refreshes.
        *
        * Plugins should stop any long-running processes and clean up internal state.
        *
        * Does nothing by default.
        */
        @Override
    public void onReset() {
       
    }
    
    @Override
    public void onZenderReady(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderFail(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPlayerFail(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPlayerReady(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPlayerClose(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {
        if (this.onZenderPlayerCloseCallbackContext != null) {
            String res = null;
            res = "close";
            if (res != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, res);
                result.setKeepCallback(true);
                this.onZenderPlayerCloseCallbackContext.sendPluginResult(result);
            }
        }

    }

    @Override
    public void onZenderPlayerLobbyEnter(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPlayerLobbyLeave(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAuthenticationInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAuthenticationRequired(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAuthenticationClear(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAuthenticationFail(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAuthenticationSuccess(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderTargetsInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderChannelsStreamsInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderChannelsStreamsPublish(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderChannelsStreamsUnpublish(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderStreamsInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderStreamsUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderStreamsDelete(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderStreamsStats(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderMediaInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderMediaUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderMediaDelete(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderMediaPlay(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxReplies(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxShout(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxShouts(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxShoutsDelete(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxShoutSent(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxDisable(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderShoutboxEnable(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderEmojisInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderEmojisUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderEmojisStats(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderEmojisTrigger(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAvatarsStats(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAvatarsTrigger(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsDelete(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsReset(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsVote(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsResults(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderPollsResultsAnimate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAppActivate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAppDeactivate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizInit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizUpdate(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizStart(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizStop(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizReset(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizDelete(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizQuestion(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizAnswer(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizAnswerTimeout(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizAnswerSubmit(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizAnswerCorrect(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizAnswerIncorrect(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizExtralifeUse(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizExtralifeIgnore(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizEliminated(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizEliminatedContinue(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizWinner(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizLoser(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizQuestionResults(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderQuizShareCode(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

        //  dispatchOnZenderPlayerQuizShareCode(linkedTreeMap);

    }

    @Override
    public void onZenderQuizResults(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderUiStreamsOverview(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderOpenUrl(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderAdsShow(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderLoaderShow(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderLoaderHide(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

    @Override
    public void onZenderLoaderTimeout(com.google.gson.internal.LinkedTreeMap linkedTreeMap) {

    }

}
