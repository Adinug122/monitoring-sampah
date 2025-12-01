package dev.bro.monitoring_sampah

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Ambil ID dari XML
        val inputUsername = findViewById<EditText>(R.id.edtUsername)
        val inputPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Username & password bebas
        val correctUsername = "amalianisa"
        val correctPassword = "123"

        btnLogin.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username == correctUsername && password == correctPassword) {
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Username / Password salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
