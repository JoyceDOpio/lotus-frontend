package com.eternalfairy.lotus.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.eternalfairy.lotus.auth.AuthApi
import com.eternalfairy.lotus.auth.AuthInterceptor
import com.eternalfairy.lotus.auth.AuthRepository
import com.eternalfairy.lotus.auth.IAuthRepository
import com.eternalfairy.lotus.model.dao.IActivityDAO
import com.eternalfairy.lotus.model.dao.IDayDAO
import com.eternalfairy.lotus.model.dao.IGoalDAO
import com.eternalfairy.lotus.model.dao.ITaskDAO
import com.eternalfairy.lotus.model.dao.IUserProfileDAO
import com.eternalfairy.lotus.model.dao.IVoiceNoteDAO
import com.eternalfairy.lotus.model.dao.postgres.ActivityApi
import com.eternalfairy.lotus.model.dao.postgres.ActivityDAO
import com.eternalfairy.lotus.model.dao.postgres.DayApi
import com.eternalfairy.lotus.model.dao.postgres.DayDAO
import com.eternalfairy.lotus.model.dao.postgres.GoalApi
import com.eternalfairy.lotus.model.dao.postgres.GoalDAO
import com.eternalfairy.lotus.model.dao.postgres.TaskApi
import com.eternalfairy.lotus.model.dao.postgres.TaskDAO
import com.eternalfairy.lotus.model.dao.postgres.UserProfileApi
import com.eternalfairy.lotus.model.dao.postgres.UserProfileDAO
import com.eternalfairy.lotus.model.dao.postgres.VoiceNoteApi
import com.eternalfairy.lotus.model.dao.postgres.VoiceNoteDAO
import com.eternalfairy.lotus.model.repository.ActivityRepository
import com.eternalfairy.lotus.model.repository.DayRepository
import com.eternalfairy.lotus.model.repository.GoalRepository
import com.eternalfairy.lotus.model.repository.IActivityRepository
import com.eternalfairy.lotus.model.repository.IDayRepository
import com.eternalfairy.lotus.model.repository.IGoalRepository
import com.eternalfairy.lotus.model.repository.ITaskRepository
import com.eternalfairy.lotus.model.repository.IUserProfileRepository
import com.eternalfairy.lotus.model.repository.IVoiceNoteRepository
import com.eternalfairy.lotus.model.repository.TaskRepository
import com.eternalfairy.lotus.model.repository.UserProfileRepository
import com.eternalfairy.lotus.model.repository.VoiceNoteRepository
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
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
    fun provideAuthRepository(api: AuthApi, preferences: SharedPreferences): IAuthRepository {
      return AuthRepository(
          api = api,
          preferences = preferences
      )
    }

    @Provides
    @Singleton
    fun provideActivityApi(): ActivityApi {
        return Retrofit.Builder()
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
        return ActivityRepository(
            dao = activityDAO
        )
    }

    @Provides
    @Singleton
    fun provideDayApi(): DayApi {
        return Retrofit.Builder()
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
        return DayRepository(
            dao = dayDAO
        )
    }

    @Provides
    @Singleton
    fun provideGoalApi(): GoalApi {
        return Retrofit.Builder()
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
        return GoalRepository(
            dao = goalDAO
        )
    }

    @Provides
    @Singleton
    fun provideTaskApi(): TaskApi {
        return Retrofit.Builder()
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
        return TaskRepository(
            dao = taskDAO
        )
    }

    @Provides
    @Singleton
    fun provideUserProfileApi(): UserProfileApi {
        return Retrofit.Builder()
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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
            // The ip of the computer
            .baseUrl("http://172.27.176.1:8080")
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