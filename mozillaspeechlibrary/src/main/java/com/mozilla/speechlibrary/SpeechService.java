package com.mozilla.speechlibrary;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mozilla.speechlibrary.recognition.LocalSpeechRecognition;
import com.mozilla.speechlibrary.recognition.NetworkSpeechRecognition;
import com.mozilla.speechlibrary.recognition.SpeechRecognition;

import org.mozilla.geckoview.GeckoWebExecutor;

import java.util.concurrent.Executors;

public class SpeechService {
    private final String TAG="STTSpeechService\uD83D\uDC7D";
    private Context mContext;
    private SpeechRecognition mSpeechRecognition;

    public SpeechService(@NonNull Context context) {
        mContext = context;
    }

    synchronized
    public void start(@NonNull SpeechServiceSettings settings, @NonNull SpeechResultCallback delegate) {
        Log.d(TAG, "start() called with: settings = [" + settings + "], delegate = [" + delegate + "]");
        start(settings, null, delegate);
    }

    synchronized
    public void start(@NonNull SpeechServiceSettings settings, @Nullable GeckoWebExecutor executor, @NonNull SpeechResultCallback delegate) {
        Log.d(TAG, "start() called with: settings = [" + settings + "], executor = [" + executor + "], delegate = [" + delegate + "]");
        if (mSpeechRecognition != null && mSpeechRecognition.isRunning()) {
            Log.d(TAG, "start: stopping currently running speech recognition");
            mSpeechRecognition.stop();
        }

        if (settings.useUseDeepSpeech()) {
            Log.d(TAG, "start: use deep speech");
            mSpeechRecognition = new LocalSpeechRecognition(
                    mContext);

        } else {
            Log.d(TAG, "start: Do not use deep speech");
            mSpeechRecognition = new NetworkSpeechRecognition(
                    mContext,
                    executor
            );
        }

        Log.d(TAG, "start: start speech recognition");
        execute(() -> mSpeechRecognition.start(settings, delegate));
    }

    public void stop() {
        Log.d(TAG, "stop");
        if (mSpeechRecognition != null) {
            Log.d(TAG, "stop: stopping speech recognition");
            mSpeechRecognition.stop();
        }
    }

    private void execute(@NonNull final Runnable task) {
        Log.d(TAG, "execute() called with: task = [" + task + "]");
        Executors.newSingleThreadExecutor().submit(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            task.run();
        });
    }

}
