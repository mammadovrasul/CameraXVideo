package one.smilepay.cameraxvideo

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import one.smilepay.cameraxvideo.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupView()
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .withListener(multiplePermissionsListener)
            .check()
    }

    private fun setupView() {
        binding.buttonRecordVideo.setOnClickListener { onRecordVideoClick() }
    }

    @SuppressLint("MissingPermission")
    private fun bindCamera() {
        binding.cameraView.bindToLifecycle(this)
        binding.cameraView.isPinchToZoomEnabled = true
        // Currently, there's no zoom and camera bound listener supported for CameraView

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun onRecordVideoClick() {
        if (binding.cameraView.isRecording) {
            binding.cameraView.stopRecording()
            onStopVideoRecording()
        } else {
            startVideoRecording()
            file = File.createTempFile(
                "smilepay",
                ".mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
            binding.cameraView.startRecording(
                file,
                ContextCompat.getMainExecutor(this),
                videoSavedCallback
            )
        }
    }

    private fun onPermissionGrant() {
        bindCamera()
    }

    private fun onPermissionDenied() {
        showResultMessage(getString(R.string.permission_denied))
        finish()
    }

    private val multiplePermissionsListener = object : ShortenMultiplePermissionListener() {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
                onPermissionGrant()
            } else {
                onPermissionDenied()
            }
        }
    }

    private val videoSavedCallback = @SuppressLint("UnsafeOptInUsageError")
    object : OnVideoSavedCallback {
        override fun onVideoSaved(outputFileResults: OutputFileResults) {
            Log.d("savedUri", "onVideoSaved: " + file.path)
            showResultMessage(getString(R.string.video_record_success))
        }

        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
            showResultMessage(getString(R.string.video_record_error, message, videoCaptureError))
        }
    }


    private fun startVideoRecording() {
        binding.buttonRecordVideo.setText(R.string.stop_record_video)
    }

    private fun onStopVideoRecording() {
        binding.buttonRecordVideo.setText(R.string.start_record_video)
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
