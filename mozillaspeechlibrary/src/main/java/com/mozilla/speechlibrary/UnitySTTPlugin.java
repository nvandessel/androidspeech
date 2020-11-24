package com.mozilla.speechlibrary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mozilla.speechlibrary.stt.STTResult;
import com.mozilla.speechlibrary.utils.ModelUtils;
import com.unity3d.player.UnityPlayer;

public class UnitySTTPlugin implements SpeechResultCallback  {

    public static UnitySTTPlugin getInstance() {return s_instance; }
    private static final UnitySTTPlugin s_instance = new UnitySTTPlugin();

    protected static final String TAG = "UnitySTTPlugin";
    private static final String GAME_OBJECT_NAME = "DeepSpeech";
    private static final String SPEECH_STATUS_CHANGED = "onSpeechStatusChanged";
    private static final String MODEL_PATH_REQUEST = "onModelPathRequest";

    private String mModelPath;
    private SpeechService mSpeechService;

    private UnitySTTPlugin(){
        Log.d(TAG, "UnitySTTPlugin has been created");
        mModelPath = Environment.getExternalStorageDirectory() + "/deepspeech";
    }

    public void initialize(@NonNull Context context){
        if (!ModelUtils.isReady(mModelPath)){
            Log.e(TAG, "ModelPath is not ready! Cannot Initialize STT Service");
            return;
        }

        if (mSpeechService == null) { mSpeechService = new SpeechService(context); }
        SpeechServiceSettings.Builder builder = new SpeechServiceSettings.Builder()
                .withUseDeepSpeech(true)
                .withModelPath(mModelPath);
        mSpeechService.start(builder.build(), this);
    }

    public void stopSTT(){
        mSpeechService.stop();
    }

    // SpeechResultCallback

    @Override
    public void onStartListen() {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Started to listen\n");
    }

    @Override
    public void onMicActivity(double fftsum) {
    }

    @Override
    public void onDecoding() {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Decoding... \n");
    }

    @Override
    public void onSTTResult(@Nullable STTResult result) {
        SendUnityMessage(SPEECH_STATUS_CHANGED, String.format("Success: %s\n", result.mTranscription + "\n"));
    }

    @Override
    public void onIntDecResult(@Nullable STTResult result) {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Int:" + result.mTranscription + "\n");
    }

    @Override
    public void onNoVoice() {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "No Voice detected\n");
    }

    @Override
    public void onError(int errorType, @Nullable String error) {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "Error:" + error);
    }

    //Unity Implementation

    protected void SendUnityMessage(String methodName, String message){

        UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, methodName, message);
    }

    public String getModelPath() { return this.mModelPath; }

    public void publishModelPath(){
        SendUnityMessage(MODEL_PATH_REQUEST, this.getModelPath());
    }
}
