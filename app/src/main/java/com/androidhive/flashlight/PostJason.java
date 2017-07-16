package com.androidhive.flashlight;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by lucadalseno on 26/06/17.
 */

public class  PostJason extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        String url = "http://192.168.1.76";


    @Override
    public JSONObject doInBackground(JSONObject... jsonObjects) {

            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 8080);

            JSONObject jsonResponse = null;
            HttpPost post = new HttpPost(url);

            try {

                StringEntity se = new StringEntity(jsonObjects.toString());
                post.addHeader("content-type", "application/x-www-form-urlencoded");
                post.setEntity(se);

                HttpResponse response;
                response = client.execute(post);
                String resFromServer = org.apache.http.util.EntityUtils.toString(response.getEntity());

                jsonResponse=new JSONObject(resFromServer);
                Log.i("Response from server", jsonResponse.getString("msg"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return jsonResponse;
            }

    public JSONObject createJson(String sdk, String device){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.accumulate("SDK", sdk);
            jsonObject.accumulate("Device", device);
        } catch (Exception e){

        }

            // convert JSONObject to JSON to String
            return jsonObject;
        }
}










       /* InputStream inputStream = null;
        String result = "";
        String json = "";




        public String createJson(String sdk, String device){
            JSONObject jsonObject = new JSONObject();

            try{
                jsonObject.accumulate("SDK", sdk);
                jsonObject.accumulate("Device", device);
            } catch (Exception e){

            }

            // convert JSONObject to JSON to String
           return jsonObject.toString();
        }

        public void post(String json) {

            try {
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                //make POST request to the given URL
                HttpPost httpPost = new HttpPost("http://192.168.1.76:8080/index.html");
                // set json to StringEntity
                StringEntity se = new StringEntity(json);
                // set httpPost Entity
                httpPost.setEntity(se);
                // Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                // Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                //  convert inputstream to string
                if (inputStream != null)
                    result = (inputStream).toString();
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.i("InputStream", e.getLocalizedMessage());
            }
        }*/


