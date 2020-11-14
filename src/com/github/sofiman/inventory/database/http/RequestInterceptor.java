package com.github.sofiman.inventory.database.http;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {

    private String token;
    private String tokenType;
    private boolean useOauth2 = false;

    public RequestInterceptor(){
    }

    public void setToken(String token, String type){
        this.token = token;
        this.tokenType = type;
    }

    public void enableOauth2(){
        useOauth2 = true;
    }

    public void disableOauth2(){
        useOauth2 = false;
    }

    public boolean isOauth2Enabled(){
        return useOauth2;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        if (!useOauth2 || token == null || tokenType == null) {
            return chain.proceed(originalRequest);
        }

        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", tokenType + " " + token)
                .build();

        return chain.proceed(newRequest);
    }

}
