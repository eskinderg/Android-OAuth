package com.example.drawer.auth;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthDataService {

    @FormUrlEncoded
    @POST("/realms/master/protocol/openid-connect/token")
    Call<AccessToken> getAccessToken(
            @Field("client_id") String client_id,
            @Field("grant_type") String grant_type,
            @Field("scope") String scope,
            @Field("username") String username,
            @Field("password") String password
    );

    @Authorized
    @POST("/realms/master/protocol/openid-connect/userinfo")
    Call<User> getUserInfo();

    @FormUrlEncoded
    @POST("/realms/master/protocol/openid-connect/logout")
    Call<ResponseBody> logout(
            @Field("client_id") String client_id,
            @Field("refresh_token") String refreshToken
    );
}
