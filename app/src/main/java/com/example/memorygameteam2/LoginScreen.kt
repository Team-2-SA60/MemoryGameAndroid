package com.example.memorygameteam2

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.memorygameteam2.databinding.ActivityLoginScreenBinding
import com.example.memorygameteam2.model.User
import com.example.memorygameteam2.utils.RetroFitClient
import kotlinx.coroutines.launch

class LoginScreen : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var prefsHelper: PrefsHelper
    private val apiService by lazy { RetroFitClient.api }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsHelper = PrefsHelper(this)

        setupViews()
        checkIfAlreadyLoggedIn()
    }

    private fun setupViews() {
        binding.loginBtn.setOnClickListener {
            val username = binding.User.text.toString()
            val password = binding.password.text.toString()
            if (validateInput(username, password)) {
                loginUser()
            }
        }

    }

    private fun validateInput(
        username: String,
        password: String,
    ): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.User.error = "Username required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.password.error = "Password required"
            isValid = false
        }

        return isValid
    }

    private fun loginUser() {
        val username = binding.User.text?.toString()?.trim() ?: ""
        val password = binding.password.text?.toString()?.trim() ?: ""

        // Validate inputs first
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password")
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true)

                // Create user object with only non-null fields
                val user =
                    User(
                        username = username,
                        password = password,
                        // Other fields remain null
                    )

                val response = apiService.validateUser(user)

                if (response.isSuccessful) {
                    response.body()?.let { loggedInUser ->
                        if (!loggedInUser.username.isNullOrEmpty()) {
                            // Save user data
                            prefsHelper.saveUser(
                                loggedInUser.username!!,
                                loggedInUser.isPremium ?: false,
                                loggedInUser.id.toString(),
                            )
                            navigateToMainScreen()
                        } else {
                            showError("Invalid user data received")
                        }
                    } ?: showError("Empty response from server")
                } else {
                    showError("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.localizedMessage}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun checkIfAlreadyLoggedIn() {
        if (!prefsHelper.getUsername().isNullOrEmpty()) {
            navigateToMainScreen()
        }
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.loginBtn.isEnabled = !show
        if (show) {
            binding.loginBtn.text = "Logging in..."
        } else {
            binding.loginBtn.text = "Login Now"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
