package uqac.catwalk

import android.R.attr.priority
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.renderscript.RenderScript
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import uqac.catwalk.ui.theme.CatwalkTheme
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import com.google.android.gms.location.Priority

class WalkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProgressContent(
                        modifier = Modifier.padding(innerPadding),
                        context = this
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressContent(modifier: Modifier = Modifier, context: Context) {
    var progress by remember { mutableDoubleStateOf(0.0) }

    val serviceIntent = remember { Intent(context, LocationService::class.java) }
    var isServiceRunning by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                context.startService(serviceIntent)
                isServiceRunning = true
            }
        }
    )

    val location by LocationService.instance?.locationUpdates?.collectAsState() ?: remember { mutableStateOf(null) }
    var PreviousLoc by remember { mutableStateOf(location) }

    // Ce LaunchedEffect se déclenchera chaque fois que `location` change
    LaunchedEffect(location) {
        location?.let { currentLocation -> // Using a more descriptive name than "it"
            Toast.makeText(
                context,
                "Latitude: ${currentLocation.latitude}, Longitude: ${currentLocation.longitude}",
                Toast.LENGTH_LONG
            ).show()

            PreviousLoc?.let { previous -> // Also good practice to use let for PreviousLoc
                //d= 2R × sin⁻¹(√[sin²((θ₂ - θ₁)/2) + cosθ₁ × cosθ₂ × sin²((φ₂ - φ₁)/2)])
                val lat1 = Math.toRadians(previous.latitude)
                val lat2 = Math.toRadians(currentLocation.latitude)
                val lon1 = Math.toRadians(previous.longitude)
                val lon2 = Math.toRadians(currentLocation.longitude)
                val distance = 2 * 6371400.0 * asin(
                    sqrt(sin((lat2 - lat1) / 2).pow(2) + cos(lat1) * cos(lat2) * sin((lon2 - lon1) / 2).pow(2))
                )
                progress += distance
            }
            PreviousLoc = currentLocation // Update PreviousLoc with the stable currentLocation
        }
    }


    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // La permission est déjà accordée, on démarre le service
                context.startService(serviceIntent)
                isServiceRunning = true
            }
            else -> {
                // La permission n'est pas accordée, on la demande
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Text
        Text(
            text = "Ballade en cours...",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Temporary image (using launcher icon as placeholder)
        Image(
            painter = painterResource(id = android.R.drawable.ic_dialog_info),
            contentDescription = "Image temporaire",
            modifier = Modifier.size(150.dp)
        )

        // Progress bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Marché ${(progress)} mètres",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            var remplissage : Float = (progress / 2500).toFloat()
            while (remplissage > 1){
                remplissage-=1
            }
            LinearProgressIndicator(
            progress = { remplissage},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
        Text(
            text="Prochain objectif à 10 000 pas",
        )

        // Stop button
        Button(
            onClick = {
                val intent = Intent(context, EndWalkActivity::class.java)
                intent.putExtra("progress", progress)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                context.stopService(serviceIntent)
                isServiceRunning = false
                context.startActivity(intent)
            },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
                .height(60.dp),
        ) {
            Text(text = "STOP", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressContentPreview() {
    CatwalkTheme {
        ProgressContent(context = LocalContext.current)
    }
}

class LocationService : LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { location ->
                _locationUpdates.value = location
            }
        }
    }

    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates: StateFlow<Location?> = _locationUpdates.asStateFlow()

    // Companion object pour accéder au service plus facilement
    companion object {
        var instance: LocationService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        startForeground(1, createNotification())
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, ContextCompat.getMainExecutor(this), locationCallback)
        }
    }


    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)

            return Notification.Builder(this, channelId)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        } else {
            return Notification.Builder(this)
                .setContentTitle("Tracking Location")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        instance = null
    }
}
