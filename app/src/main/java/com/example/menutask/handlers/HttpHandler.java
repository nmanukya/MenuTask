package com.example.menutask.handlers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpHandler {
    public HttpHandler() {
    }

    @Nullable
    public String makeServiceCall(String reqUrl) {

        String response = null;
        try {

            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (checkServerConnection(conn)) {
                conn.setRequestMethod("GET");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
            } else {
                response = "Server is not available";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    private boolean checkServerConnection(HttpURLConnection url) {
        url.setConnectTimeout(3000); //<- 3Seconds Timeout
        try {
            url.connect();
            return (url.getResponseCode() == 200);
        } catch (MalformedURLException ignored) {
            return false;
        } catch (IOException ignored) {
            return false;
        }

    }

    @NonNull
    private String convertStreamToString(@NonNull InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                //

                //sb.append(line).append('\n');
                sb.append(line);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //
        return sb.toString();
    }
}