package com.kosherdev.imagecapturetest.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.kosherdev.imagecapturetest.MainActivity;
import com.kosherdev.imagecapturetest.R;

import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class ClarifaiTask extends AsyncTask<String, Void, String> {
    private MainActivity mainActivity;
    private ProgressDialog dialog;

    public ClarifaiTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.dialog = new ProgressDialog(mainActivity);
    }

    @Override
    protected String doInBackground(String... params) {
        ClarifaiClient clarifai = new ClarifaiBuilder(
                mainActivity.getResources().getString(R.string.clarifai_api_client_id)
        ).buildSync();

        Model<Concept> generalModel = clarifai.getDefaultModels().generalModel();

        PredictRequest<Concept> request = generalModel.predict().withInputs(
                ClarifaiInput.forImage(mainActivity.getFile())
        );
        List<ClarifaiOutput<Concept>> results = request.executeSync().get();

        StringBuilder result = new StringBuilder();
        for (ClarifaiOutput<Concept> clarifaiOutput : results) {
            for (Concept concept : clarifaiOutput.data()) {
                result.append(concept.name()).append(" ");
            }
        }
        return result.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog.setTitle(R.string.ContactingServers);
        dialog.setMessage(mainActivity.getResources().getString(R.string.image_picker_task));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
//        if (mainActivity != null)
//            dialog.show();
    }

    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        mainActivity.setText(result);
    }
}
