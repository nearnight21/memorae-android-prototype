package com.memorae.prototype.timeline

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.timelineGesture(
    state: TimelineState,
    spacingPx: () -> Float,
): Modifier = pointerInput(state) {
    coroutineScope {
        var settleJob: Job? = null
        var velocityTracker = VelocityTracker()

        detectDragGestures(
            onDragStart = {
                settleJob?.cancel()
                state.beginDrag()
                velocityTracker = VelocityTracker()
            },
            onDrag = { change, dragAmount ->
            velocityTracker.addPosition(change.uptimeMillis, change.position)
                val velocity = velocityTracker.calculateVelocity().x
                if (dragAmount.x != 0f) {
                    state.dragBy(dragAmount.x, spacingPx(), velocity)
                }
                change.consume()
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity().x
                settleJob = launch { state.settle(velocity, spacingPx()) }
            },
            onDragCancel = {
                settleJob = launch { state.settle(0f, spacingPx()) }
            },
        )
    }
}
