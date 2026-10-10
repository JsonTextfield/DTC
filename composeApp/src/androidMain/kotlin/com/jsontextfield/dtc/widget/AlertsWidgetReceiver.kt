package com.jsontextfield.dtc.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.jsontextfield.dtc.widget.ui.AlertsWidget

class AlertsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AlertsWidget()
}
