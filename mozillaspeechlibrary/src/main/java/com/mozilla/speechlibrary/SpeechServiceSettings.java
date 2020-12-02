package com.mozilla.speechlibrary;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class SpeechServiceSettings implements Serializable {

    private final String TAG = "STTSpeechServiceSettings";

    private final boolean mUseStoreSamples;
    private final boolean mUseStoreTranscriptions;
    private final String mLanguage;
    private final String mProductTag;
    private final boolean mUseDeepSpeech;
    private final String mModelPath;

    public SpeechServiceSettings(@NonNull Builder builder) {
        mUseStoreSamples = builder.storeSamples;
        mUseStoreTranscriptions = builder.storeTranscriptions;
        mLanguage = builder.language;
        mProductTag = builder.productTag;
        mUseDeepSpeech = builder.useDeepSpeech;
        mModelPath = builder.modelPath;
    }

    public boolean useStoreSamples() {
        return mUseStoreSamples;
    }

    public boolean useStoreTranscriptions() {
        return mUseStoreTranscriptions;
    }

    @NonNull
    public String getLanguage() {
        return mLanguage;
    }

    @NonNull
    public String getProductTag() {
        return mProductTag;
    }

    public boolean useUseDeepSpeech() {
        return mUseDeepSpeech;
    }

    @Nullable
    public String getModelPath() {
        return mModelPath;
    }

    public static class Builder {
        private final String TAG = "STTSettingsBuilder";

        private boolean storeSamples;
        private boolean storeTranscriptions;
        private String language;
        private String productTag;
        private boolean useDeepSpeech;
        private String modelPath;

        public Builder() {
            Log.v(TAG, "Builder: init");
            storeSamples = false;
            storeTranscriptions = false;
            language = "en-US";
            productTag = "moz-android-speech-lib";
            useDeepSpeech = false;
            modelPath = null;
        }

        public Builder withStoreSamples(boolean storeSamples) {
            Log.v(TAG, "withStoreSamples() called with: storeSamples = [" + storeSamples + "]");
            this.storeSamples = storeSamples;
            return this;
        }

        public Builder withStoreTranscriptions(boolean storeTranscriptions) {
            Log.v(TAG, "withStoreTranscriptions() called with: storeTranscriptions = [" + storeTranscriptions + "]");
            this.storeTranscriptions = storeTranscriptions;
            return this;
        }

        public Builder withLanguage(@NonNull String language) {
            Log.v(TAG, "withLanguage() called with: language = [" + language + "]");
            this.language = language;
            return this;
        }

        public Builder withUseDeepSpeech(boolean useDeepSpeech){
            Log.v(TAG, "withUseDeepSpeech() called with: useDeepSpeech = [" + useDeepSpeech + "]");
            this.useDeepSpeech = useDeepSpeech;
            return this;
        }

        public Builder withModelPath(@NonNull String modelPath){
            Log.v(TAG, "withModelPath() called with: modelPath = [" + modelPath + "]");
            this.modelPath = modelPath;
            return this;
        }

        public Builder withProductTag(@NonNull String productTag){
            Log.v(TAG, "withProductTag() called with: productTag = [" + productTag + "]");
            this.productTag = productTag;
            return this;
        }

        public SpeechServiceSettings build(){
            return new SpeechServiceSettings(this);
        }
    }

    @Override
    public String toString() {
        return "SpeechServiceSettings{" +
                ", mUseStoreSamples=" + mUseStoreSamples +
                ", mUseStoreTranscriptions=" + mUseStoreTranscriptions +
                ", mLanguage='" + mLanguage + '\'' +
                ", mProductTag='" + mProductTag + '\'' +
                ", mUseDeepSpeech=" + mUseDeepSpeech +
                ", mModelPath='" + mModelPath + '\'' +
                '}';
    }
}
