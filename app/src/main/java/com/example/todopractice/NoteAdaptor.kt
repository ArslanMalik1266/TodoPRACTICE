package com.example.todopractice

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NoteAdaptor(
    private val noteList: MutableList<NoteModel>,
    private val onNoteUpdated: () -> Unit
) :
    RecyclerView.Adapter<NoteAdaptor.NoteViewHolder>() {
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvDesc = itemView.findViewById<TextView>(R.id.tvDescription)
        val checkBoxId = itemView.findViewById<CheckBox>(R.id.cbCompleted)
        val tvTimeStamp = itemView.findViewById<TextView>(R.id.tvTimestamp)
        val todoItemId = itemView.findViewById<LinearLayout>(R.id.todo_itemID)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_item_todo, parent, false)
        return NoteViewHolder(view)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.tvTitle.text = note.title
        holder.tvDesc.text = note.description
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = sdf.format(Date(note.timestamp))
        holder.tvTimeStamp.text = "Created: $formattedDate"

        holder.checkBoxId.setOnCheckedChangeListener(null) // Prevent triggering listener during setup
        holder.checkBoxId.isChecked = note.checked
        updateTodoItemBackground(holder.todoItemId, note.checked)
        holder.checkBoxId.setOnCheckedChangeListener { _, isChecked ->
            note.checked = isChecked
            updateTodoItemBackground(holder.todoItemId, isChecked)
            updateIsCompletedInFirestore(note)
        }
    }

    override fun getItemCount() = noteList.size

    private fun updateIsCompletedInFirestore(note: NoteModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseFirestore.getInstance()

        if (userId != null) {
            database.collection("Users").document(userId)
                .collection("Note").document(note.id)
                .update("checked", note.checked)
                .addOnSuccessListener {
                    onNoteUpdated()
                    Log.d("NoteAdaptor", "Updated isCompleted for note ID: ${note.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("NoteAdaptor", "Error updating isCompleted: ${e.message}")
                }
        }
    }

    private fun updateTodoItemBackground(todoItem: LinearLayout, isChecked: Boolean) {
        val color = if (isChecked) {
            // Color when the item is checked
            todoItem.context.getColor(R.color.light_blue)
        } else {
            // Color when the item is unchecked
            todoItem.context.getColor(R.color.white)
        }
        todoItem.setBackgroundColor(color)
    }

}
