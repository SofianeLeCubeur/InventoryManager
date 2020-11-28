package com.github.sofiman.inventory.impl;

import android.os.Build;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Version;

/**
 * Adds a custom {@code User-Agent} header to OkHttp requests.
 * @author twaddington
 */
public class UserAgentInterceptor implements Interceptor {

    public final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgentInterceptor(String appName, String appVersion) {
        this(String.format(Locale.US,
                "%s/%s (Android %s; %s; %s %s; %s) %s",
                appName,
                appVersion,
                Build.VERSION.RELEASE,
                Build.MODEL,
                Build.BRAND,
                Build.DEVICE,
                Locale.getDefault().getLanguage(),
                Version.userAgent()));
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userAgentRequest = chain.request()
                .newBuilder()
                .header("User-Agent", userAgent)
                .build();
        return chain.proceed(userAgentRequest);
    }
}