package com.quickledger.app

import com.quickledger.app.domain.model.Category
import com.quickledger.app.domain.repository.CategoryRepository
import com.quickledger.app.domain.usecase.ManageCategoryUseCase
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ManageCategoryUseCaseTest {

    private lateinit var repository: CategoryRepository
    private lateinit var useCase: ManageCategoryUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ManageCategoryUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `addCategory with valid data succeeds`() = runBlocking {
        coEvery { repository.insertCategory(any()) } returns 1L

        val result = useCase.addCategory("购物", "🛒", "#FF6B6B", false)
        assertEquals(1L, result)
    }

    @Test
    fun `addCategory with blank name throws exception`() = runBlocking {
        try {
            useCase.addCategory("", "🛒", "#FF6B6B", false)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `addCategory with whitespace name throws exception`() = runBlocking {
        try {
            useCase.addCategory("   ", "🛒", "#FF6B6B", false)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `updateCategory with valid data succeeds`() = runBlocking {
        val category = Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B")
        coEvery { repository.updateCategory(any()) } just Runs

        useCase.updateCategory(category)
        coVerify { repository.updateCategory(category) }
    }

    @Test
    fun `updateCategory with blank name throws exception`() = runBlocking {
        val category = Category(id = 1, name = "", icon = "🍔", color = "#FF6B6B")
        try {
            useCase.updateCategory(category)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `deleteCategory delegates to repository`() = runBlocking {
        val category = Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B")
        coEvery { repository.deleteCategory(any()) } just Runs

        useCase.deleteCategory(category)
        coVerify { repository.deleteCategory(category) }
    }

    @Test
    fun `updateSortOrder updates all categories`() = runBlocking {
        val categories = listOf(
            Category(id = 1, name = "餐饮", icon = "🍔", color = "#FF6B6B", sort = 0),
            Category(id = 2, name = "交通", icon = "🚗", color = "#45B7D1", sort = 1),
            Category(id = 3, name = "购物", icon = "🛒", color = "#4ECDC4", sort = 2)
        )
        coEvery { repository.updateSort(any(), any()) } just Runs

        useCase.updateSortOrder(categories)
        coVerify(exactly = 3) { repository.updateSort(any(), any()) }
    }

    @Test
    fun `addCategory for income type works`() = runBlocking {
        coEvery { repository.insertCategory(any()) } returns 1L

        val result = useCase.addCategory("工资", "💰", "#2ECC71", true)
        assertEquals(1L, result)
    }
}
