package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.auth.InjectAuth
import com.eternalfairy.lotus.model.data.Day
import kotlinx.datetime.LocalDate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface DayApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/day/{id}")
    suspend fun deleteDay(@Path("id") id: Uuid)

    @InjectAuth
    @GET("/day/{date}")
    suspend fun getDay(@Path("date") date: LocalDate): Day?

    @InjectAuth
    @POST("/day")
    suspend fun insertDay(@Body day: Day)

    @InjectAuth
    @PUT("/day")
    suspend fun updateDay(@Body day: Day)
}