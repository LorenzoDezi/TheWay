package com.unicam.dezio.theway;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by dezio on 22/11/16.
 */

public interface RequestInterface {

    @POST("theWayServer/")
    Call<ServerResponse> operation(@Body ServerRequest request);

    @Multipart
    @POST("theWayServer/upload.php/")
    Call<ResponseBody> upload(@Part("description") RequestBody description,
                              @Part MultipartBody.Part file);
    @GET
    Call<ResponseBody> downloadGPX(@Url String fileUrl);


}
