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
    private lateinit var googleSignInHelper: GoogleSignin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        googleSignInHelper = GoogleSignin(this, applicationContext)

        auth = FirebaseAuth.getInstance()
        val regTextView = findViewById<TextView>(R.id.altRegisterTxt)
        regTextView.setOnClickListener {
            val registrationIntent = Intent(this, Register::class.java)
            startActivity(registrationIntent)
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            performSignin()
        }



        googleS = findViewById(R.id.googleButton)
        googleS.setOnClickListener {
            googleSignInHelper.performGoogleSignIn()
            checkAuthenticationState()
        }


    }

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
    private fun checkAuthenticationState() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in, open the homepage
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Optional: finish the current login activity
        } else {
            Toast.makeText(this, "Invalid account register below", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleSignin.RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(data)
        }
    }
    private fun checkGoogleUserExistsInDatabase(uid: String) {
        googleSignInHelper.checkUserExistsInDatabase(uid,
            onUserExists = {
                // User exists in the database, grant access or perform other actions.
                Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show()
            },
            onUserDoesNotExist = {
                // User does not exist in the database, show an error message.
                Toast.makeText(this, "User does not exist in the database", Toast.LENGTH_SHORT).show()
            }
        )
    }
}


