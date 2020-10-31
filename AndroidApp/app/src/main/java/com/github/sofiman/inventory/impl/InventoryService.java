package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.Container;
import com.github.sofiman.inventory.api.Inventory;
import com.github.sofiman.inventory.api.Item;
import com.github.sofiman.inventory.api.Token;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InventoryService {

    @POST("token")
    Call<Token> getToken(@Body HashMap<String, Object> body);

    @GET("inventories")
    Call<List<Inventory>> listInventories(@Header("Authorization") String authorization);

    @POST("inventory")
    Call<Inventory> createInventory(@Header("Authorization") String authorization, @Body HashMap<String, Object> body);

    @GET("inventory/{id}")
    Call<Inventory> fetchInventory(@Header("Authorization") String authorization, @Path("id") String id);

    @POST("inventory/{id}")
    Call<Inventory> pushInventory(@Header("Authorization") String authorization, @Path("id") String id, @Body HashMap<String, Object> body);

    @GET("item/{id}")
    Call<Item> fetchItem(@Header("Authorization") String authorization, @Path("id") String id);

    @POST("item/{id}")
    Call<Item> pushItem(@Header("Authorization") String authorization, @Path("id") String id, @Body HashMap<String, Object> body);

    @POST("items")
    Call<List<Item>> fetchItems(@Header("Authorization") String authorization, @Body HashMap<String, Object> body);

    @GET("containers")
    Call<List<Container>> listContainers(@Header("Authorization") String authorization);

    @GET("container/{id}")
    Call<Container> fetchContainer(@Header("Authorization") String authorization, @Path("id") String id);

    @POST("container/{id}")
    Call<Container> pushContainer(@Header("Authorization") String authorization, @Path("id") String id, @Body HashMap<String, Object> body);

    @POST("scan/{id}")
    Call<HashMap<String, Object>> fetchScanResult(@Header("Authorization") String authorization, @Path("id") String id, @Body HashMap<String, String> body);

    @POST("search")
    Call<HashMap<String, Object>> query(@Header("Authorization") String authorization, @Body HashMap<String, String> body);
}