package com.example.todopractice.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.todopractice.R
import com.example.todopractice.homePage
import com.google.firebase.auth.FirebaseAuth

class signupPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailSignUp: EditText
    private lateinit var passSignUp: EditText
    private lateinit var ConfirmPassSignUp: EditText
    private lateinit var SignUpBTN: Button
    private lateinit var backBTN: ImageView
    private lateinit var textViewSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_page)

        auth = FirebaseAuth.getInstance()
        emailSignUp = findViewById(R.id.emailSignUp)
        passSignUp = findViewById(R.id.passSignUp)
        ConfirmPassSignUp = findViewById(R.id.confirmPassSignUp)
        SignUpBTN = findViewById(R.id.signUpbtn)
        backBTN = findViewById(R.id.signupBackBTN)
        textViewSignUp = findViewById(R.id.alreadyAccID)

        // Handle "Already have an account?" text click
        textViewSignUp.setOnClickListener {
            startActivity(Intent(this, loginPage::class.java))
            finish()
        }

        // Handle back button click
        backBTN.setOnClickListener {
            startActivity(Intent(this, loginPage::class.java))
            finish()
        }

        // Handle sign-up button click
        SignUpBTN.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val email = emailSignUp.text.toString().trim()
        val pass = passSignUp.text.toString().trim()
        val confirmPass = ConfirmPassSignUp.text.toString().trim()

        // Validate email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailSignUp.error = "Valid email required"
            return
        }

        // Validate password length
        if (pass.length < 6) {
            passSignUp.error = "Password must be at least 6 characters"
            return
        }

        // Validate password match
        if (pass != confirmPass) {
            ConfirmPassSignUp.error = "Passwords do not match"
            return
        }

        // Create Firebase user
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, homePage::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}