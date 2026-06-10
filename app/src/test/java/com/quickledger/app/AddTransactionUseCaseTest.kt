package com.quickledger.app

import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.TransactionRepository
import com.quickledger.app.domain.usecase.AddTransactionUseCase
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AddTransactionUseCaseTest {

    private lateinit var repository: TransactionRepository
    private lateinit var useCase: AddTransactionUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AddTransactionUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `valid transaction is inserted successfully`() = runBlocking {
        val transaction = Transaction(
            amount = 35.0,
            type = TransactionType.EXPENSE,
            categoryId = 1
        )
        coEvery { repository.insertTransaction(any()) } returns 1L

        val result = useCase(transaction)
        assertEquals(1L, result)
        coVerify { repository.insertTransaction(transaction) }
    }

    @Test
    fun `transaction with zero amount throws exception`() = runBlocking {
        val transaction = Transaction(
            amount = 0.0,
            type = TransactionType.EXPENSE,
            categoryId = 1
        )
        try {
            useCase(transaction)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `transaction with negative amount throws exception`() = runBlocking {
        val transaction = Transaction(
            amount = -10.0,
            type = TransactionType.EXPENSE,
            categoryId = 1
        )
        try {
            useCase(transaction)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `income transaction is saved correctly`() = runBlocking {
        val transaction = Transaction(
            amount = 5000.0,
            type = TransactionType.INCOME,
            categoryId = 10
        )
        coEvery { repository.insertTransaction(any()) } returns 2L

        val result = useCase(transaction)
        assertEquals(2L, result)
    }

    @Test
    fun `transaction with remark is inserted`() = runBlocking {
        val transaction = Transaction(
            amount = 50.0,
            type = TransactionType.EXPENSE,
            categoryId = 2,
            remark = "午餐"
        )
        coEvery { repository.insertTransaction(any()) } returns 3L

        val result = useCase(transaction)
        assertEquals(3L, result)
        coVerify { repository.insertTransaction(match { it.remark == "午餐" }) }
    }
}
