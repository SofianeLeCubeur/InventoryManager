package com.github.sofiman.inventory.database.http;

import com.github.sofiman.inventory.database.DatabaseError;
import com.github.sofiman.inventory.database.DatabaseResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Response;

public class NetworkCall<T> extends Thread {

    private Call<T> call;
    private final DatabaseResponse<T> response;

    public NetworkCall(Call<T> call, DatabaseResponse<T> response) {
        this.call = call;
        this.response = response;
    }

    @Override
    public void run() {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                this.response.response(response.body());
            } else if(response.errorBody() != null) {
                String s = response.errorBody().string();
                System.err.println("Server responded with an error: " + s);
                JsonObject obj = JsonParser.parseString(s).getAsJsonObject();
                DatabaseError error = new DatabaseError(response.code(), obj.get("err").getAsString(), obj.get("err_description").getAsString());
                this.response.error(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.response.error(new DatabaseError(-1, "parse_failed", "Retrofit converter failed to parse JSON Body"));
            } catch (Exception e1){
                e1.printStackTrace();
            }
        }
    }

}