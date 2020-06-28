package com.example.android_app.networking

import com.example.android_app.models.CompilationText
import com.example.android_app.models.FileEntity
import com.example.android_app.models.FileResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import io.reactivex.Observable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder


object RestClient {
//    private const val URL = "http://10.0.2.2:3000/api/file/"
//    private const val URL = "http://172.20.10.5:3000/"
//    private const val URL = "http://172.20.10.5:3000/"

//    private const val URL = "http://192.168.0.102:3000/"
    private const val URL = "http://172.20.10.5:3000/"
    interface Service {

        @POST("api/file")
        fun upload(
            @Body fileEntity: FileEntity
        ): Observable<FileResult>

        @POST("api/file/compile")
        fun compile(
            @Body compilationText: CompilationText
        ): Observable<CompilationText>

}

    var okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    var gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    val service: Service = retrofit.create(
        RestClient.Service::class.java)

}