package com.quickledger.app

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.*
import com.quickledger.app.domain.usecase.CalculateCycleUseCase
import com.quickledger.app.domain.usecase.ManageCategoryUseCase
import com.quickledger.app.presentation.profile.ProfileViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var appSettingRepository: AppSettingRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var calculateCycleUseCase: CalculateCycleUseCase
    private lateinit var manageCategoryUseCase: ManageCategoryUseCase
    private lateinit var appContext: Context
    private lateinit var viewModel: ProfileViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appSettingRepository = mockk()
        categoryRepository = mockk()
        transactionRepository = mockk()
        calculateCycleUseCase = CalculateCycleUseCase()
        manageCategoryUseCase = mockk()
        appContext = mockk(relaxed = true)

        coEvery { categoryRepository.initializeDefaultCategoriesIfNeeded() } just Runs
        coEvery { appSettingRepository.getSettings() } returns flowOf(
            AppSetting(cycleStartDay = 9, themeMode = ThemeMode.SYSTEM)
        )
        coEvery { categoryRepository.getVisibleCategoriesByType(false) } returns flowOf(emptyList())
        coEvery { categoryRepository.getVisibleCategoriesByType(true) } returns flowOf(emptyList())
        coEvery { appSettingRepository.getSettingsOnce() } returns AppSetting()

        viewModel = ProfileViewModel(
            appSettingRepository, categoryRepository, transactionRepository,
            calculateCycleUseCase, manageCategoryUseCase, appContext
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial cycleStartDay is 9`() = runTest {
        advanceUntilIdle()
        assertEquals(9, viewModel.uiState.value.cycleStartDay)
    }

    @Test
    fun `setCycleStartDay updates value`() = runTest {
        coEvery { appSettingRepository.getSettingsOnce() } returns AppSetting(cycleStartDay = 9)
        coEvery { appSettingRepository.updateSettings(any()) } just Runs

        viewModel.setCycleStartDay(15)
        advanceUntilIdle()
        assertEquals(15, viewModel.uiState.value.cycleStartDay)
    }

    @Test
    fun `setCycleStartDay clamps to 1-28`() = runTest {
        coEvery { appSettingRepository.getSettingsOnce() } returns AppSetting(cycleStartDay = 9)
        coEvery { appSettingRepository.updateSettings(any()) } just Runs

        viewModel.setCycleStartDay(0)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.cycleStartDay)

        viewModel.setCycleStartDay(31)
        advanceUntilIdle()
        assertEquals(28, viewModel.uiState.value.cycleStartDay)
    }

    @Test
    fun `setThemeMode updates mode`() = runTest {
        coEvery { appSettingRepository.getSettingsOnce() } returns AppSetting()
        coEvery { appSettingRepository.updateSettings(any()) } just Runs

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `showAddCategoryDialog opens dialog in add mode`() {
        viewModel.showAddCategoryDialog(true)
        val state = viewModel.uiState.value
        assertTrue(state.showCategoryDialog)
        assertTrue(state.isNewCategory)
        assertTrue(state.isIncomeCategory)
    }

    @Test
    fun `hideCategoryDialog closes dialog`() {
        viewModel.showAddCategoryDialog(false)
        viewModel.hideCategoryDialog()
        assertFalse(viewModel.uiState.value.showCategoryDialog)
    }
}
