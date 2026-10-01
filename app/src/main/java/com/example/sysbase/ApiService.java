package com.example.sysbase;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // Ruta de inicio de sesión
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Obtener la lista de activos asignados al empleado
    @GET("employee/assets")
    Call<List<Asset>> getEmployeeAssets(@Header("Authorization") String token);

    // Levantar un ticket/reporte en formato Multipart
    @Multipart
    @POST("employee/tickets")
    Call<Void> createTicket(
            @Header("Authorization") String token,
            @Part("asset_id") RequestBody assetId,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );
}