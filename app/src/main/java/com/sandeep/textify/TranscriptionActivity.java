package com.sandeep.textify;

import static com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants.LAN_EN_US;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.framework.common.ContextCompat;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscription;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConfig;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionListener;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionResult;

import java.util.ArrayList;
import java.util.List;

public class TranscriptionActivity extends AppCompatActivity {
    private static final String TAG = TranscriptionActivity.class.getSimpleName();
    private TextView resultTv;// = findViewById(R.id.result_tv);
    private TextView errorTv;// = findViewById(R.id.error_tv);
    //private String languageItem = LAN_EN_US;
    private Button start, stop, query;
    private Context context;

    private volatile StringBuffer recognizerResult = new StringBuffer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcription);
        context = this;
        resultTv = findViewById(R.id.result_tv);
        errorTv = findViewById(R.id.error_tv);
        //initView();
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        String api = "DAEDAJPPUt4q9Yvt9vB7czlFN1fWa+mbxZK/KuxMY7zg9gjQuwgx3bfk3kN95PdEHiWLuQm0eRIb4lmO5YNSDq9Za0at+fOijcAVFA==";
        String access_token = getIntent().getExtras().getString("access_token");

        MLApplication.getInstance().setAccessToken(access_token);
        MLApplication.getInstance().setApiKey(api);

        start = findViewById(R.id.start_listen_btn);
        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                start();
            }
        });

        stop = findViewById(R.id.stop_listen_btn);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destroyRecognizer();
            }
        });

        query = findViewById(R.id.query_languages_btn);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MLSpeechRealTimeTranscription.getInstance()
                        .getLanguages(new MLSpeechRealTimeTranscription.LanguageCallback() {
                            @Override
                            public void onResult(List<String> result) {
                                Toast.makeText(TranscriptionActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "support languages==" + result.toString());
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                Log.e(TAG, "errorCode:" + errorCode + "errorMsg:" + errorMsg);
                            }

                        });
            }
        });
    }
    /*private void initView() {
        findViewById(R.id.start_listen_btn).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.stop_listen_btn).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.query_languages_btn).setOnClickListener((View.OnClickListener) this);
        resultTv = findViewById(R.id.result_tv);
        errorTv = findViewById(R.id.error_tv);
    }*/

    protected class SpeechRecognitionListener implements MLSpeechRealTimeTranscriptionListener {

        @Override
        public void onStartListening() {
            Log.d(TAG, "onStartListening");
        }

        @Override
        public void onStartingOfSpeech() {
            Log.d(TAG, "onStartingOfSpeech");
        }

        @Override
        public void onVoiceDataReceived(byte[] data, float energy, Bundle bundle) {
            int length = data == null ? 0 : data.length;
            Log.d(TAG, "onVoiceDataReceived data.length=" + length);
        }

        @Override
        public void onRecognizingResults(Bundle partialResults) {
            if (partialResults == null) {
                return;
            }
            boolean isFinal = partialResults.getBoolean(MLSpeechRealTimeTranscriptionConstants.RESULTS_PARTIALFINAL);
            String result = partialResults.getString(MLSpeechRealTimeTranscriptionConstants.RESULTS_RECOGNIZING);
            if (TextUtils.isEmpty(result)) {
                return;
            }
            if (isFinal) {

                ArrayList<MLSpeechRealTimeTranscriptionResult> wordOffset = partialResults.getParcelableArrayList(MLSpeechRealTimeTranscriptionConstants.RESULTS_WORD_OFFSET);
                ArrayList<MLSpeechRealTimeTranscriptionResult> sentenceOffset = partialResults.getParcelableArrayList(MLSpeechRealTimeTranscriptionConstants.RESULTS_SENTENCE_OFFSET);

                if (wordOffset != null) {
                    for (int i = 0; i < wordOffset.size(); i++) {
                        MLSpeechRealTimeTranscriptionResult remoteResult = wordOffset.get(i);
                        Log.d(TAG, "onRecognizingResults word offset is " + i + " ---> " + remoteResult.toString());
                    }
                }

                if (sentenceOffset != null) {
                    for (int i = 0; i < sentenceOffset.size(); i++) {
                        MLSpeechRealTimeTranscriptionResult remoteResult = sentenceOffset.get(i);
                        Log.d(TAG, "onRecognizingResults sentence offset is " + i + " ---> " + remoteResult.toString());
                    }
                }
                recognizerResult.append(result);
                errorTv.setText(recognizerResult.toString());

            } else {
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                resultTv.setText(result);
            }
        }

        @Override
        public void onError(int error, String errorMessage) {
            String tip = getPrompt(error);
            if (!TextUtils.isEmpty(tip)) {
                Log.d(TAG, "onError: " + error + " update: " + tip);
                tip = "ERROR: " + tip;
                errorTv.setVisibility(View.VISIBLE);
                errorTv.setText(tip);
                resultTv.setText("");
            }
        }

        @Override
        public void onState(int state, Bundle params) {
            if (state == MLSpeechRealTimeTranscriptionConstants.STATE_SERVICE_RECONNECTING) { // webSocket Reconnecting
                Log.d(TAG, "onState webSocket reconnect ");
            } else if (state == MLSpeechRealTimeTranscriptionConstants.STATE_SERVICE_RECONNECTED) { // webSocket Reconnection succeeded.
                Log.d(TAG, "onState webSocket reconnect success ");
            } else if (state == MLSpeechRealTimeTranscriptionConstants.STATE_LISTENING) { // The recorder is ready.
                Log.d(TAG, "onState recorder is ready ");
            } else if (state == MLSpeechRealTimeTranscriptionConstants.STATE_NO_UNDERSTAND) { // Failed to detect the current frame.
                Log.d(TAG, "onState Failed to detect the current frame ");
            } else if (state == MLSpeechRealTimeTranscriptionConstants.STATE_NO_NETWORK) { // No network is available in the current environment.
                Log.d(TAG, "onState No network ");
            }
        }
    }

    private String getPrompt(int errorCode) {
        Log.d(TAG, "ErrorCode： " + errorCode + " Other errors");
        String error_Text;
        switch (errorCode) {
            case MLSpeechRealTimeTranscriptionConstants.ERR_NO_NETWORK:
                error_Text = "The network is unavailable.，……Please try again.";
                break;
            case MLSpeechRealTimeTranscriptionConstants.ERR_SERVICE_UNAVAILABLE:
                error_Text = "The service is unavailable.";
                break;
            case MLSpeechRealTimeTranscriptionConstants.ERR_INVALIDE_TOKEN:
                error_Text = "The unavailable is token.";
                break;
            case MLSpeechRealTimeTranscriptionConstants.ERR_SERVICE_CREDIT:
                error_Text = "Insufficient balance. Please recharge.";
                break;

            default:
                error_Text = "ErrorCode： " + errorCode + " Other errors";
                break;
        }
        return error_Text;
    }

    private void start() {
        MLSpeechRealTimeTranscriptionConfig config = new MLSpeechRealTimeTranscriptionConfig.Factory()
                // Set the language that can be recognized to English. If this parameter is not set,
                // English is recognized by default.
                // Example: MLSpeechRealTimeTranscriptionConstants.LAN_ZH_CN: Chinese,
                // MLSpeechRealTimeTranscriptionConstants.LAN_EN_US: English,
                // MLSpeechRealTimeTranscriptionConstants.LAN_FR_FR: French.
                .setLanguage(LAN_EN_US)
                // set punctuation support.
                .enablePunctuation(true)
                // set sentence time offset.
                .enableSentenceTimeOffset(true)
                // set word time offset.
                .enableWordTimeOffset(true)
                // Set the usage scenario to shopping,Currently, only Chinese scenarios are supported.
                //.setScenes(MLSpeechRealTimeTranscriptionConstants.SCENES_SHOPPING)
                .create();
        MLSpeechRealTimeTranscription.getInstance().startRecognizing(config);

        MLSpeechRealTimeTranscription.getInstance().setRealTimeTranscriptionListener(new SpeechRecognitionListener());

        Log.d(TAG, "language  " + config.getLanguage());
        Log.d(TAG, "isPunctuationEnable  " + config.isPunctuationEnable());
        Log.d(TAG, "isWordTimeOffsetEnable  " + config.isWordTimeOffsetEnable());
        Log.d(TAG, "isSentenceTimeOffsetEnable  " + config.isSentenceTimeOffsetEnable());
    }

   /* public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_listen_btn:
                start();
                break;
            case R.id.stop_listen_btn:
                destroyRecognizer();
                break;
            case R.id.query_languages_btn:
                MLSpeechRealTimeTranscription.getInstance()
                        .getLanguages(new MLSpeechRealTimeTranscription.LanguageCallback() {
                            @Override
                            public void onResult(List<String> result) {
                                Toast.makeText(TranscriptionActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "support languages==" + result.toString());
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                Log.e(TAG, "errorCode:" + errorCode + "errorMsg:" + errorMsg);
                            }

                        });
                break;
            default:
                break;
        }
    } */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyRecognizer();
    }

    public void destroyRecognizer() {
        MLSpeechRealTimeTranscription.getInstance().destroy();
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == (PackageManager.PERMISSION_GRANTED==0)?true:false) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return new String[0];
        }
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), 1);
        }
    }

}