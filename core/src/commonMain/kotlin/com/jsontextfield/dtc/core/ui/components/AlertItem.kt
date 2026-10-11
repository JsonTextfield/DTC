package com.jsontextfield.dtc.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp.Companion.Hairline
import androidx.compose.ui.unit.dp
import com.jsontextfield.dtc.core.entities.Alert
import com.jsontextfield.dtc.core.entities.getFullSubject
import com.jsontextfield.dtc.core.ui.SquircleShape
import com.jsontextfield.dtc.core.ui.TimeFormat


@Composable
fun AlertItem(
    alert: Alert,
    timeFormat: TimeFormat = TimeFormat.RELATIVE,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val language = Locale.current.language
    val fontScale = LocalDensity.current.fontScale
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(
            width = Hairline,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier
                        .weight(10 / 12f),
                ) {
                    Text(
                        text = alert.getFullSubject(language),
                        modifier = Modifier
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleSmall,
                    )
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
                }
                if (!alert.isRead) {
                    Badge()
                }
            }
            Text(
                text = alert.getAnnotatedBody(language, MaterialTheme.colorScheme.primary),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}