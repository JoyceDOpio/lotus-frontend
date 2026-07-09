package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.auth.InjectAuth
import com.eternalfairy.lotus.model.data.Task
import kotlinx.datetime.LocalDate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/task/{id}")
    suspend fun deleteTask(@Path("id") id: Uuid)

    @InjectAuth
    @GET("/task/last-priority")
    suspend fun getLastPriority(): Int?

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/task/{id}")
    suspend fun getTask(@Path("id") id: Uuid): Task?

    @InjectAuth
    @GET("/task/{date}")
    suspend fun getTasks(@Path("date") date: LocalDate): List<Task>

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/task/without-date")
    suspend fun getWithoutDate(): List<Task>

    @InjectAuth
    @POST("/task")
    suspend fun insertTask(@Body task: Task)

    @InjectAuth
    @PUT("/task")
    suspend fun updateTask(@Body task: Task)
}