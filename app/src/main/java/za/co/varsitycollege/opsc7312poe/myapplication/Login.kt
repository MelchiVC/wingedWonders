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
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//For commit
class Login : AppCompatActivity() {
    private lateinit var googleS: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInHelper: GoogleSignin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        googleSignInHelper = GoogleSignin(this, applicationContext)
        auth = FirebaseAuth.getInstance()
        val regTextView = findViewById<TextView>(R.id.altRegisterTxt)
        regTextView.setOnClickListener {
            val registrationIntent = Intent(this, Map::class.java)
            startActivity(registrationIntent)
        }

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            performSignin()
        }

        googleS = findViewById(R.id.googleButton)
        googleS.setOnClickListener {
            googleSignInHelper.performGoogleSignIn()
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
                    if (user != null) {
                        fetchUserData(user.uid)
                    }
                } else {
                    Toast.makeText(this, "Unsuccessful Login try again Or Sign in below", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAuthenticationState() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Invalid account, register below", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserData(uid: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
            .child(uid)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val email = snapshot.child("email").getValue(String::class.java)
                    val full_name = snapshot.child("full_name").getValue(String::class.java)

                    if (email != null && full_name != null) {
                        val loggedInUser = UserData(
                            uid = uid,
                            full_name = full_name,
                            email = email
                        )

                        // Update the UserDataManager with the fetched data
                        UserDataManager.getInstance().setLoggedInUser(loggedInUser)

                        // Proceed to the Home activity
                        val intent = Intent(this@Login, Home::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Database error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleSignin.RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(data)
        }
    }

}


