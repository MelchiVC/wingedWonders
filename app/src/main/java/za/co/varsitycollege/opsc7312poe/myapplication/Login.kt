package za.co.varsitycollege.opsc7312poe.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {
    private lateinit var googleS: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var reg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        //Google()
        val regTextView = findViewById<TextView>(R.id.altRegisterTxt)
        regTextView.setOnClickListener {
            val registrationIntent = Intent(this, Register::class.java)
            startActivity(registrationIntent)
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            performSignin()
        }
    }

    /*private fun Google() {
        val googleSignInHelper = GoogleSignin(this)
        googleSignInHelper.checkAndHandleSignIn()
        googleS = findViewById(R.id.googleButton)

        googleS.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // Call the method in GoogleSignin to handle the Google Sign-In
                googleSignInHelper.performGoogleSignIn()
            }
        })

        googleSignInHelper.checkAndHandleSignIn()
    }*/

    private fun performSignin() {
        val email = findViewById<EditText>(R.id.emailLogin)
        val password = findViewById<EditText>(R.id.passwordLogin)

        val inputEmail = email.text.toString()
        val inputPassword = password.text.toString()

        auth.signInWithEmailAndPassword(inputEmail, inputPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


