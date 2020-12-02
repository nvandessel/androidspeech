package com.mozilla.speechlibrary.recognition;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mozilla.speechlibrary.SpeechResultCallback;
import com.mozilla.speechlibrary.SpeechServiceSettings;
import com.mozilla.speechlibrary.stt.STTLocalClient;
import com.mozilla.speechlibrary.stt.STTResult;

public class LocalSpeechRecognition extends SpeechRecognition {
    private static final String TAG = "STTLocalSpeechRecognition";
    public LocalSpeechRecognition(@NonNull Context context) {
        super(context);
    }

    @Override
    public void start(@NonNull SpeechServiceSettings settings,
                      @NonNull SpeechResultCallback callback) {
        Log.d(TAG, "start() called with: settings = [" + settings + "], callback = [" + callback + "]");
        mStt = new STTLocalClient(mContext, settings, this);
        Thread sttThread = new Thread((STTLocalClient)mStt, "STT Thread");
        sttThread.start();
        super.start(settings, callback);
    }
}
