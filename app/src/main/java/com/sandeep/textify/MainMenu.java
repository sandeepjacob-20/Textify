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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class MainMenu extends AppCompatActivity {
    private Context context;
    private ImageButton capture,transcribe;
    private TextView name_field,greeting_field;
    private static final int request_camera_code = 100;
    private static final int request_write_storage_code = 101;
    private static final int request_read_storage_code = 102;
    private Bitmap bitmap;
    private String value,name,greeting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = this;

        greeting_field = findViewById(R.id.greeting);
        name_field = findViewById(R.id.name_layout);
        capture = findViewById(R.id.capture);
        transcribe = findViewById(R.id.transcription);

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if(hour>= 12 && hour < 17){
            greeting = "Good Afternoon";
        } else if(hour >= 17 && hour < 21){
            greeting = "Good Evening";
        } else if(hour >= 21 && hour < 24){
            greeting = "Good Night";
        } else {
            greeting = "Good Morning";
        }

        name = getIntent().getExtras().getString("name");
        name_field.setText(name);

        greeting_field.setText(greeting);

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
                overridePendingTransition(R.anim.slide_to_left,R.anim.slide_from_right);
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
                overridePendingTransition(R.anim.slide_to_left,R.anim.slide_from_right);
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