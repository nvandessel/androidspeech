package com.mozilla.speechlibrary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mozilla.speechlibrary.stt.STTResult;
import com.mozilla.speechlibrary.utils.ModelUtils;
import com.unity3d.player.UnityPlayer;

public class UnitySTTPlugin implements SpeechResultCallback {

    private static UnitySTTPlugin s_instance = null;

    public static synchronized UnitySTTPlugin getInstance() {
        if(s_instance == null){
            s_instance = new UnitySTTPlugin();
        }
        return s_instance;
    }

    protected static final String TAG = "UnitySTTPlugin";
    private static final String GAME_OBJECT_NAME = "DeepSpeech";
    private static final String SPEECH_STATUS_CHANGED = "onSpeechStatusChanged";
    private static final String MODEL_PATH_REQUEST = "onModelPathRequest";

    private final String mModelPath;
    private SpeechService mSpeechService;

    private UnitySTTPlugin() {
        Log.i(TAG, "<" + this + "> UnitySTTPlugin() constructor called");
        mModelPath = Environment.getExternalStorageDirectory() + "/deepspeech";
        Log.d(TAG, "<" + this + "> UnitySTTPlugin has been created, model in " + mModelPath);
    }

    public void initialize(@NonNull Context context) {
        Log.i(TAG, "<" + this + "> initialize() called with: context = [" + context + "]");
        if (!ModelUtils.isReady(mModelPath)) {
            Log.e(TAG, "ModelPath is not ready! Cannot Initialize STT Service");
            return;
        }

        Log.d(TAG, "<" + this + "> initialize: init speech service");
        if (mSpeechService == null) {
            mSpeechService = new SpeechService(context);
        }

        Log.d(TAG, "<" + this + "> initialize: init speech service settings");
        SpeechServiceSettings.Builder builder = new SpeechServiceSettings.Builder()
                .withUseDeepSpeech(true)
                .withModelPath(mModelPath);

        Log.d(TAG, "<" + this + "> initialize: starting speech service");
        mSpeechService.start(builder.build(), this);
    }

    public void stopSTT() {
        if (mSpeechService == null) {
            Log.d(TAG, "<" + this + "> stopSTT: speech service is null, can't stop it");
            return;
        }
        Log.d(TAG, "<" + this + "> stopSTT: stopping speech service");
        mSpeechService.stop();
    }

    // SpeechResultCallback

    @Override
    public void onStartListen() {
        Log.d(TAG, "<" + this + "> onStartListen() called");
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Started to listen\n");
    }

    @Override
    public void onMicActivity(double fftsum) {
        Log.d(TAG, "<" + this + "> onMicActivity() called with: fftsum = [" + fftsum + "]");
    }

    @Override
    public void onDecoding() {
        Log.d(TAG, "<" + this + "> onDecoding() called");
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Decoding... \n");
    }

    @Override
    public void onSTTResult(@Nullable STTResult result) {
        Log.d(TAG, "<" + this + "> onSTTResult() called with: result = [" + result + "]");
        SendUnityMessage(SPEECH_STATUS_CHANGED, String.format("Success: %s\n", result.mTranscription + "\n"));
    }

    @Override
    public void onIntDecResult(@Nullable STTResult result) {
        Log.d(TAG, "<" + this + "> onIntDecResult() called with: result = [" + result + "]");
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Int:" + result.mTranscription + "\n");
    }

    @Override
    public void onNoVoice() {
        Log.d(TAG, "<" + this + "> onNoVoice() called");
        SendUnityMessage(SPEECH_STATUS_CHANGED, "No Voice detected\n");
    }

    @Override
    public void onError(int errorType, @Nullable String error) {
        Log.d(TAG, "<" + this + "> onError() called with: errorType = [" + errorType + "], error = [" + error + "]");
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Error:" + error);
    }

    //Unity Implementation

    protected void SendUnityMessage(String methodName, String message) {
        Log.i(TAG, "<" + this + "> SendUnityMessage() called with: methodName = [" + methodName + "], message = [" + message + "]");
        UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, methodName, message);
    }

    public String getModelPath() {
        return this.mModelPath;
    }

    public void publishModelPath() {
        SendUnityMessage(MODEL_PATH_REQUEST, this.getModelPath());
    }
}
