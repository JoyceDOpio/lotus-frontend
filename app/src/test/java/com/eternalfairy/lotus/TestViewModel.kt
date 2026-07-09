package com.eternalfairy.lotus

//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.CreationExtras
//import com.eternalfairy.lotus.model.data.Time
//import com.eternalfairy.lotus.view.viewmodel.room.DayState
//import com.eternalfairy.lotus.view.viewmodel.room.UserInput
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
//import java.time.LocalDate
//import java.util.UUID
//import kotlin.String
//
////typealias Tasks = List<Task>
////data class DayState (
////    val date: LocalDate = LocalDate.now(),
////    val activeTimeStart: Time = Time(6, 0),
////    val activeTimeEnd: Time = Time(22, 0),
////    val tasks: Tasks = emptyList()
////)
//
////data class UserInput(
////    val selectedDate: LocalDate = LocalDate.parse("2025-06-26"),
////)
//
//data class Day(
//    val date: LocalDate,
//    val activeTimeStart: Time,
//    val activeTimeEnd: Time,
//)
//
//data class Time(
//    var hour: Int = 0,
//    var minute: Int = 0
//)
//
//data class Task (
//    val date: LocalDate,
//    var title: String,
//    var startTime: Time,
//    var endTime: Time,
//    var description: String = "",
//    val id: UUID = UUID.randomUUID()
//)
//
//class TestViewModel() : ViewModel() {
//    companion object {
//        private const val MILLS = 5_000L
//
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(
//                modelClass: Class<T>,
//                extras: CreationExtras
//            ): T {
//                return TestViewModel() as T
//            }
//        }
//    }
//
//    val days = mapOf(
//        LocalDate.parse("2025-06-26") to Day(
//            date = LocalDate.parse("2025-06-26"),
//            activeTimeStart = Time(6,0),
//            activeTimeEnd = Time(22,0)
//        ),
//        LocalDate.parse("2025-06-27") to Day(
//            date = LocalDate.parse("2025-06-27"),
//            activeTimeStart = Time(6,0),
//            activeTimeEnd = Time(20,30)
//        )
//    )
//    val tasks = mapOf(
//        LocalDate.parse("2025-06-26") to listOf(
//            Task(
//                date = LocalDate.parse("2025-06-26"),
//                title = "Bis Dahin",
//                startTime = Time(11,3),
//                endTime = Time(13,0),
//                description = "Willkommen bei Gboard! Texte, die Sie kopieren, werden hier gespeichert.",
//                id = UUID.fromString("b8d9b09b-d18a-4e00-a347-9d388b5c779a")
//            ),
//            Task(
//                date = LocalDate.parse("2025-06-26"),
//                title = "No Let's Go",
//                startTime = Time(14,56),
//                endTime = Time(16,55),
//                description = "Programming Android with Kotlin",
//                id = UUID.fromString("c68ee93c-7100-4020-9006-c48b8402488d")
//            )
//        ),
//        LocalDate.parse("2025-06-27") to listOf(
//            Task(
//                date = LocalDate.parse("2025-06-27"),
//                title = "Hallo Herr Schmidt",
//                startTime = Time(15,34),
//                endTime = Time(20,0),
//                description = "Willkommen bei Gboard! Texte, die Sie kopieren, werden hier gespeichert.",
//                id = UUID.fromString("14873472-8bb4-43a8-9f5d-a0265a5f8eda")
//            )
//        )
//    )
//
//    val userInput = MutableStateFlow(UserInput())
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val dayState = userInput.flatMapLatest { input: UserInput ->
//        Log.i("TaskViewModel", "loading dayState")
//
//        val dayFlow = flow {
//            emit(days.get(input.selectedDate))
//        }
//        val tasksFlow = flow {
//            emit(tasks.get(input.selectedDate))
//        }
//
//        combine(
//            flow = dayFlow,
//            flow2 = tasksFlow
//        ) { day, tasks ->
//            DayState(
//                date = input.selectedDate,
//                activeTimeStart = day?.activeTimeStart ?: Time(6, 0),
//                activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),
//                tasks = tasks!!
//            )
//        }
//    }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
//            initialValue = DayState()
//        )
//
//    fun setSelectedDate(date: LocalDate) {
//        Log.i("setSelectedDate", date.toString())
//
//        userInput.update {
//            it.copy(
//                selectedDate = date
//            )
//        }
//    }
//}