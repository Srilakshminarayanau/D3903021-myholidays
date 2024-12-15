package uk.ac.tees.mad.myholidays

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import uk.ac.tees.mad.myholidays.screens.AuthScreen
import uk.ac.tees.mad.myholidays.screens.HomeScreen

@Composable
fun SplashScreen(
    onSplashScreenFinished: () -> Unit
) {
    // State to control the visibility of the splash screen
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        visible = false
        onSplashScreenFinished()
    }
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
fun MainApp() {
    var showSplashScreen by remember { mutableStateOf(true) }
    var startDes by remember { mutableStateOf("auth") }


    if (showSplashScreen) {
        SplashScreen(
            onSplashScreenFinished = {
                showSplashScreen = false
            }
        )
    } else {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = startDes
        ) {
            composable("auth") {
                AuthScreen(navController)
            }

            composable("home") {
                HomeScreen(navController)
            }

            composable("search") {
//                    SearchScreen(navController)
            }

            composable("countdown") {
//                    CountdownScreen(navController)
            }

            composable("profile") {
//                    ProfileScreen(navController)
            }

        }
    }
}