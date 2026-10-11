package com.jsontextfield.dtc.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jsontextfield.dtc.core.entities.Trip
import com.jsontextfield.dtc.core.ui.SquircleShape
import com.jsontextfield.dtc.core.ui.TimeFormat
import dtc.core.generated.resources.Res
import dtc.core.generated.resources.cancelled
import dtc.core.generated.resources.express
import dtc.core.generated.resources.number_of_cars
import dtc.core.generated.resources.platform_number
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.E
import kotlin.math.PI

private const val SHOULD_SHOW_TRAIN_INFO = false

@Composable
fun TripListItem(
    trip: Trip,
    modifier: Modifier = Modifier,
    timeFormat: TimeFormat = TimeFormat.RELATIVE,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val shouldShowTrainCode = with(LocalDensity.current) {
            fontScale <= 1.5f
        }
        Row(
            modifier = Modifier.weight(10 / 12f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (shouldShowTrainCode) {
                val fontScale = LocalDensity.current.fontScale
                TripCodeBox(
                    tripCode = trip.code,
                    textColor = trip.textColor,
                    modifier = Modifier
                        .size((MaterialTheme.typography.titleMedium.fontSize.value * fontScale * E).dp)
                        .background(color = trip.color, shape = SquircleShape)
                        .semantics {
                            contentDescription = if (trip.isBus) {
                                trip.code
                            } else {
                                trip.name
                            }
                        },
                )
            }
            Column {
                if (!shouldShowTrainCode) {
                    Text(
                        text = if (trip.isBus) {
                            "${trip.code} ${trip.name}"
                        } else {
                            trip.name
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = trip.destination,
                    maxLines = 4,
                    overflow = TextOverflow.MiddleEllipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (trip.isCancelled) {
                        Text(
                            text = stringResource(Res.string.cancelled),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    } else if (trip.isExpress) {
                        Text(
                            text = stringResource(Res.string.express),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
        TimeBox(
            trip,
            timeFormat = timeFormat,
            modifier = Modifier.weight(2 / 12f)
        )
    }
}