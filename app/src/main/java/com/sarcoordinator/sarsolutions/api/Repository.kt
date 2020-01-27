package com.sarcoordinator.sarsolutions.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sarcoordinator.sarsolutions.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object Repository {
    private const val SAR_FUNCTIONS_URL = "https://us-central1-sar-solutions.cloudfunctions.net/"

    private val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(45, TimeUnit.SECONDS)
            .connectTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
    }

    private val service: API by lazy {
        Retrofit.Builder().baseUrl(SAR_FUNCTIONS_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build().create(API::class.java)
    }

    suspend fun getCases(): List<Case> {
        return service.getCases(getToken())
//        return service.getCases("!2")
    }

    suspend fun getCaseDetail(caseId: String): Case {
        return service.getCaseData(caseId, getToken())
    }

    // Synchronously get user token
    private fun getToken(): String {
        return Tasks.await(user.getIdToken(true)).token!!
    }

    suspend fun postStartShift(
        tokenId: String,
        shift: Shift,
        caseId: String,
        isTest: Boolean
    ) =
        service.postStartShift(tokenId, shift, caseId, isTest)

    suspend fun putLocations(
        tokenId: String,
        shiftId: String,
        isTest: Boolean,
        paths: List<LocationPoint>
    ) =
        service.putLocations(tokenId, shiftId, isTest, LocationBody(paths))

    suspend fun putEndTime(
        tokenId: String,
        shiftId: String,
        isTest: Boolean,
        endTime: String
    ) =
        service.putEndTime(tokenId, shiftId, isTest, EndTimeBody(endTime))
}