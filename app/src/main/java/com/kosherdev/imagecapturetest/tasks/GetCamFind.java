package com.kosherdev.imagecapturetest.tasks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kosherdev.imagecapturetest.MainActivity;
import com.kosherdev.imagecapturetest.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

public class GetCamFind {
    private MainActivity mainActivity;
    //Set constants
    private static final String API_URL = "http://api.cloudsightapi.com/";;
    private static final String language = new String("en_US");
    private static String keyMashape = "";

    private String token = "";
    private String result = "";

    public GetCamFind(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        keyMashape = mainActivity.getResources().getString(R.string.cloundsight_api_key);
        result = "";
        token = "";
    }

    //creates object getCamFind, sends request and gets answer
    void sendCamRequest(String path) throws IOException {

        File imageFile = new File(path); //THIS FILE EXISTS AND SD IS MOUNTED

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                        //.setRequestInterceptor(new SessionRequestInterceptor())
                .build();

        // Create an instance of our API interface.
        CamFind camFind = restAdapter.create(CamFind.class);

        camFind.send(keyMashape, language, new TypedFile("image/jpeg", imageFile), new Callback<resultClass>() {
            @Override
            public void success(resultClass result, Response response) {
                TypedInput body = response.getBody();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
                    StringBuilder out = new StringBuilder();
                    String newLine = System.getProperty("line.separator");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                        out.append(newLine);
                    }

                    // Prints the correct String representation of body.
                    System.out.println(out);

                    JsonObject obj = new JsonParser().parse(out.toString()).getAsJsonObject();
                    token = obj.get("token").getAsString();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("Error : " + retrofitError.getMessage());
            }
        });

    }

    String sendCamResult(){

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                        //.setRequestInterceptor(new SessionRequestInterceptor())
                .build();

        // Create an instance of our API interface.
        CamFind camFind = restAdapter.create(CamFind.class);

        if (!token.equals(""))
        {
            camFind.sendForResponse(keyMashape, token, new Callback<resultClass>() {
                @Override
                public void success(resultClass resultClass, Response response) {
                    TypedInput body = response.getBody();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
                        StringBuilder out = new StringBuilder();
                        String newLine = System.getProperty("line.separator");
                        String line;
                        while ((line = reader.readLine()) != null) {
                            out.append(line);
                            out.append(newLine);
                        }

                        // Prints the correct String representation of body.
                        //System.out.println(out);

                        result = out.toString();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {

                }
            });
        }

        return result;
    }

    static class Contributor {
        String login;
        int contributions;
    }


    //interface of the object getCamFind
    interface CamFind {

        @Multipart
        @POST("/image_requests")
        void send(
                @Header("Authorization") String keyMashape,
                @Part("image_request[locale]") String language,
                @Part("image_request[image]") TypedFile imageFile,

                //@Field("image_request[remote_image_url]") String URL,
                Callback<resultClass> callback);

        @GET("/image_responses/{token}")
        void sendForResponse(
                @Header("Authorization") String keyMashape,
                @Path("token") String token,
                Callback<resultClass> callback);

    }

    private class resultClass{
        String name;
        String status;

        public String getName(){return this.name;}
        public String getStatus(){return this.status;}


    }


}