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
 * This interface is used by retrofit2 API to make requests to
 * the server. Its methods specifies different kinds of requests
 */

public interface RequestInterface {

    /**
     *This method sends the standard request to the server, request's different
     * properties change the kind of operation.
     */
    @POST("theWayServer/")
    Call<ServerResponse> operation(@Body ServerRequest request);

    /**
     *This method is used to upload files to the server, in particular
     * gpxs related to the path.
     */
    @Multipart
    @POST("theWayServer/upload.php/")
    Call<ResponseBody> upload(@Part("description") RequestBody description,
                              @Part MultipartBody.Part file);

    /**
     *This method is used to download gpxs from the server.
     */
    @GET
    Call<ResponseBody> downloadGPX(@Url String fileUrl);


}
