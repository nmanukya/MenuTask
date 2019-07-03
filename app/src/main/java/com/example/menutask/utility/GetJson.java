package com.example.menutask.utility;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.example.menutask.handlers.HttpHandler;
import com.example.menutask.interfaces.AsyncResponse;

public class GetJson extends AsyncTask<String, Void, String> {

    private AsyncResponse delegate;//Call back interface
    private String url;//Call back interface

    public GetJson(String url, AsyncResponse asyncResponse){
        delegate = asyncResponse;
        this.url=url;
    }
    //onPreExecute() invoked on the UI thread before the task is executed.
    // This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
    @Override
    protected void onPreExecute() {super.onPreExecute();}
    // doInBackground(Params...) invoked on the background thread immediately after onPreExecute() finishes executing.
    // This step is used to perform background computation that can take a long time.
    // The parameters of the asynchronous task are passed to this step.
    // The result of the computation must be returned by this step and will be passed back to the last step
    @Nullable
    @Override
    protected String doInBackground(String... arg0) {
        HttpHandler sh = new HttpHandler();
        return sh.makeServiceCall(url);
    }
    // onProgressUpdate(Progress...), invoked on the UI thread after a call to publishProgress(Progress...).
    // it can be used to animate a progress bar or show logs in a text field.

    //onPostExecute(Result) invoked on the UI thread after the background computation finishes.
    // The result of the background computation is passed to this step as a parameter.
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //
        delegate.processFinish(result);
    }
}
