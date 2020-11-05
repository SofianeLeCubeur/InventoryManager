package com.github.sofiman.inventory.impl;

import android.os.AsyncTask;

import com.github.sofiman.inventory.utils.Callback;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class NetworkCall<T> extends AsyncTask<Call<T>, Void, T> {

    private APIResponse<T> response;

    public NetworkCall(APIResponse<T> response) {
        super();
        this.response = response;
    }

    @SafeVarargs
    @Override
    protected final T doInBackground(Call<T>... call) {
        try {
            Response<T> response = call[0].execute();
            if (response.isSuccessful()) {
                return response.body();
            } else if(response.errorBody() != null) {
                String s = response.errorBody().string();
                System.err.println("Server responded with an error: " + s);
                JsonObject obj = JsonParser.parseString(s).getAsJsonObject();
                RequestError error = new RequestError(response.code(), obj.get("err").getAsString(), obj.get("err_description").getAsString());
                this.response.error(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.response.error(new RequestError(-1, "parse_failed", "Retrofit converter failed to parse JSON Body"));
            } catch (Exception e1){
                e1.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(T result){
        if(result != null){
            try {
                this.response.response(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}