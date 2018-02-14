package com.openshamba.mpesa.data;

import com.openshamba.mpesa.data.entities.ApiResponse;
import com.openshamba.mpesa.data.entities.LoginResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Maina on 1/17/2018.
 */

public interface MpesaClient {

    @Headers({"Cache-Control: no-cache"})
    @FormUrlEncoded
    @POST("v1/login/facebook")
    Call<LoginResponse> login(
            @Field("auth_token") String auth_token
    );

    @Multipart
    @POST("v1/upload")
    Call<ResponseBody> upload(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );

    @GET("v1/stk")
    Call<ApiResponse> stk(
            @Query("phone") String phone,
            @Query("amount") int amount,
            @Query("description") String description
    );
}
