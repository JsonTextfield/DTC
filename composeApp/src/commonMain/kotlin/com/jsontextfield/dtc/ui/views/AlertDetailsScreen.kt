package com.jsontextfield.dtc.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jsontextfield.dtc.core.entities.getFullSubject
import com.jsontextfield.dtc.core.ui.SquircleShape
import com.jsontextfield.dtc.core.ui.Status
import com.jsontextfield.dtc.core.ui.components.BackButton
import com.jsontextfield.dtc.core.ui.components.ErrorScreen
import com.jsontextfield.dtc.core.ui.components.LoadingScreen
import com.jsontextfield.dtc.core.ui.components.TripCodeBox
import com.jsontextfield.dtc.core.ui.viewmodels.AlertDetailsViewModel
import com.jsontextfield.dtc.ui.BannerAd
import dtc.core.generated.resources.Res
import dtc.core.generated.resources.alert_details_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailsScreen(
    alertDetailsViewModel: AlertDetailsViewModel,
    onBackPressed: () -> Unit = {},
) {
    val language = Locale.current.language
    val fontScale = LocalDensity.current.fontScale
    val uiState by alertDetailsViewModel.uiState.collectAsState()
    LaunchedEffect(language) {
        alertDetailsViewModel.loadAlert(language)
    }
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(Res.string.alert_details_title))
            }, navigationIcon = {
                BackButton(onBackPressed)
            }, modifier = Modifier.shadow(4.dp))
        },
        bottomBar = {
            if (uiState.isAdEnabled) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    BannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(
                                    LayoutDirection.Ltr
                                ),
                                bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding(),
                            ),
                    )
                }
            }
        },
    ) {
        AnimatedContent(
            uiState.status,
            transitionSpec = {
                fadeIn(tween(150)) togetherWith fadeOut(tween(150))
            },
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp),
        ) { state ->
            when (state) {
                Status.LOADING -> {
                    LoadingScreen()
                }

                Status.LOADED -> {
                    uiState.alert?.let { alert ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (line in alert.affectedLines) {
                                    TripCodeBox(
                                        tripCode = line,
                                        modifier = Modifier
                                            .size((MaterialTheme.typography.titleMedium.fontSize.value * fontScale * 2).dp)
                                            .background(
                                                color = Color.Gray,
                                                shape = SquircleShape,
                                            )
                                    )
                                }
                            }
                            Text(alert.getFullSubject(language), style = MaterialTheme.typography.titleMedium)
                            Text(
                                alert.getAnnotatedBody(language, MaterialTheme.colorScheme.primary),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(
                                        bottom = WindowInsets.safeDrawing.asPaddingValues()
                                            .calculateBottomPadding() + 40.dp
                                    )
                            )
                        }
                    }
                }

                Status.ERROR -> {
                    ErrorScreen {
                        alertDetailsViewModel.loadAlert(language)
                    }
                }
            }

        }
    }
}