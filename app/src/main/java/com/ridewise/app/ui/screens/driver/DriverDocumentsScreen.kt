package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DriverDocumentsScreen(documents: DriverDocument?) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverCard(title = "My Documents") {
            DocumentStatusRow("Driver License", documents?.licenseFile)
            DocumentStatusRow("PDP", documents?.pdpFile)
            DocumentStatusRow("Roadworthy Certificate", documents?.roadworthyFile)
        }
    }
}