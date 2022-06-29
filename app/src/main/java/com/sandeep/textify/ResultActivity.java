package com.sandeep.textify;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.translate.MLTranslateLanguage;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "destroyed";
    private Button save,translate;
    private TextView result_window;
    private String value;
    private String api = "DAEDAJPPUt4q9Yvt9vB7czlFN1fWa+mbxZK/KuxMY7zg9gjQuwgx3bfk3kN95PdEHiWLuQm0eRIb4lmO5YNSDq9Za0at+fOijcAVFA==";
    private BannerView bannerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        value = getIntent().getExtras().getString("result");
        save = findViewById(R.id.save);
        translate = findViewById(R.id.translate);
        result_window = findViewById(R.id.result);

        result_window.setText(value);
        result_window.setMovementMethod(new ScrollingMovementMethod());

        bannerView = findViewById(R.id.hw_banner_view);
        bannerView.setAdId("testw6vs28auh3");
        bannerView.setBannerRefresh(60);
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);
        bannerView.setAdListener(adListener);

        MLApplication.getInstance().setApiKey(api);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MLLocalTranslateSetting setting = new MLLocalTranslateSetting.Factory()
                        // Set the source language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                        .setSourceLangCode("en")
                        // Set the target language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                        .setTargetLangCode("fr")
                        .create();

                final MLLocalTranslator mlLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting);
                MLTranslateLanguage.getLocalAllLanguages().addOnSuccessListener(
                        new OnSuccessListener<Set<String>>() {
                            @Override
                            public void onSuccess(Set<String> result) {
                                Log.d("success","local language received");
                                // Languages supported by on-device translation are successfully obtained.
                            }
                        });

                MLModelDownloadStrategy downloadStrategy = new MLModelDownloadStrategy.Factory()
                        //.needWifi() // It is recommended that you download the package in a Wi-Fi environment.
                        .create();

                MLModelDownloadListener modelDownloadListener = new MLModelDownloadListener() {
                    @Override
                    public void onProcess(long alreadyDownLength, long totalLength) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display the download progress or perform other operations.
                            }
                        });
                    }
                };

                mlLocalTranslator.preparedModel(downloadStrategy, modelDownloadListener).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess (Void aVoid){
                                Log.d("Model Download ","Model package Downloaded");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure (Exception e){
                        Log.d("Model Download ","Error downloading Model package");
                        // Called when the model package fails to be downloaded.
                    }
                });

                final Task<String> task = mlLocalTranslator.asyncTranslate(value);
                task.addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        result_window.setText(s);
                        value = s;
                        // Processing logic for detection success.
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Processing logic for detection failure.
                    }
                });

                if (mlLocalTranslator!= null) {
                       mlLocalTranslator.stop();
                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndSaveFile();
            }
        });
    }

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            // Called when an ad is loaded successfully.
            Toast.makeText(getApplicationContext(),"Ad loaded.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailed(int errorCode) {
            // Called when an ad fails to be loaded.
            Toast.makeText(getApplicationContext(),String.format(Locale.ROOT, "Ad failed to load with error code %d.",errorCode),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdOpened() {
            // Called when an ad is opened.
            Toast.makeText(getApplicationContext(),String.format("Ad opened "),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdClicked() {
            // Called when a user taps an ad.
            Toast.makeText(getApplicationContext(),"Ad clicked",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdLeave() {
            // Called when a user has left the app.
            Toast.makeText(getApplicationContext(),"Ad Leave",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdClosed() {
            // Called when an ad is closed.
            Toast.makeText(getApplicationContext(),"Ad closed",Toast.LENGTH_SHORT).show();
        }
    };


    private void createAndSaveFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE,"new_doc.txt");
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();

                        try {
                            OutputStream outputStream = getContentResolver().openOutputStream(uri);
                            outputStream.write(value.getBytes());
                            outputStream.close();

                            Toast.makeText(getApplicationContext(),"File Saved",Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else{
                        Toast.makeText(getApplicationContext(),"File Not Saved",Toast.LENGTH_LONG).show();
                    }
                }
            });
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (bannerView != null) {
            bannerView.destroy();
        }
    }

}