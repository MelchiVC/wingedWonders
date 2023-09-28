package za.co.varsitycollege.opsc7312poe.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.Serializable

class UserDataViewModel : ViewModel() {
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData>
        get() = _userData
    fun setUserData(uid: String, name: String?, email: String?) {
        val user = UserData(uid, name, email)
        _userData.value = user
    }
}