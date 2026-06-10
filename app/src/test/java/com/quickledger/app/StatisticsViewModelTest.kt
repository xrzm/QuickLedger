package com.quickledger.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.AppSettingRepository
import com.quickledger.app.domain.usecase.CalculateCycleUseCase
import com.quickledger.app.domain.usecase.GetStatisticsUseCase
import com.quickledger.app.presentation.statistics.StatisticsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var getStatisticsUseCase: GetStatisticsUseCase
    private lateinit var calculateCycleUseCase: CalculateCycleUseCase
    private lateinit var appSettingRepository: AppSettingRepository
    private lateinit var viewModel: StatisticsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getStatisticsUseCase = mockk()
        calculateCycleUseCase = CalculateCycleUseCase()
        appSettingRepository = mockk()

        coEvery { appSettingRepository.getSettingsOnce() } returns AppSetting(cycleStartDay = 9)
        coEvery { getStatisticsUseCase(any(), any()) } returns StatisticsData()

        viewModel = StatisticsViewModel(getStatisticsUseCase, calculateCycleUseCase, appSettingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state completes loading`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadStatistics calls use case`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { getStatisticsUseCase(any(), any()) }
    }

    @Test
    fun `loadStatistics sets isLoading to false`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }
}
