package com.mozilla.speechlibrary;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.mozilla.speechlibrary.stt.STTResult;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class SpeechResultReceiver extends ResultReceiver {

    private static final String TAG = "STTResultReceiver";
    public static final String ERROR_TYPE = "errorType";
    public static final String PARAM_RESULT = "result";
    public static final String PARAM_FFT_SUM = "fftsum";

    private ArrayList<SpeechResultCallback> mReceivers;

    public SpeechResultReceiver(Handler handler) {
        super(handler);
        Log.d(TAG, "SpeechResultReceiver: init");
        
        mReceivers = new ArrayList<>();
    }

    public void addReceiver(@NonNull SpeechResultCallback receiver) {
        Log.d(TAG, "addReceiver: ");
        mReceivers.add(receiver);
    }

    public void removeReceiver(@NonNull SpeechResultCallback receiver) {
        Log.d(TAG, "removeReceiver: ");
        mReceivers.remove(receiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult() called with: resultCode = [" + resultCode + "], resultData = [" + resultData + "]");
        try {
            mReceivers.forEach(receiver -> {
                switch (SpeechState.values()[resultCode]) {
                    case DECODING:
                        receiver.onDecoding();
                        break;
                    case MIC_ACTIVITY:
                        receiver.onMicActivity(resultData.getDouble(PARAM_FFT_SUM));
                        break;
                    case STT_RESULT:
                        receiver.onSTTResult((STTResult) resultData.getSerializable(PARAM_RESULT));
                        break;
                    case START_LISTEN:
                        receiver.onStartListen();
                        break;
                    case NO_VOICE:
                        receiver.onNoVoice();
                        break;
                    case ERROR:
                        receiver.onError(resultData.getInt(ERROR_TYPE), resultData.getString(PARAM_RESULT));
                        break;
                    case INT_DEC:
                        receiver.onIntDecResult((STTResult)resultData.getSerializable(PARAM_RESULT));
                        break;
                }
            });

        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

}
