package br.unisanta.tp6_kotlinfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MainActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val welcome = findViewById<TextView>(R.id.welcomeText)
        welcome.text = "Bem-vindo(a), ${user.displayName ?: user.email}"

       
        findViewById<Button>(R.id.btnChangeName).setOnClickListener {
            showChangeNameDialog { newName ->
                val request = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(request).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        welcome.text = "Bem-vindo(a), $newName"
                        toast("Nome atualizado!")
                    } else {
                        toast("Falha ao atualizar nome.")
                    }
                }
            }
        }

     
        findViewById<Button>(R.id.btnChangePasswordDirect).setOnClickListener {
            showChangePasswordDialog()
        }

        // Sair
        findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        
        findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            val userToDelete = auth.currentUser
            if (userToDelete == null) {
                toast("Nenhum usuário logado.")
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Excluir conta")
                .setMessage("Tem certeza que deseja deletar sua conta? Essa ação não pode ser desfeita.")
                .setPositiveButton("Sim, deletar") { _, _ ->
                    userToDelete.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast("Conta excluída com sucesso.")
                            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                        } else {
                            toast("Não foi possível excluir a conta. Faça login novamente e tente de novo.")
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    
    private fun showChangeNameDialog(onConfirm: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Novo nome"
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle("Alterar nome")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.length < 2) {
                    toast("Informe um nome válido (mín. 2 caracteres).")
                } else onConfirm(text)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

  
    private fun showChangePasswordDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val oldPass = EditText(this).apply {
            hint = "Senha atual"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val newPass = EditText(this).apply {
            hint = "Nova senha (mín. 6)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(oldPass)
        layout.addView(newPass)

        AlertDialog.Builder(this)
            .setTitle("Alterar senha")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val current = oldPass.text.toString()
                val nova = newPass.text.toString()

                if (current.isBlank() || nova.isBlank()) {
                    toast("Preencha os dois campos.")
                    return@setPositiveButton
                }
                if (nova.length < 6) {
                    toast("A nova senha deve ter pelo menos 6 caracteres.")
                    return@setPositiveButton
                }

                val user = auth.currentUser
                val email = user?.email
                if (user == null || email.isNullOrEmpty()) {
                    toast("Não foi possível alterar agora.")
                    return@setPositiveButton
                }

                
                val credential = EmailAuthProvider.getCredential(email, current)
                user.reauthenticate(credential).addOnCompleteListener { reauth ->
                    if (reauth.isSuccessful) {
                        user.updatePassword(nova).addOnCompleteListener { update ->
                            if (update.isSuccessful) {
                                toast("Senha atualizada com sucesso!")
                            } else {
                                toast("Falha ao atualizar senha.")
                            }
                        }
                    } else {
                        toast("Senha atual incorreta.")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toast(msg: String) =
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show()
}
