package com.kosherdev.imagecapturetest;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kosherdev.imagecapturetest.helpers.Helpers;
import com.kosherdev.imagecapturetest.tasks.CamFindTask;
import com.kosherdev.imagecapturetest.tasks.ClarifaiTask;
import com.kosherdev.imagecapturetest.tasks.CloudVisionTask;
import com.kosherdev.imagecapturetest.utils.PermissionUtils;

import java.io.File;

public class MainActivity extends Activity {
    TextView textView = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        textView = findViewById(R.id.textView);
    }

    @Override
    public void
    onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Helpers.GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && intent != null) {
            uploadImage(intent.getData());
        } else if (requestCode == Helpers.CLOUD_VISION_REQUEST && resultCode == RESULT_OK) {
            uploadImage(getCameraFile());
        }

        if (requestCode == Helpers.CAMFIND_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                new CamFindTask(this).execute();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.SomethingWrong, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == Helpers.CLARIFAI_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                new ClarifaiTask(this).execute();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.SomethingWrong, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startCamera(int request) {
        if (PermissionUtils.requestPermission(this, Helpers.CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Uri photoURI = getCameraFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                intent.setClipData(ClipData.newRawUri("", photoURI));
            }
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(intent, request);
        }
    }

    public Uri getCameraFile() {

        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", getFile());
        grantUriPermission(context.getPackageName(), fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return fileUri;
    }

    public File getFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "pics_local");
        dir.mkdirs();

        return new File(dir, Helpers.FILE_NAME);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void uploadImage(Uri uri) {
        new CloudVisionTask(this, uri).execute();
    }

    public void OnButtonClick(View view) {
        textView.setText("");
        switch (view.getId()) {
            case R.id.cloudVisionButton:
                startCamera(Helpers.CLOUD_VISION_REQUEST);
                break;
            case R.id.camFindButton:
                startCamera(Helpers.CAMFIND_REQUEST);
                break;
            case R.id.clarifaiButton:
                startCamera(Helpers.CLARIFAI_REQUEST);
                break;
        }
    }
}
