package com.codose.chats.views.cashForWork

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.NetworkRepository
import com.codose.chats.network.response.BaseResponse
import com.codose.chats.network.response.campaign.CampaignByOrganizationModel
import com.codose.chats.network.response.progress.SubmitProgressModel
import com.codose.chats.network.response.tasks.GetTasksModel
import com.codose.chats.utils.ApiResponse
import com.codose.chats.utils.handleThrowable
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
        description: String,
        images: ArrayList<File>,
    ) {
        _imageUpload.value = ImageUploadState.Loading
        viewModelScope.launch(exceptionHandler) {
            val response = networkRepository.uploadTaskEvidence(
                beneficiaryId = beneficiaryId,
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
