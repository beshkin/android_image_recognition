package com.kosherdev.imagecapturetest.tasks;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.kosherdev.imagecapturetest.MainActivity;
import com.kosherdev.imagecapturetest.R;
import com.kosherdev.imagecapturetest.helpers.Helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CloudVisionTask extends AsyncTask<Object, Void, String> {
    private Uri uri;
    private MainActivity mainActivity;
    private ProgressDialog dialog;

    public CloudVisionTask(MainActivity mainActivity, Uri uri) {
        this.uri = uri;
        this.mainActivity = mainActivity;
        this.dialog = new ProgressDialog(mainActivity);
    }

    @Override
    protected String doInBackground(Object... params) {
        Bitmap bitmap = null;
        if (uri != null) {

            try {
                // scale the image to 800px to save on bandwidth
                bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(mainActivity.getContentResolver(), uri), 1200);

            } catch (IOException e) {
                Log.d(Helpers.TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(mainActivity, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(Helpers.TAG, "Image picker gave us a null image.");
            Toast.makeText(mainActivity, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
        if (bitmap != null) {
            try {
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(new
                        VisionRequestInitializer(mainActivity.getResources().getString(R.string.cloud_vision_api_key)));
                Vision vision = builder.build();

                BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                        new BatchAnnotateImagesRequest();
                final Bitmap finalBitmap = bitmap;
                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                    // Add the image
                    Image base64EncodedImage = new Image();
                    // Convert the bitmap to a JPEG
                    // Just in case it's a format that Android understands but Cloud Vision
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Base64 encode the JPEG
                    base64EncodedImage.encodeContent(imageBytes);
                    annotateImageRequest.setImage(base64EncodedImage);

                    // add the features we want
                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                        Feature logoDetection = new Feature();
                        logoDetection.setType("LOGO_DETECTION");
                        logoDetection.setMaxResults(10);
                        add(logoDetection);
                        Feature textDetection = new Feature();
                        textDetection.setType("TEXT_DETECTION");
                        textDetection.setMaxResults(10);
                        add(textDetection);
                        Feature labelDetection = new Feature();
                        labelDetection.setType("LABEL_DETECTION");
                        labelDetection.setMaxResults(10);
                        add(labelDetection);
                    }});

                    // Add the list of one thing to the request
                    add(annotateImageRequest);
                }});

                Vision.Images.Annotate annotateRequest =
                        vision.images().annotate(batchAnnotateImagesRequest);
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                annotateRequest.setDisableGZipContent(true);
                Log.d(Helpers.TAG, "created Cloud Vision request object, sending request");

                BatchAnnotateImagesResponse response = annotateRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(Helpers.TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(Helpers.TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
        }
        return "Cloud Vision API request failed. Check logs for details.";
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

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        if (logos != null) {
            message += "logo: ";
            for (EntityAnnotation text : logos) {
                Log.d(Helpers.TAG, text.toString());
                message += text.getDescription().replaceAll("\\n", " ");
            }
            message += "\n";
        }

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            message += "text: ";
            for (EntityAnnotation text : texts) {
                Log.d(Helpers.TAG, text.toString());
                message += text.getDescription().replaceAll("\\n", " ");
            }
            message += "\n";
        }
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            message += "label: ";
            for (EntityAnnotation label : labels) {
                Log.d(Helpers.TAG, label.toString());
                if (label.getScore() >= 0.7) {
                    message += label.getDescription() + " ";
                }
            }
            message += "\n";
        }

        return message;
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}
