package com.example.android_app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*
import io.reactivex.Observable
import okhttp3.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


object RestClient {
    private const val URL = "http://10.0.2.2:3000/api/file/"

    interface Service {


//        @POST("")
//        fun uploadPhoto(@Body fileName: String, @Body file: String): Observable<String>

        @Multipart
        @POST("")
        fun upload(
            @PartMap map: Map<String, RequestBody>
        ): Call<ResponseBody>

//        @Multipart
//        @POST("api/update/enfant/photo/{id}")
//        fun uploadImage(
//            @Path("id") enfantId: Int,
//            @Part file: MultipartBody.Part,
//            @Part("image") requestBody: RequestBody
//        ): Call<ResponseBody>
    }

    val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    val service: Service = retrofit.create(Service::class.java)

}