package com.quickledger.app

import com.quickledger.app.domain.usecase.CalculateCycleUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CalculateCycleUseCaseTest {

    private lateinit var useCase: CalculateCycleUseCase

    @Before
    fun setUp() {
        useCase = CalculateCycleUseCase()
    }

    @Test
    fun `getCurrentCycle returns valid range`() {
        val cycle = useCase.getCurrentCycle(9)
        assertNotNull(cycle)
        assertTrue(cycle.startTime > 0)
        assertTrue(cycle.endTime > cycle.startTime)
        assertTrue(cycle.label.isNotEmpty())
        assertTrue(cycle.label.contains("~"))
    }

    @Test
    fun `cycle start day 1 returns correct range`() {
        val cycle = useCase.getCurrentCycle(1)
        assertTrue(cycle.label.contains("01") || cycle.label.contains("/1"))
    }

    @Test
    fun `cycle start day 28 returns correct range`() {
        val cycle = useCase.getCurrentCycle(28)
        assertNotNull(cycle)
    }

    @Test
    fun `cycleStartDay is clamped to 1-28`() {
        val cycle0 = useCase.getCurrentCycle(0)
        val cycle29 = useCase.getCurrentCycle(29)
        assertNotNull(cycle0)
        assertNotNull(cycle29)
    }

    @Test
    fun `previousCycle returns earlier cycle`() {
        val current = useCase.getCurrentCycle(9)
        val previous = useCase.getPreviousCycle(9, current.startTime)
        assertTrue(previous.endTime < current.startTime)
        assertTrue(previous.label.contains("~"))
    }

    @Test
    fun `nextCycle returns later cycle`() {
        val current = useCase.getCurrentCycle(9)
        val next = useCase.getNextCycle(9, current.endTime)
        assertTrue(next.startTime > current.endTime)
        assertTrue(next.label.contains("~"))
    }

    @Test
    fun `multiple cycles are consistent`() {
        val current = useCase.getCurrentCycle(15)
        val prev = useCase.getPreviousCycle(15, current.startTime)
        val next = useCase.getNextCycle(15, current.endTime)

        assertTrue(prev.endTime < current.startTime)
        assertTrue(next.startTime > current.endTime)
        val oneDayMs = 24 * 60 * 60 * 1000L
        assertTrue(current.startTime - prev.endTime <= oneDayMs + 1000)
    }

    @Test
    fun `getCycleForDate with specific date works`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cycle = useCase.getCycleForDate(cal, 9)
        assertNotNull(cycle)
        val startCal = Calendar.getInstance().apply { timeInMillis = cycle.startTime }
        assertEquals(2026, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, startCal.get(Calendar.MONTH))
        assertEquals(9, startCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `date before cycle start uses previous month`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 5, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cycle = useCase.getCycleForDate(cal, 9)
        val startCal = Calendar.getInstance().apply { timeInMillis = cycle.startTime }
        assertEquals(2026, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, startCal.get(Calendar.MONTH))
        assertEquals(9, startCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `cycle label format is correct`() {
        val cycle = useCase.getCurrentCycle(1)
        val regex = Regex("\\d{4}/\\d{2}/\\d{2} ~ \\d{4}/\\d{2}/\\d{2}")
        assertTrue("Expected format yyyy/MM/dd ~ yyyy/MM/dd, got: ${cycle.label}",
            regex.matches(cycle.label))
    }
}
