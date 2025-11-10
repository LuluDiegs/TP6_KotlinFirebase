package br.unisanta.tp6_kotlinfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var signInLaunched = false

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        signInLaunched = false
        val response = IdpResponse.fromResultIntent(result.data)
        if (result.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            if (response == null) {
                toast("Login cancelado.")
            } else {
                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK -> toast("Sem internet.")
                    ErrorCodes.UNKNOWN_ERROR -> toast("Erro desconhecido.")
                    else -> toast(response.error?.message ?: "Falha de autenticação.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        if (!signInLaunched) launchSignIn()

        findViewById<Button>(R.id.btnSignIn).setOnClickListener { launchSignIn() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun launchSignIn() {
        if (signInLaunched) return
        signInLaunched = true

        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.Theme_TP6_KotlinFirebase)
            .build()

        signInLauncher.launch(intent)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
