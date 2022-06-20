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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class ResultActivity extends AppCompatActivity {
    private Button save;
    private TextView result;
    private String value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

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

}