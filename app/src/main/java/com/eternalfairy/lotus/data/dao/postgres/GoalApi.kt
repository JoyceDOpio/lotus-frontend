package com.eternalfairy.lotus.data.dao.postgres

import com.eternalfairy.lotus.data.auth.InjectAuth
import com.eternalfairy.lotus.data.model.Goal
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface GoalApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/goal/{id}")
    suspend fun deleteGoal(@Path("id") id: Uuid)

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/goal/{id}")
    suspend fun getGoal(@Path("id") id: Uuid): Goal?

    @InjectAuth
    @GET("/goal")
    suspend fun getGoals(): List<Goal>

    @InjectAuth
    @GET("/goal/last-priority")
    suspend fun getLastPriority(): Int?

    @InjectAuth
    @POST("/goal")
    suspend fun insertGoal(@Body goal: Goal)

    @InjectAuth
    @PUT("/goal")
    suspend fun updateGoal(@Body goal: Goal)
}