package com.example.sysbase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Esta es la ruta exacta que tienes en Laravel
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

}