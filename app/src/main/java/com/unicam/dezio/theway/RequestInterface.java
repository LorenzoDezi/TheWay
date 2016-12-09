package com.unicam.dezio.theway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by dezio on 22/11/16.
 */

public interface RequestInterface {

    @POST("theWayServer/")
    Call<ServerResponse> operation(@Body ServerRequest request);

}
