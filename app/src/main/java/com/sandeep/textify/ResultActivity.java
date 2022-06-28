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

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "destroyed";
    private Button save;
    private TextView result;
    private String value;

    private BannerView bannerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        bannerView = findViewById(R.id.hw_banner_view);
        bannerView.setAdId("testw6vs28auh3");
        bannerView.setBannerRefresh(60);
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);
        bannerView.setAdListener(adListener);



        save = findViewById(R.id.save);
        result = findViewById(R.id.result);

        value = getIntent().getExtras().getString("result");

        result.setText(value);
        result.setMovementMethod(new ScrollingMovementMethod());

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