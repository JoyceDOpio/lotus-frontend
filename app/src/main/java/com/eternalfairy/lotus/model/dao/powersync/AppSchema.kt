package com.eternalfairy.lotus.model.dao.powersync

import com.powersync.db.schema.Column
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table

val AppSchema: Schema = Schema(
    listOf(
        Table(
            name = "activities",
            columns = listOf(
                Column.text("created_at"),
                Column.text("user_id"),
                Column.text("date"),
                Column.text("start_time"),
                Column.text("end_time"),
                Column.text("title"),
                Column.text("note"),
                Column.text("main_activity_id")
            )
        ),
        Table(
            name = "days",
            columns = listOf(
                Column.text("created_at"),
                Column.text("user_id"),
                Column.text("date"),
                Column.text("active_time_start"),
                Column.text("active_time_end"),
                Column.text("actual_active_time_start"),
                Column.text("actual_active_time_end"),
                Column.text("note")
            )
        ),
        Table(
            name = "goals",
            columns = listOf(
                Column.text("created_at"),
                Column.text("user_id"),
                Column.text("title"),
                Column.text("priority")
            )
        ),
        Table(
            name = "tasks",
            columns = listOf(
                Column.text("created_at"),
                Column.text("user_id"),
                Column.text("date"),
                Column.text("start_time"),
                Column.text("end_time"),
                Column.text("title"),
                Column.text("description"),
                Column.text("priority"),
                Column.text("pinned")
            )
        ),
        Table(
            name = "user_profiles",
            columns = listOf(
                Column.text("created_at"),
                Column.text("first_name"),
                Column.text("last_name"),
                Column.text("email")
            )
        ),
        Table(
            name = "voice_notes",
            columns = listOf(
                Column.text("created_at"),
                Column.text("user_id"),
                Column.text("uri"),
                Column.text("duration"),
                Column.text("timestamp"),
                Column.text("activity_id")
            )
        )
    )
)