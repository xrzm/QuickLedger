package com.quickledger.app.domain.usecase

import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Long {
        require(transaction.amount > 0) { "金额必须大于0" }
        require(transaction.amount.toString().length <= 12) { "金额不能超过12位" }
        return transactionRepository.insertTransaction(transaction)
    }
}
