package com.eternalfairy.lotus.model.dao.powersync

import com.powersync.PowerSyncDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(DelicateCoroutinesApi::class)
@Singleton
class OnlineSyncDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase,
//    private val supabaseClient: SupabaseClient
) {
    init {
//        GlobalScope.launch {
//            powerSyncDatabase.connect(SupabaseConnector(
//                supabaseClient,
////                dotenv["POWERSYNC_URL"]
//                "https://69d13606e7a6f7d0681b88c2.powersync.journeyapps.com"
//            ))
//        }
    }
    fun getDatabase(): PowerSyncDatabase {
        return powerSyncDatabase
    }
}