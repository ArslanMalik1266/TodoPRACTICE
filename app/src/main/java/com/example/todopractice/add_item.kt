package com.example.todopractice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class add_item : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var txtViewTitle: EditText
    private lateinit var txtViewDescription: EditText
    private lateinit var saveBtn: Button
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_item)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        txtViewTitle = findViewById(R.id.taskTitleInput)
        txtViewDescription = findViewById(R.id.taskDescriptionInput)
        saveBtn = findViewById(R.id.addTaskButton)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        saveBtn.setOnClickListener {
            val title = txtViewTitle.text.toString().trim()
            val description = txtViewDescription.text.toString().trim()

            if (title.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val newNoteRef = db.collection("Users").document(userId).collection("Note").document()

                val newNote = NoteModel(
                    id = newNoteRef.id,
                    title = title,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )

                newNoteRef.set(newNote)
                    .addOnSuccessListener {
                        val intent = Intent()
                        intent.putExtra("newNote", newNote)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error occurred while saving the note!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}