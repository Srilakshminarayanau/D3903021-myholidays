package uk.ac.tees.mad.myholidays.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import uk.ac.tees.mad.myholidays.models.Holiday
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import uk.ac.tees.mad.myholidays.R
import uk.ac.tees.mad.myholidays.models.HolidaysState
import uk.ac.tees.mad.myholidays.models.LocationResult
import uk.ac.tees.mad.myholidays.utils.LocationService
import uk.ac.tees.mad.myholidays.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
) {
    val holidaysState by viewModel.holidaysState.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val locationPermission =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION) {
            if (it) {
                viewModel.fetchLocation(LocationService(context))
            }
        }

    val locationService = LocationService(context)
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            viewModel.fetchLocation(locationService)
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationState is LocationResult.Error) {
        if (locationState != null) {
            snackbarHostState.showSnackbar(
                message = (locationState as? LocationResult.Error)?.message ?: "",
                duration = SnackbarDuration.Short
            )
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("MyHolidays") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement country selection dialog */ }) {
                        Icon(Icons.Default.Public, contentDescription = "Select Country")
                    }
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search Holidays")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Location Information
                LocationInfoChip(
                    locationState = locationState,
                    onRetry = { viewModel.fetchLocation(locationService) }
                )
            }

            // Handle different states of holidays and location
            when (val state = holidaysState) {
                is HolidaysState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is HolidaysState.Success -> {
                    // Holidays Header
                    item {
                        Text(
                            text = "Upcoming Holidays",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    // Holiday List
                    items(state.holidays) { holiday ->
                        HolidayCard(
                            holiday = holiday,
                            onHolidayClick = {
                                // Navigate to holiday details
                                navController.navigate("countdown")
                            }
                        )
                    }
                }

                is HolidaysState.Error -> {
                    // Show error with retry option
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Error: ${state.message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.fetchLocation(locationService) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                is HolidaysState.Initial -> {}
            }
        }
    }
}

@Composable
fun LocationInfoChip(
    locationState: LocationResult?,
    onRetry: () -> Unit,
) {
    when (locationState) {
        is LocationResult.Success -> {
            FilterChip(
                selected = true,
                onClick = {},
                label = {
                    Text("${locationState.countryName} Holidays")
                },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location")
                }
            )
        }

        is LocationResult.PermissionDenied -> {
            FilterChip(
                selected = false,
                onClick = onRetry,
                label = { Text("Location Permission Denied") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOff, contentDescription = "Location Denied")
                }
            )
        }

        is LocationResult.Error -> {
            FilterChip(
                selected = false,
                onClick = onRetry,
                label = { Text("Location Error. Tap to Retry") },
                leadingIcon = {
                    Icon(Icons.Default.Error, contentDescription = "Location Error")
                }
            )
        }

        null -> {
            FilterChip(
                selected = false,
                onClick = onRetry,
                label = { Text("Fetching Location...") },
                leadingIcon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            )
        }
    }
}


@Composable
fun HolidayCard(
    holiday: Holiday,
    onHolidayClick: (Holiday) -> Unit,
) {
    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), holiday.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHolidayClick(holiday) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Holiday Image
            Image(
                painter = painterResource(id = R.drawable.default_holiday),
                contentDescription = holiday.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Holiday Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = holiday.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "${holiday.date} (${daysUntil} days away)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Country",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = holiday.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}