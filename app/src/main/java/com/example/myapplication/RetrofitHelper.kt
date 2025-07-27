package com.example.myapplication

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url


object RetrofitHelper {

    private var baseUrl = "http://127.0.0.2:5000/"
    //private const val baseUrl = "http://192.168.2.150/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun setBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
    }

    fun getBaseUrl(): String {
        return baseUrl
    }
}


data class getBackupsResponse(
    // "status" es lo que viene en el json
    @SerializedName("BackUpList") var backUpList: List<BackUpList>,
    @SerializedName("status") var status: String
)


data class getBackupsName(
    // "status" es lo que viene en el json
    @SerializedName("bd_names") var bd_names: List<BackUpListNameServer>,
    @SerializedName("status") var status: String,
    @SerializedName("response") var response: String,
)


data class LoginCheck(
    @SerializedName("status") val status: Boolean
)


data class MyCookie(
    @SerializedName("csrf_token") val cookie: String
)


class LoginResponse {
    val session: String = ""
    val status: Boolean = false
}

class LogoutResponse {
    val status: String = ""
}

class CreateResponse {
    val response: String = ""
    val status: String = ""
}

class deleteResponse {
    val response: String = ""
}


interface QuotesApi {
    @GET("/api/v1/get_cookie")
    suspend fun getCookies(): MyCookie


    @GET("/api/v1/get_backups")
    suspend fun getBackups(@Header("Cookie") Cookie: String): Response<getBackupsResponse>


    @GET("/api/v1/get_bd_names")
    suspend fun getBackupsNames(@Header("Cookie") Cookie: String): Response<getBackupsName>


    @GET("/api/v1/checklogin")
    suspend fun checklogin(@Header("Cookie") Cookie: String): Response<LoginCheck>


    @Streaming
    @GET
    suspend fun downloadBackup(
        @Header("Cookie") Cookie: String,
        @Url() url: String
    ): Response<ResponseBody>


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/api/v1/create_backup/")
    suspend fun create_backup(
        @Header("Cookie") Cookie: String,
        @Field("backup_cname") backup_cname: String,
        @Field("backup_name") backup_name: String,
    ): CreateResponse


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/api/v1/restore_bd/")
    suspend fun restore_bd(
        @Header("Cookie") Cookie: String,
        @Field("backup_id") backup_id: Int,
        @Field("backup_name") backup_name: String,
        @Field("restore_create_new") restore_create_new: Boolean
    ): CreateResponse


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/api/v1/login/")
    suspend fun login(
        @Header("Cookie") Cookie: String,
        @Field("inputUser") inputUser: String,
        @Field("inputPassword") inputPassword: String,
        @Field("csrf_token") csrf_token: String,
    ): LoginResponse


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/api/v1/logout/")
    suspend fun logout(@Header("Cookie") Cookie: String): LogoutResponse


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/api/v1/delete_backup/")
    suspend fun delete(
        @Header("Cookie") Cookie: String,
        @Field("backup_id") backup_id: String,
        @Field("csrf_token") csrf_token: String,
        // ): deleteResponse
    ): Response<deleteResponse>
}



