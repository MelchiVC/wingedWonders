package za.co.varsitycollege.opsc7312poe.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
class GetStarted : AppCompatActivity() {
//For commit
    private lateinit var getStartedButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        //animation function to make the get started button bounce
        getStartedButton = findViewById(R.id.getStarted)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_animation)
        getStartedButton.startAnimation(bounceAnimation)

        getStartedButton.setOnClickListener {
            val intent = Intent(this@GetStarted, Login::class.java)
            startActivity(intent)
        }
    }
}