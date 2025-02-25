package chats.cash.chats_field.views.auth.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.DialogLoginBinding
import chats.cash.chats_field.network.api.SessionManager
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.FRAGMENT_LOGIN_RESULT_KEY
import chats.cash.chats_field.utils.ChatsFieldConstants.LOGIN_BUNDLE_KEY
import chats.cash.chats_field.utils.ChatsFieldConstants.PRIVACY_POLICY
import chats.cash.chats_field.utils.ChatsFieldConstants.TERMS_OF_USE
import chats.cash.chats_field.views.auth.dialog.ForgotDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogLoginBinding
    private val viewModel by viewModel<LoginViewModel>()

    // Handling token bearer for field agent
    private lateinit var sessionManager: SessionManager
    private val preferenceUtil: PreferenceUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogLoginBinding.bind(inflater.inflate(R.layout.dialog_login, container, false))
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            validateFields()
        }

        binding.forgotPasswordText.setOnClickListener {
            openForgot()
        }

        setupObservers()

        binding.terms.makeLinks(
            getString(R.string.terms_and_conditions) to View.OnClickListener {
                openLink(TERMS_OF_USE)
            },
            getString(R.string.privacy_policy) to View.OnClickListener {
                openLink(PRIVACY_POLICY)
            }
        )
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupObservers() = with(binding) {
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it) {
                is LoginViewModel.LoginState.Error -> {
                    loginProgress.root.isVisible = false
                    loginButton.isVisible = true
                    requireContext().toast(it.errorMessage)
                }
                LoginViewModel.LoginState.Loading -> {
                    loginProgress.root.isVisible = true
                    loginButton.isInvisible = true
                }
                is LoginViewModel.LoginState.Success -> {
                    setFragmentResult(FRAGMENT_LOGIN_RESULT_KEY, bundleOf(LOGIN_BUNDLE_KEY to true))
                    dismiss()
                    showToast("Login successful")
                    loginButton.isVisible = true
                }
            }
        }
    }

    private fun validateFields() = with(binding) {
        val email = loginEmailEdit.text.toString()
        val password = loginPasswordEdit.text.toString()
        if (loginEmailEdit.text.isNullOrBlank()) {
            loginEmailLayout.error = "Email is required"
            return
        } else {
            loginEmailLayout.error = ""
        }
        if (loginPasswordEdit.text.isNullOrBlank()) {
            loginPasswordLayout.error = "Password is required"
            return
        } else {
            loginPasswordLayout.error = ""
        }
        val loginBody = LoginBody(email, password)
        viewModel.login(loginBody)
    }

    companion object {
        fun newInstance(): LoginDialog = LoginDialog()
    }

    private fun openForgot(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = ForgotDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }
}
