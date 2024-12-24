package uk.ac.tees.mad.myholidays.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.ac.tees.mad.myholidays.models.HolidaysState
import uk.ac.tees.mad.myholidays.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayDetailsScreen(
    holidayId: String,
    onBackClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    val holidaysState by viewModel.holidaysState.collectAsState()

    // Find the specific holiday
    val holiday = (holidaysState as? HolidaysState.Success)
        ?.holidays
        ?.find { it.id == holidayId }

    Log.d("HolidayDetailsScreen", "Holiday: $holiday")
    holiday?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(it.name) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Detailed holiday information
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holiday_detail_title_${it.id}")
                )
            }
        }
    }
}