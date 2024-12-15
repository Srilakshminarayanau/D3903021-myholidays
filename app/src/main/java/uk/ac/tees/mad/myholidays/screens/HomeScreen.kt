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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import uk.ac.tees.mad.myholidays.models.Holiday
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import uk.ac.tees.mad.myholidays.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    // Dummy data
    val holidays = remember {
        listOf(
            Holiday(
                id = "1",
                name = "Christmas",
                date = LocalDate.of(2024, 12, 25),
                country = "Global",
                imageUrl = "https://example.com/christmas.jpg"
            ),
            Holiday(
                id = "2",
                name = "Independence Day",
                date = LocalDate.of(2024, 7, 4),
                country = "United States",
                imageUrl = "https://example.com/independence-day.jpg"
            ),
            Holiday(
                id = "3",
                name = "Diwali",
                date = LocalDate.of(2024, 10, 31),
                country = "India",
                imageUrl = "https://example.com/diwali.jpg"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyHolidays") },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search Holidays")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Event, contentDescription = "Countdown") },
                    label = { Text("Countdown") },
                    selected = false,
                    onClick = { navController.navigate("countdown") }
                )
            }
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
                Text(
                    text = "Upcoming Holidays",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            items(holidays) { holiday ->
                HolidayCard(
                    holiday = holiday,
                    onHolidayClick = {
                        // Navigate to holiday details or countdown
                        navController.navigate("countdown")
                    }
                )
            }
        }
    }
}

@Composable
fun HolidayCard(
    holiday: Holiday,
    onHolidayClick: (Holiday) -> Unit
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
            holiday.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = holiday.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Image(
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
                        Brush.verticalGradient(
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