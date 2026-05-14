package com.paryavaran.kavalu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Report(
    val id: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val status: String = "pending",
    val userId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    reports = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Report::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportItem(report = report, onCleaned = {
                        db.collection("reports").document(report.id)
                            .update("status", "cleaned")
                        
                        // Optionally reward user points here
                        db.collection("users").document(report.userId)
                            .get().addOnSuccessListener { userDoc ->
                                val currentPoints = userDoc.getLong("points") ?: 0
                                db.collection("users").document(report.userId)
                                    .update("points", currentPoints + 10)
                            }
                    })
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onCleaned: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Phone: ${report.phoneNumber}", style = MaterialTheme.typography.labelLarge)
                Text(text = report.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Status: ${report.status}",
                    color = if (report.status == "cleaned") Color.Green else Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            if (report.status != "cleaned") {
                IconButton(onClick = onCleaned) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark as Cleaned",
                        tint = Color.Green
                    )
                }
            }
        }
    }
}
