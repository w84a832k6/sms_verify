package com.example.smsverify.viewModels

import androidx.lifecycle.*
import com.example.smsverify.database.Bank
import com.example.smsverify.repositories.BankRepository
import kotlinx.coroutines.launch

class BankViewModel (private val repository: BankRepository): ViewModel() {
    val allBank: LiveData<List<Bank>> = repository.getAll.asLiveData()

    fun insertAll(banks: List<Bank>) = viewModelScope.launch {
        repository.insertAll(banks)
    }
}

class BankViewModelFactory(private val repository: BankRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}