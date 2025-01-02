package uk.ac.tees.mad.myholidays.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    init {
        loadUserProfile()
    }

    fun updateUserProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading

                firestore.collection("users")
                    .document(auth.currentUser?.uid ?: "")
                    .set(mapOf("name" to name, "email" to email))
                    .await()
                _userState.value = UserState.Success(UserProfile(name, email))

            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                // Save URI to SharedPreferences
                getApplication<Application>().getSharedPreferences(
                    "user_prefs",
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .putString("profile_image", uri.toString())
                    .apply()

                _profileImageUri.value = uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            firestore.collection("users")
                .document(auth.currentUser?.uid ?: "")
                .get()
                .addOnSuccessListener {
                    val name = it.getString("name") ?: ""
                    val email = it.getString("email") ?: ""

                    val prefs = getApplication<Application>()
                        .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

                    val imageUri = prefs.getString("profile_image", null)?.let { Uri.parse(it) }
                    _profileImageUri.value = imageUri

                    _userState.value = UserState.Success(UserProfile(name, email))
                }.addOnFailureListener {
                    _userState.value = UserState.Error(it.message ?: "Unknown error")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            getApplication<Application>()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            _userState.value = UserState.Initial
        }
    }
}

data class UserProfile(
    val name: String,
    val email: String,
)

sealed class UserState {
    object Initial : UserState()
    object Loading : UserState()
    data class Success(val profile: UserProfile) : UserState()
    data class Error(val message: String) : UserState()
}