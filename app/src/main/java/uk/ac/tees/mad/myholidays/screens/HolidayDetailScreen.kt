package uk.ac.tees.mad.myholidays.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import uk.ac.tees.mad.myholidays.models.HolidaysState
import uk.ac.tees.mad.myholidays.viewmodels.HomeViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayDetailsScreen(
    holidayId: String,
    onBackClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    val holidaysState by viewModel.holidaysState.collectAsState()
    val holiday = (holidaysState as? HolidaysState.Success)?.holidays?.find { it.id == holidayId }

    holiday?.let { currentHoliday ->
        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), currentHoliday.date)
        val timeUntil by produceState(initialValue = calculateTimeUntil(currentHoliday.date)) {
            while (true) {
                delay(1000)
                value = calculateTimeUntil(currentHoliday.date)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentHoliday.name) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Countdown Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Time Until Holiday",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CountdownUnit(value = timeUntil.days, unit = "Days")
                                CountdownUnit(value = timeUntil.hours, unit = "Hours")
                                CountdownUnit(value = timeUntil.minutes, unit = "Minutes")
                                CountdownUnit(value = timeUntil.seconds, unit = "Seconds")
                            }
                        }
                    }
                }

                // Holiday Info Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.CalendarMonth,
                                label = "Date",
                                value = currentHoliday.date.format(
                                    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(
                                icon = Icons.Default.Flag,
                                label = "Country",
                                value = currentHoliday.country
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(
                                icon = Icons.Default.Category,
                                label = "Type",
                                value = currentHoliday.type.toString()
                            )
                        }
                    }
                }

                // Description Section
                currentHoliday.description?.let { desc ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "About this Holiday",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: Long, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

data class TimeUntil(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
)

fun calculateTimeUntil(date: LocalDate): TimeUntil {
    val now = LocalDateTime.now()
    val holidayDateTime = date.atTime(0, 0)

    val days = ChronoUnit.DAYS.between(now.toLocalDate(), date)
    val hours = ChronoUnit.HOURS.between(now, holidayDateTime) % 24
    val minutes = ChronoUnit.MINUTES.between(now, holidayDateTime) % 60
    val seconds = ChronoUnit.SECONDS.between(now, holidayDateTime) % 60

    return TimeUntil(days, hours, minutes, seconds)
}