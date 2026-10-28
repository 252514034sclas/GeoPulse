package com.geopulse.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DraggableBottomDockGestureTest {

    private val itemWidthPx = 200f

    @Test
    fun touch_without_drag_does_not_navigate() {
        // Tapping or touching without drag must NOT navigate
        for (page in 0..3) {
            val result = calculateDragTargetPage(
                startPage = page,
                totalDragPx = 0f,
                itemWidthPx = itemWidthPx,
                velocityX = 0f
            )
            assertEquals("Page $page should not change on touch without drag", page, result)
        }
    }

    @Test
    fun short_drag_below_threshold_returns_to_current_destination() {
        // Drag distance < 35% of item width should settle back to current page
        val shortDragLeft = -50f // 25% of item width
        val resultLeft = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = shortDragLeft,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Short drag left should return to startPage 0", 0, resultLeft)

        val shortDragRight = 50f // 25% of item width
        val resultRight = calculateDragTargetPage(
            startPage = 2,
            totalDragPx = shortDragRight,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Short drag right should return to startPage 2", 2, resultRight)
    }

    @Test
    fun long_drag_advances_to_next_destination() {
        // Dragging left (> 50% item width) advances forward
        val longDragLeft = -120f // 60% of item width
        val result = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = longDragLeft,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Long drag left from 0 should advance to 1 (Map)", 1, result)

        val resultFrom1 = calculateDragTargetPage(
            startPage = 1,
            totalDragPx = longDragLeft,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Long drag left from 1 should advance to 2 (Activity)", 2, resultFrom1)
    }

    @Test
    fun long_drag_advances_to_previous_destination() {
        // Dragging right (> 50% item width) advances backward
        val longDragRight = 120f // 60% of item width
        val result = calculateDragTargetPage(
            startPage = 2,
            totalDragPx = longDragRight,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Long drag right from 2 should move to 1 (Map)", 1, result)

        val resultToHome = calculateDragTargetPage(
            startPage = 1,
            totalDragPx = longDragRight,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Long drag right from 1 should move to 0 (Home)", 0, resultToHome)
    }

    @Test
    fun multi_destination_drag_advances_across_multiple_tabs() {
        // Dragging across multiple item widths
        val multiDragLeft = -380f // 1.9 items
        val result = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = multiDragLeft,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Long continuous drag across 2 tabs should land on Tab 2 (Activity)", 2, result)

        val multiDragAllWay = -580f // 2.9 items
        val resultAllWay = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = multiDragAllWay,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Full drag across 3 tabs should land on Tab 3 (Profile)", 3, resultAllWay)
    }

    @Test
    fun clamped_at_first_and_last_destinations_no_wraparound() {
        // Dragging right at first destination (0: Home) must clamp at 0
        val overDragRight = 400f
        val resultHome = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = overDragRight,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Over-dragging right at Tab 0 must remain 0 (no negative wrap)", 0, resultHome)

        // Dragging left at last destination (3: Profile) must clamp at 3
        val overDragLeft = -400f
        val resultProfile = calculateDragTargetPage(
            startPage = 3,
            totalDragPx = overDragLeft,
            itemWidthPx = itemWidthPx,
            velocityX = 0f
        )
        assertEquals("Over-dragging left at Tab 3 must remain 3 (no forward wrap)", 3, resultProfile)
    }

    @Test
    fun fast_flick_left_advances_immediately() {
        // Even with a short drag distance, a quick flick (> 450 px/s) advances to next destination
        val shortDrag = -20f // only 10% item width
        val flickVelocity = -800f // fast flick left
        val result = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = shortDrag,
            itemWidthPx = itemWidthPx,
            velocityX = flickVelocity
        )
        assertEquals("Fast flick left should advance to Tab 1 despite short distance", 1, result)
    }

    @Test
    fun fast_flick_right_advances_immediately() {
        // Quick flick right (> 450 px/s) advances to previous destination
        val shortDrag = 20f // only 10% item width
        val flickVelocity = 800f // fast flick right
        val result = calculateDragTargetPage(
            startPage = 2,
            totalDragPx = shortDrag,
            itemWidthPx = itemWidthPx,
            velocityX = flickVelocity
        )
        assertEquals("Fast flick right should advance to Tab 1 despite short distance", 1, result)
    }

    @Test
    fun fast_flick_clamped_at_boundaries() {
        // Flick right at Tab 0 must not underflow
        val resultMin = calculateDragTargetPage(
            startPage = 0,
            totalDragPx = 50f,
            itemWidthPx = itemWidthPx,
            velocityX = 1200f
        )
        assertEquals("Flick right at Tab 0 must stay 0", 0, resultMin)

        // Flick left at Tab 3 must not overflow
        val resultMax = calculateDragTargetPage(
            startPage = 3,
            totalDragPx = -50f,
            itemWidthPx = itemWidthPx,
            velocityX = -1200f
        )
        assertEquals("Flick left at Tab 3 must stay 3", 3, resultMax)
    }
}
