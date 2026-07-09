package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.auth.InjectAuth
import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ActivityApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/activity/{id}")
    suspend fun deleteActivity(@Path("id") id: Uuid)

    @InjectAuth
    @GET("/activity/{date}")
    suspend fun getActivities(@Path("date") date: LocalDate): List<Activity>

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/activity/{id}")
    suspend fun getActivity(@Path("id") id: Uuid): Activity?

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/activity/sub-activities/{id}")
    suspend fun getSubActivitiesOfMainActivity(mainActivityId: Uuid): List<Activity>

    @InjectAuth
    @GET("/activity/recorded/main")
    suspend fun getMainRecordedActivity(): Activity?

    @InjectAuth
    @GET("/activity/recorded/sub")
    suspend fun getSubRecordedActivity(): Activity?

    @InjectAuth
    @POST("/activity")
    suspend fun insertActivity(@Body activity: Activity)

    @InjectAuth
    @PUT("/activity")
    suspend fun updateActivity(@Body activity: Activity)
}