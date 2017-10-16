package com.androidhive.flashlight;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lucadalseno on 08/10/17.
 */

public class Connection extends AsyncTask{

    private URL url;
    private HttpURLConnection httpURLConnection;
    private DataOutputStream wr;
    private JSONObject jsonObject;


    public Connection(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            setUrl(new URL("http://95.236.89.221:8000/Server/")); //Server's URL
            setHttpURLConnection((HttpURLConnection) getUrl().openConnection());
            getHttpURLConnection().setDoOutput(true);
            getHttpURLConnection().setRequestMethod("POST"); //Specify it's a POST request
            getHttpURLConnection().setRequestProperty("Content-Type", "application/json"); //Setting the type of content to JSON
            getHttpURLConnection().connect();


            //Opening an output stream towards the server
            setDataOutputStream(new DataOutputStream(httpURLConnection.getOutputStream()));
            getDataOutputStream().writeBytes(this.jsonObject.toString());
            getDataOutputStream().flush();
            getDataOutputStream().close();

            //Response of the http connection
            Log.i("URL", "" + getHttpURLConnection().getURL().toString());
            Log.i("Response", "" + getHttpURLConnection().getResponseMessage());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setUrl(URL url){
        this.url = url;
    }

    private URL getUrl(){
        return this.url;
    }

    private void setHttpURLConnection(HttpURLConnection httpURLConnection){
        this.httpURLConnection = httpURLConnection;
    }

    private HttpURLConnection getHttpURLConnection(){
        return this.httpURLConnection;
    }

    private void setDataOutputStream(DataOutputStream wr){
        this.wr = wr;
    }

    private DataOutputStream getDataOutputStream(){
        return this.wr;
    }

}

