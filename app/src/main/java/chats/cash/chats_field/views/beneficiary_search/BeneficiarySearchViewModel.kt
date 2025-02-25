package chats.cash.chats_field.views.beneficiary_search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.response.beneficiary_onboarding.Beneficiary
import chats.cash.chats_field.utils.ChatsFieldConstants.API_SUCCESS
import chats.cash.chats_field.utils.handleThrowable
import chats.cash.chats_field.utils.toDateString
import chats.cash.chats_field.utils.toTitleCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class BeneficiarySearchViewModel(private val repository: NetworkRepository) : ViewModel() {

    private val _uiState = MutableLiveData<BeneficiarySearchUiState>()
    val uiState: LiveData<BeneficiarySearchUiState> = _uiState

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.value = BeneficiarySearchUiState.Error(throwable.handleThrowable())
    }

    fun loadBeneficiaries(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null,
        nin: String? = null,
    ) {
        _uiState.value = BeneficiarySearchUiState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = repository.getExistingBeneficiaries(
                firstName = firstName,
                lastName = lastName,
                email = email,
                nin = nin,
                phone = phone
            )
            if (response.status == API_SUCCESS && response.code in 200..202) {
                val beneficiaries = response.data
                beneficiaries?.let { beneficiaryItems ->
                    if (beneficiaryItems.isNotEmpty()) {
                        _uiState.postValue(BeneficiarySearchUiState.Success(beneficiaryItems.map { it.mapToUi() }))
                    } else {
                        _uiState.postValue(BeneficiarySearchUiState.EmptyBeneficiaries)
                    }
                }
            } else if (response.code in 401..403) {
                _uiState.value =
                    BeneficiarySearchUiState.Error("Session expired. Log in and try again")
            } else {
                _uiState.value = BeneficiarySearchUiState.Error(response.message)
            }
        }
    }

    private fun Beneficiary.mapToUi(): BeneficiaryUi {
        return BeneficiaryUi(
            dob = dob?.toDateString(),
            email = email,
            firstName = firstName?.trim(),
            gender = gender.toTitleCase(),
            id = id,
            lastName = lastName?.trim(),
            maritalStatus = maritalStatus,
            phone = phone,
            profilePic = profilePic
        )
    }

    sealed class BeneficiarySearchUiState {
        object Loading : BeneficiarySearchUiState()
        object EmptyBeneficiaries : BeneficiarySearchUiState()
        data class Error(val errorMessage: String?) : BeneficiarySearchUiState()
        data class Success(val beneficiaries: List<BeneficiaryUi>) : BeneficiarySearchUiState()
    }
}
