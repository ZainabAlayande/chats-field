package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.Utils.toCountryCode
import chats.cash.chats_field.views.auth.login.LoginDialog
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.core.permissions.COARSE_LOCATION
import chats.cash.chats_field.views.core.permissions.FINE_LOCATION
import chats.cash.chats_field.views.core.permissions.checkPermission
import com.hbb20.CountryCodePicker
import com.hbb20.CountryCodePicker.OnCountryChangeListener
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*


@InternalCoroutinesApi
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val preferenceUtil: PreferenceUtil by inject()
    private val offlineViewModel by activityViewModels<OfflineViewModel>()
    private val organizationId: Int by lazy { preferenceUtil.getNGOId() }
    private val viewModel by sharedViewModel<RegisterViewModel>()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var campaign: ModelCampaign? = null

    private var isNumberValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ChatsFieldConstants.FRAGMENT_CAMPAIGN_RESULT_LISTENER) { _, bundle ->
            campaign = bundle.getParcelable(ChatsFieldConstants.CAMPAIGN_BUNDLE_KEY)
            binding.registerCampaignEdit.setText(campaign?.title)
        }
    }

    val adapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_selectable_list_item,
            listOf("Male", "Female")
        )
    }

    val specialAdapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_selectable_list_item,
            listOf("No", "Yes")
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)


        viewModel.nfc = null
        viewModel.allFinger = null
        viewModel.profileImage = null

        binding.run {
            backBtn.setOnClickListener {
                findNavController().navigateUp()
            }
            registerCampaignEdit.setOnClickListener {
                findNavController().safeNavigate(RegisterFragmentDirections.toCampaignDialog())
            }
            registerCampaignLayout.setEndIconOnClickListener {
                findNavController().safeNavigate(RegisterFragmentDirections.toCampaignDialog())
            }

            registerGenderEdit.setAdapter(adapter)

            observeLoginDone()

            registerSpecialCaseEdit.setAdapter(specialAdapter)

            registerSpecialCaseEdit.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    ninGroup.hide()
                } else {
                   if( ccp.selectedCountryEnglishName.equals(NIGERIA,true)){
                       ninGroup.show()
                   }
                }
            }

            changeAccountText.setOnClickListener {
                openLogin(true)
                doLogout()
            }
            if (organizationId == 0) {
                openLogin()
                changeLoggedOutText()
            } else {
                changeLoggedInText()
            }
            registerDateEdit.setOnClickListener {
                openCalendar()
            }

            registerNextButton.setOnClickListener {
                checkInputs()
            }

            addCCpListeners()
        }
    }

    private fun observeLoginDone() {
        viewModel.login.observe(viewLifecycleOwner) {
            Timber.v("XXXlogin vn reached ")
            when (it) {
                is ApiResponse.Success -> {
                    Timber.v("XXXlogin vn reached ")
                    findNavController().safeNavigate(R.id.action_registerFragment_to_onboardingFragment)
                }
                else -> {}
            }
        }
    }

    private fun changeLoggedInText() = with(binding) {
        loggedInText.text = getString(R.string.logged_in)
        changeAccountText.text =getString(R.string.change_account)
    }

    private fun changeLoggedOutText() = with(binding) {
        loggedInText.text = getString(R.string.not_logged_in)
        changeAccountText.text = getString(R.string.log_in)
    }

    private fun checkInputs() = with(binding) {
        val firstName: String
        val lastName: String
        val email: String
        val phone: String
        val date: String
        val pin: String
        if (organizationId == 0) {
            showToast("Please Log In")
            openLogin()
            return
        }
        if (registerFirstNameEdit.isValid()) {
            firstName = registerFirstNameEdit.text.toString()
            registerFirstNameLayout.error = ""
        } else {
            registerFirstNameLayout.error = "First name is required"
            return
        }
        if (registerLastNameEdit.isValid()) {
            registerLastNameLayout.error = ""
            lastName = registerLastNameEdit.text.toString()
        } else {
            registerLastNameLayout.error = "Last name is required"
            return
        }
        if (registerEmailEdit.text.isNullOrBlank()) {
            email = "${System.currentTimeMillis()}@gmail.com"
            registerEmailLayout.error = ""
        } else {
            if (registerEmailEdit.text.toString().isEmailValid()) {
                registerEmailLayout.error = ""
                email = registerEmailEdit.text.toString()
            } else {
                registerEmailLayout.error = "Invalid email address"
                return@with
            }
        }
        if ( isNumberValid) {
            phoneError.error = ""
            phoneError.hide()
            phone = binding.ccp.fullNumber
            Timber.v(phone)
        } else {
            phoneError.error = "Phone number is required"
            phoneError.show()
            return
        }
        if (inputPinEdit.isValid() && inputPinEdit.text.toString().isValidPin()) {
            inputPinLayout.error = ""
            pin = inputPinEdit.text.toString()
        } else {
            inputPinLayout.error = "A 4-digit PIN is required"
            return
        }
        if (registerDateEdit.isValid()) {
            registerDateLayout.error = ""
            date = registerDateEdit.text.toString()
        } else {
            registerDateLayout.error = "Date of birth is required"
            return
        }

        if (campaign?.id == null) {
            registerCampaignLayout.error = "Select a campaign"
            return
        } else {
            registerCampaignLayout.error = ""
        }
        val gender = registerGenderEdit.text.toString()
        if (gender.isBlank()) {
            registerGenderLayout.error = "Gender is required"
            return
        } else {
            registerGenderLayout.error = ""
        }
        val password: String = Utils.generatePassword()
        Timber.v(password)
        campaign?.let { viewModel.campaign = it.id.toString() }

        val isSpecialCase = registerSpecialCaseEdit.text.toString() == "Yes"
        viewModel.specialCase = isSpecialCase

        if (isSpecialCase) {
            when {
                registerNINEdit.text.isNullOrBlank() -> {
                    showToast("NIN is required.")
                    return
                }
                registerNINEdit.text.toString().length in 10..16 -> {
                    viewModel.nin = registerNINEdit.text.toString()
                }
                else -> {
                    showToast("NIN is not valid")
                    return
                }
            }
        }

        lifecycleScope.launch {
            offlineViewModel.getAllCampaignForms().asLiveData(coroutineContext).observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Timber.d(campaign!!.id.toString())
                    val campaignForm = it.find { it.campaigns[0].id == campaign!!.id }
                    campaignForm?.let {form ->
                        Timber.d(form.questions.toString())
                        offlineViewModel.setCampaignForm(form)
                        findNavController().safeNavigate(RegisterFragmentDirections.toRegisterOptinCampaignFragment2(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phone = phone,
                            password = password,
                            latitude = preferenceUtil.getLatLong().first.toString(),
                            longitude = preferenceUtil.getLatLong().second.toString(),
                            organizationId = organizationId,
                            gender = gender,
                            date = date,
                            pin = pin,
                            campaign = campaign!!
                        ))
                    }?:run{
                        findNavController().safeNavigate(
                            RegisterFragmentDirections.toRegisterVerifyFragment(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phone = phone,
                                password = password,
                                latitude =  preferenceUtil.getLatLong().first.toString(),
                                longitude = preferenceUtil.getLatLong().second.toString(),
                                organizationId = organizationId,
                                gender = gender,
                                date = date,
                                pin = pin,
                                campaign = campaign!!,
                            )
                        )

                    }

                }
                else{
                    viewModel.getAllCampaignForms()
                    findNavController().safeNavigate(
                        RegisterFragmentDirections.toRegisterVerifyFragment(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phone = phone,
                            password = password,
                            latitude =  preferenceUtil.getLatLong().first.toString(),
                            longitude = preferenceUtil.getLatLong().second.toString(),
                            organizationId = organizationId,
                            gender = gender,
                            date = date,
                            pin = pin,
                            campaign = campaign!!,
                        )
                    )
                }

            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
            changeLoggedOutText()
        } else {
            changeLoggedInText()
        }
    }

    private fun openCalendar() {
        val date =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateLabel()
            }
        val datePicker = DatePickerDialog(
            requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        )
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE, "OK", datePicker)
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", datePicker)
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }

    private fun updateLabel() {
        val date = myCalendar.time
        binding.registerDateEdit.setText(date.convertDateToString())
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }

    private fun doLogout() {
        preferenceUtil.clearPreference()
        changeLoggedOutText()
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            offlineViewModel.getAllCampaignForms().asLiveData(coroutineContext).removeObservers(viewLifecycleOwner)
        }
    }

    fun addCCpListeners(){
        binding.ccp.apply{
            setOnCountryChangeListener(OnCountryChangeListener {
                val country = binding.ccp.selectedCountryEnglishName
                if(country.equals(NIGERIA,true)){
                    if(binding.registerSpecialCaseEdit.text.toString() == "Yes") {
                        binding.ninGroup.show()
                    }
                }
                else{
                    if(binding.registerSpecialCaseEdit.text.toString() == "Yes") {
                        binding.ninGroup.hide()
                    }
                }
            })

            registerCarrierNumberEditText(binding.registerPhoneEdit)

            setPhoneNumberValidityChangeListener {isValid ->
                isNumberValid = isValid
                Timber.v(fullNumber)
            }
            binding.registerPhoneEdit.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if(s?.startsWith("0")==true){
                        s.replace(0,1,"")
                    }
                }

            })
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

const val NIGERIA = "NIGERIA"