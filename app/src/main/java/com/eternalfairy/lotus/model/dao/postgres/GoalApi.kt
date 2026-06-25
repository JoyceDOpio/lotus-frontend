package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.data.Goal
import kotlinx.datetime.LocalDate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface GoalApi {
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/goal/{id}")
    suspend fun deleteGoal(@Path("id") id: Uuid)

    @OptIn(ExperimentalUuidApi::class)
    @GET("/goal/{id}")
    suspend fun getGoal(@Path("id") id: Uuid): Goal?

    @GET("/goal")
    suspend fun getGoals(): List<Goal>

    @GET("/goal/last-priority")
    suspend fun getLastPriority(): Int?

    @POST("/goal")
    suspend fun insertGoal(@Body goal: Goal)

    @PUT("/goal")
    suspend fun updateGoal(@Body goal: Goal)
}