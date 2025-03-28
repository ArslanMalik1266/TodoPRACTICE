package com.example.todopractice

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todopractice.auth.loginPage
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class homePage : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdaptor: NoteAdaptor
    private val db = FirebaseFirestore.getInstance()
    private lateinit var logoutBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var checked: CheckBox
    private lateinit var fabBtn: ExtendedFloatingActionButton
    private val notes = mutableListOf<NoteModel>()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus){
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        noteAdaptor = NoteAdaptor(notes) { updateTaskStats() }
         auth = FirebaseAuth.getInstance()
        navigationView = findViewById(R.id.nav_view)
        navigationView.post {
            fetchTimeAndSetGreeting()
        }


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigate to homePage
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        progressBar = findViewById(R.id.progressBar)


        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        val drawerIcon = toolbar.navigationIcon
        drawerIcon?.setTint(ContextCompat.getColor(this, R.color.blue_text))
        toolbar.setTitleTextAppearance(this, R.style.BoldTextStyle)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        val headerView = navigationView.getHeaderView(0)
        val headerEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        val headerName = headerView.findViewById<TextView>(R.id.nav_header_name)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "No Email"
            val name = email.substringBefore("@")
            headerEmail.text = email
            headerName.text = name
        } else {
            headerEmail.text = "Guest"
            headerName.text = "User"
        }

        logoutBtn = findViewById(R.id.logoutbtn)
        fabBtn = findViewById(R.id.fabAdd)
        recyclerView = findViewById(R.id.rvTodos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdaptor = NoteAdaptor(notes) { updateTaskStats() }
        recyclerView.adapter = noteAdaptor
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        logoutBtn.setOnClickListener {
            confirmLogout()
        }
        fabBtn.setOnClickListener {
            val intent = Intent(this, add_item::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }
        fetchTaskFromFireStore()

    }

    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
//            val noteToDelete = notes[position]
//            noteAdaptor.deleteItem(position)

            auth.currentUser?.let { user ->
                val removedNote = notes[position]
                try {
                    db.collection("Users")
                        .document(user.uid)
                        .collection("Note")
                        .document(removedNote.id)
                        .delete()
                        .addOnSuccessListener {
                            notes.removeAt(position)
                            noteAdaptor.notifyItemRemoved(position)
                            updateTaskStats()
                            Toast.makeText(
                                this@homePage,
                                "Note deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            noteAdaptor.notifyItemChanged(position)
                            Toast.makeText(
                                this@homePage,
                                "Error deleting note: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } catch (e: Exception) {
                    noteAdaptor.notifyItemChanged(position)
                    Toast.makeText(
                        this@homePage,
                        "An unexpected error occurred: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } ?: run {
                noteAdaptor.notifyItemChanged(position)
                Toast.makeText(this@homePage, "Error! try again.", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val icon = ContextCompat.getDrawable(this@homePage, R.drawable.ic_delete)
            val background = ColorDrawable(ContextCompat.getColor(this@homePage, R.color.red))
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)
            val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

    private fun fetchTaskFromFireStore() {
        progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("Users")
            .document(userId).collection("Note")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notes.clear()
                for (document in documents) {
                    val note = document.toObject(NoteModel::class.java)
                    note.id = document.id
                    notes.add(note)
                }
                updateTaskStats()
                progressBar.visibility = View.GONE
                noteAdaptor.notifyDataSetChanged()

            }.addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun confirmLogout() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(this, loginPage::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            val newNote = data?.getSerializableExtra("newNote") as? NoteModel
            newNote?.let {
                addNewNoteToList(it)
            }
        }
    }


    private fun addNewNoteToList(note: NoteModel) {
        notes.add(0, note)
        noteAdaptor.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
        updateTaskStats()
    }

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1
    }
    private fun updateTaskStats() {
        val total = notes.size
        val completed = notes.count { it.checked }
        val pending = total - completed

        findViewById<TextView>(R.id.totalTasks).text = " $total"
        findViewById<TextView>(R.id.completedTasks).text = " $completed"
        findViewById<TextView>(R.id.pendingTasks).text = " $pending"
    }
    private fun fetchTimeAndSetGreeting() {
        val headerView = navigationView.getHeaderView(0) ?: return
        val headerNameTextView = headerView.findViewById<TextView>(R.id.nav_header_name)

        val userId = auth.currentUser?.uid
        val greetingTextView = findViewById<TextView>(R.id.greetingText)

        if (userId == null) {
            greetingTextView.text = "Hello, Guest!"
            return
        }
        val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentTime) {
            in 0..4 -> "Good Night"
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        val userName = headerNameTextView.text.toString().trim()
        greetingTextView.text = "$greeting, $userName !"
    }
}