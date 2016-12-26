package com.example.shuaiz.smartlock

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status


class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val RC_SAVE: Int = 0

    private lateinit var emailLabel: AutoCompleteTextView
    private lateinit var passwordLabel: EditText
    private lateinit var emailSignInButton: Button
    private lateinit var saveCredentialbutton: Button
    private lateinit var requestCredentialbutton: Button

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        googleApiClient?.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e(localClassName, "GoogleApiClient connection suspended")
    }

    override fun onConnected(p0: Bundle?) {
        Log.e(localClassName, "GoogleApiClient connected")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e(localClassName, "GoogleApiClient connection failed")
    }

    private fun initViews() {
        emailLabel = findViewById(R.id.email) as AutoCompleteTextView
        passwordLabel = findViewById(R.id.password) as EditText
        emailSignInButton = findViewById(R.id.email_sign_in_button) as Button
        saveCredentialbutton = findViewById(R.id.save_credential_button) as Button
        requestCredentialbutton = findViewById(R.id.request_credential_button) as Button
    }

    private fun initData() {
        googleApiClient = createGoogleApiClient()
        googleApiClient?.connect()

        emailLabel.setText("520zhangshuai@gmail.com")
        saveCredentialbutton.setOnClickListener { saveCredential() }
        requestCredentialbutton.setOnClickListener { requestCredential() }
    }

    private fun saveCredential() {
        val email = emailLabel.text.toString().trim()
        val password = passwordLabel.text.toString().trim()

        if (email.isNullOrEmpty()) {
            emailLabel.setError("Empty email")
            return
        }
        if (password.isNullOrEmpty()) {
            passwordLabel.setError("Empty pwd")
            return
        }

        val credentialToSave: Credential = Credential.Builder(email)
                .setPassword(password)
                .build()

        Auth.CredentialsApi
                .save(googleApiClient, credentialToSave)
                .setResultCallback {
                    result ->
                    handleCredentialSaveResult(result)
                }
    }

    private fun requestCredential() {
        Auth.CredentialsApi
                .request(googleApiClient, createCredentialRequest())
                .setResultCallback {
                    result ->
                    handleCredentialRequestResult(result)
                }
    }

    private fun handleCredentialSaveResult(result: Status) {
        when {
            result.isSuccess -> {
                // Credentials were saved
                Toast.makeText(this, "Credentials were saved", Toast.LENGTH_SHORT).show()
            }
            result.hasResolution() -> {
                // Try to resolve the save request
                Toast.makeText(this, "Try to resolve the save request", Toast.LENGTH_SHORT).show()
                result.startResolutionForResult(this, RC_SAVE)
            }
            else -> {
                // Could not resolve the request
                Toast.makeText(this, "Could not resolve the request", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCredentialRequestResult(result: CredentialRequestResult) {
        when {
            result.status.isSuccess -> {
                // Handle successful credential requests
                Toast.makeText(this, "Handle successful credential requests: name: " + result.credential.name, Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Handle unsuccessful and incomplete credential requests
                Toast.makeText(this, "Handle unsuccessful and incomplete credential requests", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createCredentialRequest() = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
            .build()

    private fun createGoogleApiClient(): GoogleApiClient? {
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build()
    }
}

