package com.example.shuaiz.smartlock

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status


class LoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object RCCode {
        val RC_SAVE: Int = 88
        val RC_REQUEST: Int = 89
        val RC_HINT: Int = 90
    }

    private lateinit var emailLabel: AutoCompleteTextView
    private lateinit var passwordLabel: EditText
    private lateinit var emailSignInButton: Button
    private lateinit var saveCredentialbutton: Button
    private lateinit var requestCredentialbutton: Button
    private lateinit var retrieveHintButton: Button

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
        when (requestCode) {
            RC_SAVE -> {
                showToast("On Activity Result, saved")
            }
            RC_REQUEST -> {
                showToast("On Activity Result, request")
            }
            RC_HINT -> {
                showToast("On Activity Result, hint")
            }
        }
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            showToast("On Activity Result, canceled")
            return
        }
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
        retrieveHintButton = findViewById(R.id.retrieve_hint_button) as Button
    }

    private fun initData() {
        googleApiClient = createGoogleApiClient()
        googleApiClient?.connect()

        emailLabel.setText("520zhangshuai@gmail.com")
        saveCredentialbutton.setOnClickListener { saveCredential() }
        requestCredentialbutton.setOnClickListener { requestCredential() }
        retrieveHintButton.setOnClickListener { retrieveHint() }
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

        googleApiClient?.isConnected?.let {
            val credentialToSave: Credential = Credential.Builder(email)
                    .setPassword(password)
                    .build()

            Auth.CredentialsApi
                    .save(googleApiClient, credentialToSave)
                    .setResultCallback {
                        handleCredentialSaveResult(it)
                    }
        }
    }

    private fun requestCredential() {
        Auth.CredentialsApi
                .request(googleApiClient, createCredentialRequest())
                .setResultCallback {
                    handleCredentialRequestResult(it.status)
                }
    }

    private fun retrieveHint() {
        val hintRequest = HintRequest.Builder()
                .setHintPickerConfig(CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setEmailAddressIdentifierSupported(true)
//                .setAccountTypes(IdentityProviders.GOOGLE)
                .build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, RC_HINT, null, 0, 0, 0)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(localClassName, "Could not start hint picker Intent", e)
        }
    }

    private fun handleCredentialSaveResult(result: Status) {
        when {
            result.isSuccess -> {
                showToast("Credentials were saved")
            }
            result.hasResolution() -> {
                showToast("Try to resolve the save request")
                try {
                    result.startResolutionForResult(this, RC_SAVE)
                } catch (e: IntentSender.SendIntentException) {
                    // Could not resolve the request
                }
            }
            else -> {
                showToast("Could not resolve the request")
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun handleCredentialRequestResult(status: Status) {
        when {
            status.isSuccess -> {
                showToast("Handle successful credential requests")
            }
            status.hasResolution() -> {
                showToast("Handle successful with resolution")
                try {
                    status.status.startResolutionForResult(this, RC_REQUEST)
                } catch (e: IntentSender.SendIntentException) {
                    // Could not resolve the request
                }
            }
            else -> {
                showToast("Handle unsuccessful and incomplete credential requests")
            }
        }
    }

    private fun createCredentialRequest(): CredentialRequest = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
            .build()

    private fun createGoogleApiClient(): GoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .enableAutoManage(this, this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
}

