package com.sandeep.textify;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscription;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConfig;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants;
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionListener;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;


import java.io.IOException;

public class MainMenu extends AppCompatActivity {
    private Context context;
    private Button capture,transcribe;
    private static final int request_camera_code = 100;
    private static final int request_write_storage_code = 101;
    private static final int request_read_storage_code = 102;
    private Bitmap bitmap;
    private String value,access_token,api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = this;

        access_token = getIntent().getExtras().getString("access_token");
        //Toast.makeText(getApplicationContext(),access_token,Toast.LENGTH_SHORT).show();
        api = "DAEDAJPPUt4q9Yvt9vB7czlFN1fWa+mbxZK/KuxMY7zg9gjQuwgx3bfk3kN95PdEHiWLuQm0eRIb4lmO5YNSDq9Za0at+fOijcAVFA==";


        capture = findViewById(R.id.capture);
        transcribe = findViewById(R.id.transcription);


        if(ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainMenu.this,new String[]{
                    Manifest.permission.CAMERA
            },request_camera_code);
        }

        if(ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainMenu.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },request_write_storage_code);
        }

        if(ContextCompat.checkSelfPermission(MainMenu.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainMenu.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },request_read_storage_code);
        }
        transcribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, TranscriptionActivity.class);
                intent.putExtra("access_token",access_token);
                startActivity(intent);
            }
        });
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(MainMenu.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri =  data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                recognize(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private void recognize(Bitmap bitmap){
        final String[] val = new String[1];
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create();
        MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
        MLFrame frame = MLFrame.fromBitmap(bitmap);

        Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLText>() {
            @Override
            public void onSuccess(MLText text) {
                // Processing for successful recognition.
                value = text.getStringValue();
                Intent intent = new Intent(MainMenu.this,ResultActivity.class);
                intent.putExtra("result",value);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainMenu.this, "Couldn't recognise text", Toast.LENGTH_SHORT).show();
                // Processing logic for recognition failure.
            }
        });

        try {
            if (analyzer != null) {
                analyzer.stop();
            }
        } catch (IOException e) {
        }

    }
}