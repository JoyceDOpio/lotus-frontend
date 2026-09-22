package com.eternalfairy.lotus.data.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.eternalfairy.lotus.data.auth.AuthApi
import com.eternalfairy.lotus.data.auth.AuthInterceptor
import com.eternalfairy.lotus.data.test.MockAuthRepository
import com.eternalfairy.lotus.domain.auth.AuthRepository
import com.eternalfairy.lotus.data.dao.IActivityDAO
import com.eternalfairy.lotus.data.dao.IDayDAO
import com.eternalfairy.lotus.data.dao.IGoalDAO
import com.eternalfairy.lotus.data.dao.ITaskDAO
import com.eternalfairy.lotus.data.dao.IUserProfileDAO
import com.eternalfairy.lotus.data.dao.IVoiceNoteDAO
import com.eternalfairy.lotus.data.dao.postgres.ActivityApi
import com.eternalfairy.lotus.data.dao.postgres.ActivityDAO
import com.eternalfairy.lotus.data.dao.postgres.DayApi
import com.eternalfairy.lotus.data.dao.postgres.DayDAO
import com.eternalfairy.lotus.data.dao.postgres.GoalApi
import com.eternalfairy.lotus.data.dao.postgres.GoalDAO
import com.eternalfairy.lotus.data.dao.postgres.TaskApi
import com.eternalfairy.lotus.data.dao.postgres.TaskDAO
import com.eternalfairy.lotus.data.dao.postgres.UserProfileApi
import com.eternalfairy.lotus.data.dao.postgres.UserProfileDAO
import com.eternalfairy.lotus.data.dao.postgres.VoiceNoteApi
import com.eternalfairy.lotus.data.dao.postgres.VoiceNoteDAO
import com.eternalfairy.lotus.domain.repository.IActivityRepository
import com.eternalfairy.lotus.domain.repository.IDayRepository
import com.eternalfairy.lotus.domain.repository.IGoalRepository
import com.eternalfairy.lotus.domain.repository.ITaskRepository
import com.eternalfairy.lotus.domain.repository.IUserProfileRepository
import com.eternalfairy.lotus.domain.repository.IVoiceNoteRepository
import com.eternalfairy.lotus.data.test.MockActivityRepository
import com.eternalfairy.lotus.data.test.MockDayRepository
import com.eternalfairy.lotus.data.test.MockGoalRepository
import com.eternalfairy.lotus.data.test.MockTaskRepository
import com.eternalfairy.lotus.data.repository.UserProfileRepository
import com.eternalfairy.lotus.data.repository.VoiceNoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

//val tokenProvider: () -> String? = {
//    /* Logic to fetch token (i.e. get from shared preferences) */
//}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // The ip of the computer
    val baseUrl = "http://192.168.3.135:8080"

    @Provides
    @Singleton
    fun provideOkHttpClient(preferences: SharedPreferences): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
//                AuthInterceptor(tokenProvider)
                AuthInterceptor({ preferences.getString("jwt", Context.MODE_PRIVATE.toString()) })
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(okHttpClient: OkHttpClient): AuthApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            // To automatically parse JSON
            .addConverterFactory(
                MoshiConverterFactory.create()
            )
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, preferences: SharedPreferences): AuthRepository {
//      return AuthRepository(
//          api = api,
//          preferences = preferences
//      )

        return MockAuthRepository(
            preferences = preferences
        )
    }

    @Provides
    @Singleton
    fun provideActivityApi(): ActivityApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideActivityDAO(activityApi: ActivityApi): IActivityDAO {
        return ActivityDAO(
            api = activityApi
        )
    }

    @Provides
    @Singleton
    fun provideActivityRepository(activityDAO: IActivityDAO): IActivityRepository {
//        return ActivityRepository(
//            dao = activityDAO
//        )
        return MockActivityRepository()
    }

    @Provides
    @Singleton
    fun provideDayApi(): DayApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideDayDAO(dayApi: DayApi): IDayDAO {
        return DayDAO(
            api = dayApi
        )
    }

    @Provides
    @Singleton
    fun provideDayRepository(dayDAO: IDayDAO): IDayRepository {
//        return DayRepository(
//            dao = dayDAO
//        )
        return MockDayRepository()
    }

    @Provides
    @Singleton
    fun provideGoalApi(): GoalApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideGoalDAO(goalApi: GoalApi): IGoalDAO {
        return GoalDAO(
            api = goalApi
        )
    }

    @Provides
    @Singleton
    fun provideGoalRepository(goalDAO: IGoalDAO): IGoalRepository {
//        return GoalRepository(
//            dao = goalDAO
//        )
        return MockGoalRepository()
    }

    @Provides
    @Singleton
    fun provideTaskApi(): TaskApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideTaskDAO(taskApi: TaskApi): ITaskDAO {
        return TaskDAO(
            api = taskApi
        )
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDAO: ITaskDAO): ITaskRepository {
//        return TaskRepository(
//            dao = taskDAO
//        )
        return MockTaskRepository()
    }

    @Provides
    @Singleton
    fun provideUserProfileApi(): UserProfileApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideUserProfileDAO(userProfileApi: UserProfileApi): IUserProfileDAO {
        return UserProfileDAO(
            api = userProfileApi
        )
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(userProfileDAO: IUserProfileDAO): IUserProfileRepository {
        return UserProfileRepository(
            dao = userProfileDAO
        )
    }

    @Provides
    @Singleton
    fun provideVoiceNoteApi(): VoiceNoteApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            // To automatically parse JSON
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideVoiceNoteDAO(voiceNoteApi: VoiceNoteApi): IVoiceNoteDAO {
        return VoiceNoteDAO(
            api = voiceNoteApi
        )
    }

    @Provides
    @Singleton
    fun provideVoiceNoteRepository(voiceNoteDAO: IVoiceNoteDAO): IVoiceNoteRepository {
        return VoiceNoteRepository(
            dao = voiceNoteDAO
        )
    }
}