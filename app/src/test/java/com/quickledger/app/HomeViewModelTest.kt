package com.quickledger.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.*
import com.quickledger.app.domain.usecase.*
import com.quickledger.app.presentation.home.HomeViewModel
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
class HomeViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var appSettingRepository: AppSettingRepository
    private lateinit var calculateCycleUseCase: CalculateCycleUseCase
    private lateinit var addTransactionUseCase: AddTransactionUseCase
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk()
        categoryRepository = mockk()
        appSettingRepository = mockk()
        calculateCycleUseCase = CalculateCycleUseCase()
        addTransactionUseCase = mockk()

        coEvery { categoryRepository.initializeDefaultCategoriesIfNeeded() } just Runs
        coEvery { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        coEvery { appSettingRepository.getSettings() } returns flowOf(
            AppSetting(cycleStartDay = 9, themeMode = ThemeMode.SYSTEM)
        )
        coEvery { transactionRepository.getTotalByTypeAndDateRange(any(), any(), any()) } returns 0.0
        coEvery { transactionRepository.getTransactionsByDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { categoryRepository.getVisibleCategoriesByType(any()) } returns flowOf(emptyList())
        coEvery { addTransactionUseCase(any()) } returns 1L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `cycle label is populated after init`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.cycleLabel.isNotEmpty())
        assertTrue(state.cycleLabel.contains("~"))
        assertFalse(state.isLoading)
    }

    @Test
    fun `showQuickRecord sets dialog state`() {
        val category = Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B")
        viewModel.showQuickRecord(category, TransactionType.EXPENSE)
        val state = viewModel.uiState.value
        assertTrue(state.showQuickRecord)
        assertEquals(category, state.quickRecordCategory)
    }

    @Test
    fun `hideQuickRecord clears dialog state`() {
        viewModel.showQuickRecord(Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B"), TransactionType.EXPENSE)
        viewModel.hideQuickRecord()
        assertFalse(viewModel.uiState.value.showQuickRecord)
    }

    @Test
    fun `onQuickRecordAmountChange accepts valid input`() {
        viewModel.onQuickRecordAmountChange("35.50")
        assertEquals("35.50", viewModel.uiState.value.quickRecordAmount)
    }

    @Test
    fun `onQuickRecordAmountChange rejects invalid input`() {
        viewModel.onQuickRecordAmountChange("abc")
        assertEquals("", viewModel.uiState.value.quickRecordAmount)
    }

    @Test
    fun `submitQuickRecord does nothing with zero amount`() = runTest {
        advanceUntilIdle()
        viewModel.showQuickRecord(Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B"), TransactionType.EXPENSE)
        viewModel.onQuickRecordAmountChange("0")
        viewModel.submitQuickRecord()
        advanceUntilIdle()
        coVerify(exactly = 0) { addTransactionUseCase(any()) }
    }

    @Test
    fun `submitQuickRecord with valid amount calls use case`() = runTest {
        advanceUntilIdle()
        viewModel.showQuickRecord(Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B"), TransactionType.EXPENSE)
        viewModel.onQuickRecordAmountChange("50")
        viewModel.submitQuickRecord()
        advanceUntilIdle()
        coVerify(exactly = 1) { addTransactionUseCase(any()) }
    }

    @Test
    fun `previousCycle updates cycle correctly`() = runTest {
        advanceUntilIdle()
        val before = viewModel.uiState.value.cycleLabel
        viewModel.previousCycle()
        advanceUntilIdle()
        assertNotEquals(before, viewModel.uiState.value.cycleLabel)
    }

    @Test
    fun `nextCycle updates cycle correctly`() = runTest {
        advanceUntilIdle()
        val before = viewModel.uiState.value.cycleLabel
        viewModel.nextCycle()
        advanceUntilIdle()
        assertNotEquals(before, viewModel.uiState.value.cycleLabel)
    }

    @Test
    fun `state flow is not null`() {
        assertNotNull(viewModel.uiState.value)
    }
}
