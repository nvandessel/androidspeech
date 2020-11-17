package com.mozilla.speechlibrary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mozilla.speechlibrary.recognition.LocalSpeechRecognition;
import com.mozilla.speechlibrary.stt.STTResult;
import com.unity3d.player.UnityPlayer;

public class UnitySTTPlugin implements SpeechResultCallback  {

    public static UnitySTTPlugin getInstance() {return s_instance; }
    private static final UnitySTTPlugin s_instance = new UnitySTTPlugin();

    protected static final String TAG = "UnitySTTPlugin";
    private static final String GAME_OBJECT_NAME = "DeepSpeech";
    private static final String SPEECH_STATUS_CHANGED = "onSpeechStatusChanged";
    private static final String MODEL_PATH_REQUEST = "onModelPathRequest";

    private String _modelPath;
    private LocalSpeechRecognition _localSpeechRecognition;

    @NonNull
    private Context _context;

    private UnitySTTPlugin(){
        Log.d(TAG, "UnitySTTPlugin has been created");
        _modelPath = Environment.getExternalStorageDirectory() + "/deepspeech";
    }

    public void Initialize(@NonNull Context context){
        _context = context;
        _localSpeechRecognition = new LocalSpeechRecognition(_context);
        SpeechServiceSettings.Builder builder = new SpeechServiceSettings.Builder()
                .withUseDeepSpeech(true)
                .withModelPath(_modelPath);
        _localSpeechRecognition.start(builder.build(), this.getInstance());
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
        SendUnityMessage(SPEECH_STATUS_CHANGED, String.format("Success: %s\n", result.mTranscription));
    }

    @Override
    public void onNoVoice() {
        SendUnityMessage(SPEECH_STATUS_CHANGED, "No Voice detected\n");
    }

    @Override
    public void onError(int errorType, @Nullable String error) {
        SendUnityMessage(SPEECH_STATUS_CHANGED, String.format("Error:", error, "\n"));
    }

    //Unity Implementation

    protected void SendUnityMessage(String methodName, String message){

        UnityPlayer.UnitySendMessage(GAME_OBJECT_NAME, methodName, message);
    }

    public String getModelPath() { return this._modelPath; }

    public void publishModelPath(){
        SendUnityMessage(MODEL_PATH_REQUEST, this.getModelPath());
    }
}
