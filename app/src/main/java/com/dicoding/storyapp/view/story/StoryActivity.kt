package com.dicoding.storyapp.view.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityStoryBinding
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.utils.getImageUri
import com.dicoding.storyapp.utils.uriToFile
import com.dicoding.storyapp.utils.reduceFileImage
import com.dicoding.storyapp.view.main.MainActivity
import com.dicoding.storyapp.view.main.MainViewModel
import com.dicoding.storyapp.di.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class StoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var lat : Float? = null
    private var lon : Float? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted : Boolean ->
        if(isGranted) {
            getCurrentLocation()
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.title = resources.getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        setupAction()
    }

        private fun setupAction() {
            viewModel.curImage.observe(this) {image ->
                binding.previewImageView.setImageURI(image)
            }

        binding.buttonEnableLocation.setOnCheckedChangeListener {_, isChecked->
            if(isChecked) {
                getCurrentLocation()
            } else {
                lat = null
                lon = null
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener { uploadImage() }
    }

    private fun startCamera() {
        val uri = getImageUri(this)
        if (uri != null) {
            currentImageUri = uri
            launcherIntentCamera.launch(uri)
        } else {
            showToast(getString(R.string.failed_to_get_image_uri))
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }


    private fun showImage() {
        val uri = currentImageUri
        if (uri != null) {
            Log.d("Image URI", "showImage: $uri")
            binding.previewImageView.setImageURI(uri)
        } else {
            Log.d("Image URI", "No image URI found")
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No image selected")
        }
    }

    private fun uploadImage() {
        val uri = currentImageUri
        if (uri != null) {
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "Image file path: ${imageFile.path}")
            val description = binding.edAddDescription.text.toString()

            viewModel.getSession().observe(this) { story ->
                val token = story.token
                viewModel.uploadImage(token, imageFile, description, lat, lon).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> showLoading(true)
                            is Result.Success -> {
                                showToast(result.data.message)
                                showLoading(false)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                            is Result.Error -> {
                                showToast(result.error)
                                showLoading(false)
                            }
                        }
                    }
                }
            }
        } else {
            showToast(getString(R.string.empty_image_warning))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCurrentLocation() {
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if(loc != null) {
                        lat = loc.latitude.toFloat()
                        lon = loc.longitude.toFloat()
                        showToast("Added Location for Story")
                    } else {
                        showToast("Location not found or disabled")
                    }
                }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
