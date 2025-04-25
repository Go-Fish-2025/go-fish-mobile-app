package com.garlicbread.gofish

import android.annotation.SuppressLint
import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.garlicbread.gofish.data.FirebaseTokenRequest
import com.garlicbread.gofish.data.JwtResponse
import com.garlicbread.gofish.retrofit.RetrofitInstance
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.util.Utils.Companion.decryptJwt
import com.garlicbread.gofish.util.Utils.Companion.encryptAndStoreJwt
import com.garlicbread.gofish.util.Utils.Companion.generateKeyIfNeeded
import com.garlicbread.gofish.util.Utils.Companion.isJwtExpired
import com.garlicbread.gofish.util.Utils.Companion.logout
import com.garlicbread.gofish.util.Utils.Companion.saveJwtTimestamp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        RetrofitInstance.init(this)

        val token = decryptJwt(this)
        if (token != null) {
            if (!isJwtExpired(this)) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                lifecycleScope.launch(Dispatchers.IO) {
                    GoFishDatabase.getDatabase(this@LoginActivity).clearAllTables()
                }
                logout(this, false)
            }
        }

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        findViewById<Button>(R.id.loginButton).setOnClickListener { view ->
            val email = findViewById<EditText>(R.id.emailInput).text.toString()
            val password = findViewById<EditText>(R.id.passwordInput).text.toString()
            findViewById<TextView>(R.id.verification).isVisible = false
            loginWithEmailPassword(email, password)

            findViewById<EditText>(R.id.emailInput).clearFocus()
            findViewById<EditText>(R.id.passwordInput).clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        findViewById<SignInButton>(R.id.googleSignInButton).setOnClickListener {
            signInWithGoogle()
        }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        emailInput.addTextChangedListener(textWatcher)
        passwordInput.addTextChangedListener(textWatcher)
    }

    @SuppressLint("CutPasteId")
    private fun updateLoginButtonState() {
        val email = findViewById<EditText>(R.id.emailInput).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordInput).text.toString().trim()
        findViewById<Button>(R.id.loginButton).isEnabled = email.isNotEmpty() && password.isNotEmpty()
        findViewById<Button>(R.id.loginButton).alpha = if (email.isNotEmpty() && password.isNotEmpty()) 1f else 0.4f
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    verifyEmail()
                } else {
                    signUp(task, email, password)
                }
            }
    }

    private fun signUp(task: Task<AuthResult>, email: String, password: String) {
        when (task.exception) {
            is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { createTask ->
                        if (createTask.isSuccessful) {
                            verifyEmail()
                        } else if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Account already exists. Try logging in with Google.", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this, "Login / Signup failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else -> {
                Toast.makeText(this, "Login failed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verifyEmail() {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification()
                .addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        findViewById<TextView>(R.id.verification).isVisible = true
                    } else {
                        auth.signOut()
                        Toast.makeText(this, "Failed to send verification email. Try again later.", Toast.LENGTH_LONG).show()
                    }
                }
        } else if (user != null) {
            fetchToken()
        }
        else {
            Toast.makeText(this, "Login failed!", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchToken() {
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val idToken = result.token ?: return@addOnSuccessListener
                val request = FirebaseTokenRequest(firebase_token = idToken)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.loginWithFirebaseToken(request)
                        if (response.isSuccessful) {
                            val jwt = (response.body() as JwtResponse).jwt
                            generateKeyIfNeeded()
                            encryptAndStoreJwt(this@LoginActivity, jwt)
                            saveJwtTimestamp(this@LoginActivity)
                            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            auth.signOut()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@LoginActivity, "Login failed.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (_: Exception) {
                        auth.signOut()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Network error. Try again later.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }

    private val legacyGoogleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        if (task.isSuccessful) {
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            val exception = task.exception
            if (exception is ApiException && exception.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Sign-In failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val result = withContext(Dispatchers.Main) {
                        credentialManager.getCredential(
                            request = credentialRequest,
                            context = this@LoginActivity
                        )
                    }

                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        firebaseAuthWithGoogle(idToken)

                    } else {
                        Toast.makeText(this@LoginActivity, "Unexpected credential type: ${credential::class.java.simpleName}", Toast.LENGTH_SHORT).show()
                    }

                } catch (_: GetCredentialCancellationException) {
                    Toast.makeText(this@LoginActivity, "Sign-in cancelled by user", Toast.LENGTH_SHORT).show()
                } catch (e: GetCredentialException) {
                    Toast.makeText(this@LoginActivity, "Credential error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
                val signInIntent = googleSignInClient.signInIntent
                legacyGoogleSignInLauncher.launch(signInIntent)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    fetchToken()
                } else {
                    Toast.makeText(this, "Google Auth failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
