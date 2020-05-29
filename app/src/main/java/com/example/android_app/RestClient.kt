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
import kotlin.collections.HashMap
import javax.xml.datatype.DatatypeConstants.SECONDS
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder
import com.google.gson.Gson




object RestClient {
//    private const val URL = "http://10.0.2.2:3000/api/file/"
    private const val URL = "http://192.168.0.105:3000/"
//    private const val URL = "http://172.20.10.5:3000/"

//    private const val URL = "http://192.168.43.38:3000/"
    interface Service {


        @POST("api/file")
        fun upload(
//            @PartMap map: HashMap<String, RequestBody>
            @Body fileEntity: FileEntity
        ): Observable<FileResult>

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

    val service: Service = retrofit.create(Service::class.java)

}