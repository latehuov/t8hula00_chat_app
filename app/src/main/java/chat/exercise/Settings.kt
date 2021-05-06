package chat.exercise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Settings : AppCompatActivity() {
    private lateinit var textEmail :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)


        textEmail = findViewById(R.id.email)
        supportActionBar?.apply {
            title="Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val email = intent.getParcelableExtra<FirebaseUser>("currentUser")
        textEmail.text =email?.email

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun signOut(view : View){
        FirebaseAuth.getInstance().signOut()
        textEmail.text = ""
    }
}