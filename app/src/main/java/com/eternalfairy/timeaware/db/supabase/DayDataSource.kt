package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.supabase.ApiResponse
import com.eternalfairy.timeaware.db.data.Day
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DayDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun insertDay(day: Day): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.days").insert(
                    day
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectDayByDate(date: String): Flow<ApiResponse<Day>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val day = supabaseClient.from("public.days").select() {
                    filter {
                        Day::date eq date
                    }
                }.decodeSingle<Day>()
                emit(ApiResponse.Success(day))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun updateDay(day: Day): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.days").update(
                    {
                        Day::activeTimeStart setTo day.activeTimeStart
                        Day::activeTimeEnd setTo day.activeTimeEnd
                        Day::actualActiveTimeStart setTo day.actualActiveTimeStart
                        Day::actualActiveTimeEnd setTo day.actualActiveTimeEnd
                        Day::note setTo day.note
                    }
                ) {
                    filter {
                        Day::id eq day.id
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}