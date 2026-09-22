package com.eternalfairy.lotus.data.test

import android.util.Log
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Task
import com.eternalfairy.lotus.domain.repository.ITaskRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

class MockTaskRepository: ITaskRepository {
    var mockTasks = mutableListOf(
        Task(
            id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().toString()),
            title = "predefined task",
            description = "predefined description",
            startTime = LocalTime(8, 0),
            endTime = LocalTime(10, 0),
            priority = null,
            pinned = false
        ),
        Task(
            id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().toString()),
            title = "predefined task 2",
            description = "predefined description",
            startTime = LocalTime(11, 15),
            endTime = LocalTime(12, 34),
            priority = null,
            pinned = false
        ),
        Task(
            id = Uuid.Companion.parse("b05c211a-a2f8-4d60-b9a7-62551f95a1e4"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(1).toString()),
            title = "predefined task",
            description = "predefined description",
            startTime = LocalTime(11, 30),
            endTime = LocalTime(12, 45),
            priority = null,
            pinned = false
        ),
        Task(
            id = Uuid.Companion.parse("504ab1f4-ca8f-4065-9fbb-a1ca85a8bd85"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(-1).toString()),
            title = "predefined task",
            description = "predefined description",
            startTime = LocalTime(13, 50),
            endTime = LocalTime(15, 20),
            priority = null,
            pinned = false
        ),
        Task(
            id = Uuid.Companion.parse("494623db-021f-4c72-ae05-3a78f7a077f9"),
            date = null,
            title = "predefined to-do task",
            description = "predefined description",
            startTime = null,
            endTime = null,
            priority = 1,
            pinned = false
        )
    )

    override suspend fun addTask(task: Task): ApiResponse<Unit> {
        return try {
            mockTasks += task
            Log.i("MockTaskRepository", "addTask() mockTasks: $mockTasks")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun deleteTask(id: Uuid): ApiResponse<Unit> {
        return try {
            for (task in mockTasks) {
                if (task.id == id) {
                    mockTasks.remove(task)
                }
            }
            Log.i("MockTaskRepository", "deleteTask() mockTasks: $mockTasks")
            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editTask(task: Task): ApiResponse<Unit> {
        return try {
            mockTasks.forEach { taskToBeEdited ->
                if (taskToBeEdited.id == task.id) {
                    mockTasks.remove(taskToBeEdited)
                    mockTasks.add(task)
                }
            }
            Log.i("MockTaskRepository", "editTask() mockTasks: $mockTasks")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getLastPriority(): ApiResponse<Int> {
        return try {
            var priority = 0
            mockTasks.forEach { task ->
                task.priority?.let {
                    if (it > priority) priority = it
                }
            }

            ApiResponse.Success(priority)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getTask(id: Uuid): ApiResponse<Task> {
        return try {
            val task = mockTasks.find { t -> t.id == id }

            ApiResponse.Success(task)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getTasksPerDay(date: LocalDate): ApiResponse<List<Task>> {
        return try {

            var tasks = emptyList<Task>()
            mockTasks.forEach { task ->
                if (task.date == date) tasks += task
            }

            ApiResponse.Success(tasks)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getToDoTasks(): ApiResponse<List<Task>> {
        return try {
            var tasks = emptyList<Task>()
            mockTasks.forEach { task ->
                if (task.date == null) tasks += task
            }
            Log.i("MockTaskRepository", "getToDoTasks() tasks: $tasks")

            ApiResponse.Success(tasks.sortedBy { it.priority })
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}