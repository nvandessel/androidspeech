package com.mozilla.speechlibrary.stt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mozilla.speechlibrary.SpeechServiceSettings;
import com.mozilla.speechlibrary.utils.ModelUtils;

import org.json.JSONObject;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class STTLocalClient extends STTBaseClient implements Runnable {

    private static final String TAG = STTLocalClient.class.getSimpleName();

    private boolean mKeepClips = false;
    private DeepSpeechModel mModel;
    private DeepSpeechStreamingState mStreamingState;
    private FileChannel clipDebug;
    private Queue<short[]> mBuffers;
    private boolean mEndOfStream;

    private String mIntDec;

    public STTLocalClient(@NonNull Context context,
                   @NonNull SpeechServiceSettings settings,
                   @NonNull STTClientCallback callback) {
        super(context, settings, callback);
        Log.d(TAG, "STTLocalClient() called with: context = [" + context + "], settings = [" + settings + "], callback = [" + callback + "]");

        String modelRoot = settings.getModelPath();
        if (!ModelUtils.isReady(modelRoot)) {
            Log.w(TAG, "STTLocalClient: Model not Ready");
            mIsRunning = false;
            mEndOfStream = true;
            mCallback.onSTTError("STT Error: Model not ready");
            return;
        }
        Log.i(TAG, "STTLocalClient: Model is Ready");

        try {
            StringBuilder infoJsonContent = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(ModelUtils.getInfoJsonFolder(modelRoot)));
            String line;
            while ((line = br.readLine()) != null) {
                Log.d(TAG, "STTLocalClient: line=" + line);
                infoJsonContent.append(line);
            }
            br.close();
            Log.d(TAG, "STTLocalClient: infoJsonContent=" + infoJsonContent);

        } catch (Exception e) {
            mIsRunning = false;
            mEndOfStream = true;
            mCallback.onSTTError("STT Error");
            return;
        }

        int clipNumber = 0;
        clipNumber += 1;

        mKeepClips = (new File(modelRoot + "/.keepClips")).exists();
        boolean useDecoder = !(new File(modelRoot + "/.noUseDecoder")).exists();

        Log.d(TAG, "STTLocalClient: keepClips=" + mKeepClips);
        Log.d(TAG, "STTLocalClient: useDecoder=" + useDecoder);

        try {
            if (mModel == null) {
                Log.d(TAG, "STTLocalClient: new DeepSpeechModel(\"" + ModelUtils.getTFLiteFolder(modelRoot) + "\")");
                mModel = new DeepSpeechModel(ModelUtils.getTFLiteFolder(modelRoot));
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "STTLocalClient: Could not instantiate DeepSpeech Model", e);
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "STTLocalClient: this should never happen", e);
            throw e;
        }

        if (useDecoder) {
            Log.d(TAG, "STTLocalClient: using Decoder from <" + ModelUtils.getScorerFolder(modelRoot) + ">");
            mModel.enableExternalScorer(ModelUtils.getScorerFolder(modelRoot));
        }

        if (mKeepClips) {
            try {
                String clipFile = modelRoot + "/clip_" + clipNumber + ".wav";
                clipDebug = new FileOutputStream(clipFile).getChannel();
                Log.d(TAG, "STTLocalClient: keeping clip as  <" + clipFile + ">");
            } catch (Exception ignored) {
            }
        }

        mStreamingState = mModel.createStream();
        mIsRunning = true;
        mEndOfStream = false;

    }

    @Override
    public void encode(final short[] aBuffer, final int pos, final int len) {
        Log.v(TAG, "encode() called with: aBuffer = [" + aBuffer + "], pos = [" + pos + "], len = [" + len + "]");
        mBuffers.add(aBuffer);
    }

    @Override
    public void process() {
        Log.d(TAG, "process() called");
        mEndOfStream = true;
    }

    private void closeModel() {
        Log.d(TAG, "closeModel() called");
        if (mModel != null) {
            mModel.freeModel();
        }

        mStreamingState = null;
        mModel = null;
    }

    private void decode() {
        Log.d(TAG, "decode() called");
        mCallback.onSTTStart();

        String finalDecoded = mModel.finishStream(mStreamingState);

        STTResult sttResult = new STTResult(finalDecoded, (float)(1.0));
        Log.i(TAG, "decode: sttResult="+sttResult);
        mCallback.onSTTFinished(sttResult);

        closeModel();

        mIsRunning = false;
    }

    private void int_dec(){
        Log.d(TAG, "int_dec() called");
        String curDec = mModel.intermediateDecode(mStreamingState);
        if (curDec.equals(mIntDec)) { return; }

        mIntDec = curDec;
        STTResult sttResult = new STTResult(mIntDec, 1.0f);
        Log.i(TAG, "int_dec: sttResult="+sttResult);
        mCallback.onSTTIntDec(sttResult);
    }

    @Override
    public void run() {
        Log.d(TAG, "run() called");
        mBuffers = new ConcurrentLinkedQueue<>();

        while (!mEndOfStream || mBuffers.size() > 0) {
            short[] aBuffer = mBuffers.poll();

            if (aBuffer == null) {
                Log.v(TAG, "run: null aBuffer");
                continue;
            }
            Log.v(TAG, "run: feeding audio content to model");

            this.mModel.feedAudioContent(mStreamingState, aBuffer, aBuffer.length);

            Log.v(TAG, "run: intermediary decoding");
            int_dec();

            // DEBUG
            if (mKeepClips) {
                Log.i(TAG, "run: keeping clip: saving clip");
                ByteBuffer myByteBuffer = ByteBuffer.allocate(aBuffer.length * 2);
                myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
                myShortBuffer.put(aBuffer);

                try {
                    clipDebug.write(myByteBuffer);

                } catch (Exception ignored) {}
            }
        }

        Log.v(TAG, "run: decoding");
        decode();
    }
}
