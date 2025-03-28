package com.example.todopractice.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.todopractice.R
import com.example.todopractice.homePage
import com.google.firebase.auth.FirebaseAuth

class loginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailSignIn: EditText
    private lateinit var passSignIn: EditText
    private lateinit var SignInBTN: Button
    private lateinit var textViewSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        auth = FirebaseAuth.getInstance()
        emailSignIn = findViewById(R.id.emailSignIn)
        passSignIn = findViewById(R.id.passSignIn)
        SignInBTN = findViewById(R.id.signInbtn)
        textViewSignIn = findViewById(R.id.newAcoountTextView)


        textViewSignIn.setOnClickListener {
            startActivity(Intent(this, signupPage::class.java))
            finish()
        }


        if (auth.currentUser != null) {
            startActivity(Intent(this, homePage::class.java))
            finish()
        }


        SignInBTN.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = emailSignIn.text.toString().trim()
        val pass = passSignIn.text.toString().trim()


        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailSignIn.error = "Valid email required"
            return
        }


        if (pass.isEmpty() || pass.length < 6) {
            passSignIn.error = "Password must be at least 6 characters"
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, homePage::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}