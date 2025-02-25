package chats.cash.chats_field.views.cashForWork

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.NetworkRepository
import chats.cash.chats_field.network.response.BaseResponse
import chats.cash.chats_field.network.response.campaign.CampaignByOrganizationModel
import chats.cash.chats_field.network.response.progress.SubmitProgressModel
import chats.cash.chats_field.network.response.tasks.GetTasksModel
import chats.cash.chats_field.utils.ApiResponse
import chats.cash.chats_field.utils.handleThrowable
import chats.cash.chats_field.utils.location.UserLocation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CashForWorkViewModel(private val networkRepository: NetworkRepository) : ViewModel() {
    val cashForWorks = MutableLiveData<ApiResponse<CampaignByOrganizationModel>>()
    val tasks = MutableLiveData<ApiResponse<GetTasksModel>>()
    val taskOperation = MutableLiveData<ApiResponse<SubmitProgressModel>>()
    val imageList = MutableLiveData<ArrayList<Bitmap>>(arrayListOf())
    val tempImageList = MutableLiveData<ArrayList<Uri>>(arrayListOf())

    private val _cashForWorkCampaign = MutableLiveData<List<ModelCampaign>>()
    val cashForWorkCampaign: LiveData<List<ModelCampaign>> = _cashForWorkCampaign

    private val _imageUpload = MutableLiveData<ImageUploadState>()
    val imageUpload: LiveData<ImageUploadState> = _imageUpload

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        taskOperation.value = ApiResponse.Failure(throwable.handleThrowable())
        _imageUpload.value = ImageUploadState.Error(throwable.handleThrowable())
    }

    fun getCashForWorks() {
        cashForWorks.value = ApiResponse.Loading()
        viewModelScope.launch(exceptionHandler) {
            val data = withContext(Dispatchers.IO) {
                networkRepository.getAllCashForWorkCampaigns()
            }
            _cashForWorkCampaign.value = data
        }
    }

    fun getTask(campaignId: String) {
        tasks.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.getTasks(campaignId)
                tasks.postValue(data)
            }
        }
    }

    fun postTaskImages(
        beneficiaryId: Int,
        taskAssignmentId: String,
        description: String,
        location:UserLocation,
        images: ArrayList<File>,
    ) {
        _imageUpload.value = ImageUploadState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = networkRepository.uploadTaskEvidence(
                beneficiaryId = beneficiaryId,
                location = location,
                taskAssignmentId = taskAssignmentId,
                comment = description,
                uploads = images
            )
            _imageUpload.postValue(ImageUploadState.Success(response))
        }
    }

    fun postTaskCompleted(taskId: String, userId: String) {
        taskOperation.value = ApiResponse.Loading()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = networkRepository.postTaskCompleted(taskId, userId)
                taskOperation.postValue(data)
            }
        }
    }

    sealed class ImageUploadState {
        object Loading : ImageUploadState()
        data class Success(val data: BaseResponse<Any>) : ImageUploadState()
        data class Error(val errorMessage: String?) : ImageUploadState()
    }
}
