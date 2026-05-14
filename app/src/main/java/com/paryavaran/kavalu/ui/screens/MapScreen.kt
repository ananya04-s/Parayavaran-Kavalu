package com.paryavaran.kavalu.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var reports by remember { mutableStateOf<List<MapReport>>(emptyList()) }
    
    val hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Default location: Mangaluru
    val defaultLocation = LatLng(12.9141, 74.8560)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    LaunchedEffect(Unit) {
        db.collection("reports").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                reports = snapshot.documents.mapNotNull { doc ->
                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")
                    if (lat != null && lng != null) {
                        MapReport(
                            id = doc.id,
                            position = LatLng(lat, lng),
                            title = doc.getString("description") ?: "Report",
                            status = doc.getString("status") ?: "Pending",
                            imageUrl = doc.getString("imageUrl")
                        )
                    } else null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Cleanliness Map", style = MaterialTheme.typography.titleMedium)
                        Text("LIVE CLEANUP TRACKER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* Filter logic */ }) {
                        Text("FILTER NEARBY", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission)
            ) {
                reports.forEach { report ->
                    MarkerInfoWindowContent(
                        state = MarkerState(position = report.position),
                        title = report.title,
                    ) {
                        // Custom Info Window design matching the screenshot
                        Card(
                            modifier = Modifier
                                .width(250.dp)
                                .padding(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                if (report.imageUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(report.imageUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = report.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Status: ${report.status.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (report.status.lowercase() == "cleaned") Color(0xFF4CAF50) else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Map Legend overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                LegendItem(color = Color.Blue, text = "Reported Spots")
                LegendItem(color = Color(0xFF4CAF50), text = "Recently Cleaned")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(6.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

data class MapReport(
    val id: String,
    val position: LatLng,
    val title: String,
    val status: String,
    val imageUrl: String? = null
)
