package com.kosherdev.imagecapturetest.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.kosherdev.imagecapturetest.MainActivity;
import com.kosherdev.imagecapturetest.R;

import java.util.List;

public class ClarifaiTask extends AsyncTask<String, Void, String> {
    private MainActivity mainActivity;
    private ProgressDialog dialog;

    public ClarifaiTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.dialog = new ProgressDialog(mainActivity);
    }

    @Override
    protected String doInBackground(String... params) {
        ClarifaiClient clarifai = new ClarifaiClient(
                mainActivity.getResources().getString(R.string.clarifai_api_client_id),
                mainActivity.getResources().getString(R.string.clarifai_api_client_secret)
        );
        List<RecognitionResult> results =
                clarifai.recognize(new RecognitionRequest(mainActivity.getCameraFile()));

        String result = "";
        for (Tag tag : results.get(0).getTags()) {
            result += tag.getName() + " ";
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog.setTitle(R.string.ContactingServers);
        dialog.setMessage(mainActivity.getResources().getString(R.string.image_picker_task));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        if (mainActivity != null)
            dialog.show();
    }

    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        mainActivity.setText(result);
    }
}
