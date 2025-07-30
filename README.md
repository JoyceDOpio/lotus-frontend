



TO DO:
fun validateActiveTime(start: Time, end: Time): Boolean
When start and end values of active time are both equal to  0:00, the active time will be 24 hours (starting from midnight). Otherwise, the end value cannot be smaller than the start value.

TASK DIAL
Single tap on a task (on the task dial) opens a screen with task details (with options to edit and delete task).
Double tap on a task (on the task dial) activates the task edit mode: the task area changes its color. If the user starts dragging along the task dial edge (starting from within the task area) and surpasses one of the task's boundary angles (start or end), the drag will start to move the surpassed boundary angle. A single tap on the task area updates the task with the new boundaries.

A long press on the task dial followed by a drag along the task dial designates the start and end time for a new task. Once the drag ends, a task creation screen opens with the start time set to the time where the long press occurred, and the end time set to the time where the drag ended.

The active time start and active time end are within the same day. There is no possibility to start the active time on one day and end it on the next day.


Audio Playback
- 16 kHz in 16-bit mono
- STREAM_VOICE_CALL
- MODE_STREAM

















