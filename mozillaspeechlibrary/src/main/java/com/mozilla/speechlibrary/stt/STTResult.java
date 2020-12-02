package com.mozilla.speechlibrary.stt;

import android.util.Log;

import java.io.Serializable;

public class STTResult implements Serializable {
    private static final String TAG = "STTResult";
    public String mTranscription;
    public float mConfidence;

    STTResult(String aTranscription, float aConfidence) {
        Log.i(TAG, "STTResult() called with: aTranscription = [" + aTranscription + "], aConfidence = [" + aConfidence + "]");
        this.mTranscription = aTranscription;
        this.mConfidence = aConfidence;
    }

    @Override
    public String toString() {
        return "STTResult{" +
                "mTranscription='" + mTranscription + '\'' +
                ", mConfidence=" + mConfidence +
                '}';
    }
}