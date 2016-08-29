package com.kosherdev.imagecapturetest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kosherdev.imagecapturetest.helpers.Helpers;
import com.kosherdev.imagecapturetest.tasks.CamFindTask;
import com.kosherdev.imagecapturetest.tasks.ClarifaiTask;
import com.kosherdev.imagecapturetest.tasks.CloudVisionTask;
import com.kosherdev.imagecapturetest.utils.PermissionUtils;

import java.io.File;

public class MainActivity extends Activity {
    Button cloudVisionButton = null;
    Button camFindButton = null;
    Button clarifaiButton = null;
    TextView textView = null;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cloudVisionButton = (Button)findViewById(R.id.cloudVisionButton);
        camFindButton = (Button)findViewById(R.id.camFindButton);
        clarifaiButton = (Button)findViewById(R.id.clarifaiButton);
        textView = (TextView) findViewById(R.id.textView);

        cloudVisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                startCamera(Helpers.CLOUD_VISION_REQUEST);
            }
        });

        camFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                startCamera(Helpers.CAMFIND_REQUEST);
            }
        });

        clarifaiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                startCamera(Helpers.CLARIFAI_REQUEST);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void
    onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Helpers.GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && intent != null) {
            uploadImage(intent.getData());
        } else if (requestCode == Helpers.CLOUD_VISION_REQUEST && resultCode == RESULT_OK) {
            uploadImage(Uri.fromFile(getCameraFile()));
        }

        if (requestCode == Helpers.CAMFIND_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                new CamFindTask(this).execute();

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }

        if (requestCode == Helpers.CLARIFAI_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                new ClarifaiTask(this).execute();

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void startCamera(int request) {
        if (PermissionUtils.requestPermission(this, Helpers.CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, request);
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, Helpers.FILE_NAME);
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
    }

    public void uploadImage(Uri uri) {
        new CloudVisionTask(this, uri).execute();
    }
}