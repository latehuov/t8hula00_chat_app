package chat.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val TAG : String = MainActivity::class.java.name
    private lateinit var messages : ArrayList<Message>
    private lateinit var database : DatabaseReference
    private lateinit var editText: EditText
    private lateinit var rcMessages: RecyclerView
    private lateinit var auth : FirebaseAuth
    private var currentUser : FirebaseUser? = null
    var size  = 0

    override fun onStart() {
        super.onStart()

        currentUser = auth.currentUser
        if(currentUser == null)
            loginDialog()
    }


    fun loginDialog(){
        val builder = AlertDialog.Builder(this)

        with(builder){
            setTitle("Login")

            val linearLayout : LinearLayout = LinearLayout(this@MainActivity)
            val inputEmail = EditText(this@MainActivity)
            val pass = EditText(this@MainActivity)


            linearLayout.orientation = LinearLayout.VERTICAL

            inputEmail.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Email"

            linearLayout.addView(inputEmail)

            pass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            pass.hint = "password"

            linearLayout.addView(pass)
            builder.setView(linearLayout)

            builder.setPositiveButton("OK"){ dialog, which -> login(
                inputEmail.text.toString(),
                pass.text.toString()
            )
            }.show()
        }
    }

    fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful)
                {
                    Log.d(TAG, "loginWithEmail:Success")
                    currentUser = auth.currentUser
                }
                else
                {
                    Log.w(TAG, "LoginWithEmail:Fail", task.exception)
                    Toast.makeText(baseContext, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.MessageText)
        rcMessages = findViewById(R.id.MessageList)

        messages = arrayListOf<Message>()
        database = Firebase.database.reference
        auth = Firebase.auth
        editText.setOnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)
            {
                addMessage()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }


        //firebaserecyclerview bullshittery
        //limit messages to 10 from mesages
        var query = database
            .child("messages")
            .limitToLast(10)


        //create builder options
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Message, MessageHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
                return MessageHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_row, parent, false)
                )
            }

            override fun onBindViewHolder(holder: MessageHolder, position: Int, model: Message) {
                if(position == itemCount-1)
                    size = itemCount
                val message : TextView = holder.itemView.findViewById(R.id.Message)
                val author : TextView = holder.itemView.findViewById(R.id.Author)
                message.text = model.message
                author.text = "by ${model.author} on ${model.time}"
            }

        }

       //database.child("messages").addValueEventListener(messageListener)
        rcMessages.layoutManager  = LinearLayoutManager(this)
        rcMessages.adapter = adapter

    }

    class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem)=
        when(item.itemId){
            R.id.settings -> {
                this.showSettings()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }


    fun showSettings(){
        val intent = Intent(this, Settings::class.java).apply {
            putExtra("currentUser", currentUser)
        }
        startActivity(intent)
    }

    fun addMessage () {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val newMessage = Message(
            //cut off the \n that comes when pressing enter
            editText.text.toString().substring(0..editText.text.length-2), currentUser?.email.toString(), formatter.format(
                LocalDateTime.now()
            )
        )
        val map: MutableMap<String, Any> = HashMap()
        map[size.toString()] = newMessage
        database.child("messages").updateChildren(map)
        messages.add(newMessage)
        editText.setText("")
        closeKeyboard()

        rcMessages.smoothScrollToPosition(rcMessages.adapter!!.itemCount - 1)
    }

    private fun closeKeyboard(){
        val view = this.currentFocus
        if(view != null)
        {
            val item = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            item.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
