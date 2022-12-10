package chats.cash.chats_field.views.auth.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import chats.cash.chats_field.R
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_CODE_PERMISSIONS
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUIRED_PERMISSIONS
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register_image.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File

@InternalCoroutinesApi
class RegisterImageFragment : Fragment() {

    private val viewModel by sharedViewModel<RegisterViewModel>()
    private var imageBitmap : Bitmap? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.profileImage != null) {
            Glide.with(requireContext())
                .load(File(viewModel.profileImage!!))
                .into(registerImageView)
            registerImageNextBtn.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        registerImageView.setOnClickListener {
            //Navigate to image intent
            requestCameraPermission()
        }
        registerImageBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        //Declared Nav Btn from CaptureImage to ScanFingerPrintScan
        registerImageNextBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun requestCameraPermission() {
        if (allPermissionsGranted()) {
            findNavController().safeNavigate(RegisterImageFragmentDirections.toImageCaptureFragment())
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                findNavController().safeNavigate(RegisterImageFragmentDirections.toImageCaptureFragment())
            } else {
                requireContext().toast("Permissions not granted by the user.")
                findNavController().navigateUp()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            imageBitmap = data?.extras?.get("data") as Bitmap
//            registerImageView.setImageBitmap(imageBitmap)
//            viewModel.profileImage = imageBitmap
//            registerImageNextBtn.setOnClickListener {
//                findNavController().navigateUp()
//            }
//        }
//    }

}