package com.example.rfidscanner.api

import com.example.rfidscaner.model.CreateInventorizationRequest
import com.example.rfidscaner.model.LocationModel
import com.example.rfidscanner.model.CategoryModel
import com.example.rfidscanner.model.HolderModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/authorization/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @GET("api/Inventorization/GetInventorization")
    suspend fun getInventorization(): Response<ResponseBody>

    @GET("api/Inventorization/GetInventorizationItems")
    suspend fun getInventorizationItems(@Query("Id") id: Int): Response<ResponseBody>

    @GET("api/Inventorization/Detected")
    suspend fun detected(@Query("Id") id: Int): Response<ResponseBody>

    @GET("api/Inventorization/InventorizationDone")
    suspend fun done(@Query("Id") id: Int): Response<ResponseBody>

    @GET("api/Transfer/GetAllLocations")
    suspend fun getAllLocations(): Response<List<LocationModel>>

    @POST("api/Inventorization/create")
    suspend fun createInventorization(@Body request: CreateInventorizationRequest): Response<ResponseBody>

    @GET("api/Holder/GetAllHolders")
    suspend fun getAllHolders(): Response<List<HolderModel>>

    @GET("api/Category/maincategories")
    suspend fun getMainCategories(@Query("img") img: Int = 1): Response<List<CategoryModel>>

    // Новый метод для экрана излишков
    @GET("api/Surplus/GetAllSurplus")
    suspend fun getAllSurplus(): Response<ResponseBody>

    @GET("api/Inventorization/StandingCheck")
    suspend fun standingCheck(
        @Query("mark") mark: String,
        @Query("location") location: String
    ): Response<ResponseBody>
}