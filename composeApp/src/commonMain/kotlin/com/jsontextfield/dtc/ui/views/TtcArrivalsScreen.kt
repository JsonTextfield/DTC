@file:OptIn(ExperimentalMaterial3Api::class)

package com.jsontextfield.dtc.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jsontextfield.dtc.core.ui.components.BackButton
import com.jsontextfield.dtc.core.ui.components.CountdownTimer
import com.jsontextfield.dtc.core.ui.viewmodels.TtcArrivalItem
import com.jsontextfield.dtc.core.ui.viewmodels.TtcArrivalsUIState
import com.jsontextfield.dtc.core.ui.viewmodels.TtcArrivalsViewModel
import com.jsontextfield.dtc.ui.intents.MainScreenNavigationAction
import com.jsontextfield.dtc.ui.intents.Stops
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TtcArrivalsScreen(
    viewModel: TtcArrivalsViewModel,
    onNavigationAction: (MainScreenNavigationAction) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    FilledTonalButton(onClick = { onNavigationAction(Stops()) }) {
                        Text(viewModel.stopId.orEmpty())
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState is TtcArrivalsUIState.Success) {
                CountdownTimer({ timeRemaining })
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is TtcArrivalsUIState.Loading -> {
                    CircularProgressIndicator()
                }

                is TtcArrivalsUIState.Error -> {
                    Text(
                        text = "Failed to load arrivals: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is TtcArrivalsUIState.Success -> {
                    if (state.arrivals.isEmpty()) {
                        Text(text = "No upcoming arrivals")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.arrivals) { arrival ->
                                TtcArrivalRow(arrival)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TtcArrivalRow(arrival: TtcArrivalItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${arrival.routeId ?: "N/A"} Route Destination",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(10 / 12f)
        )

        Text(
            text = "${arrival.minutes}\nmin",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(2 / 12f)
        )
    }
}
