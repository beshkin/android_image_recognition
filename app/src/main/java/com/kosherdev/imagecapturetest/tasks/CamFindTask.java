package com.kosherdev.imagecapturetest.tasks;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kosherdev.imagecapturetest.MainActivity;
import com.kosherdev.imagecapturetest.R;
import com.kosherdev.imagecapturetest.helpers.Helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CamFindTask extends AsyncTask<String, Void, String> {
    private ProgressDialog dialog;
    private MainActivity mainActivity;

    public CamFindTask(MainActivity myActivity) {
        this.mainActivity = myActivity;
        this.dialog = new ProgressDialog(myActivity);
    }

    protected void onPreExecute() {

        dialog.setTitle(R.string.ContactingServers);
        dialog.setMessage(mainActivity.getResources().getString(R.string.image_picker_task));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        if (mainActivity != null)
            dialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        String response = "";
        GetCamFind getCamFind = new GetCamFind(mainActivity);
        try {
            File file = mainActivity.getFile();
            Bitmap bitmap = Helpers.decodeFile(file);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            getCamFind.sendCamRequest(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 20; i++) {
            String result = getCamFind.sendCamResult();
            Log.d("status", i + " " + result);
            if (!result.equals("")) {
                JsonObject obj = new JsonParser().parse(result).getAsJsonObject();

                String status = obj.get("status").toString();

                if (status.replace("\"", "").equals("completed")) {
                    response = obj.get("name").toString();
                    break;
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        result = result.replace("\"", "").trim();
        if (result.matches(".*(logo|label)$")) {
            result = result.replaceAll("(.*)(logo|label)$", "$1").trim();
        }

        if (!result.equals("")) {

            mainActivity.setText(result);
        }
    }
}
