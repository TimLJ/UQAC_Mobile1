package uqac.catwalk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uqac.catwalk.ui.theme.CatwalkTheme
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import com.google.android.gms.location.Priority
import android.provider.Settings

class WalkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProgressContent(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(colorResource(R.color.yellow_white)),
                        context = this
                    )
                }
            }
        }
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}


@Composable
fun ProgressContent(modifier: Modifier = Modifier, context: Context) {
    var progress by remember { mutableDoubleStateOf(0.0) }
    val serviceIntent = remember { Intent(context, LocationService::class.java) }
    var isServiceRunning by remember { mutableStateOf(false) }

    var locationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (isLocationEnabled(context)) {
            locationEnabled = true
            if (!isServiceRunning && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                context.startService(serviceIntent)
                isServiceRunning = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                locationEnabled = true
                context.startService(serviceIntent)
                isServiceRunning = true
            }
            else {
                locationEnabled = false
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
                if (isLocationEnabled(context)) {
                    locationEnabled = true
                    context.startService(serviceIntent)
                    isServiceRunning = true
                } else {
                    locationEnabled = false
                }
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    if (locationEnabled) {
        // --- UI existante quand tout va bien ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp, 16.dp, 16.dp, 0.dp)
                .background(color = colorResource(R.color.yellow_white)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Ballade en cours...",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(id = R.drawable.walk_icon),
                contentDescription = "Chat noir de profil qui marche.",
                modifier = Modifier.size(150.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Marché ${progress.toInt()} mètres", // Afficher en entier pour plus de lisibilité
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val remplissage = (progress / 2500.0).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { remplissage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            }
            Text(
                text = "Localisation ${location?.latitude}, ${location?.longitude}",
            )
            Button(
                onClick = {
                    val intent = Intent(context, EndWalkActivity::class.java)
                    intent.putExtra("progress", progress)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.stopService(serviceIntent)
                    isServiceRunning = false
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.red)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .width(200.dp)
                    .height(60.dp),
            ) {
                Text(text = "STOP", fontSize = 18.sp)
            }
        }
    } else {
        // --- NOUVELLE UI quand la localisation est désactivée ---
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Géolocalisation requise",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pour suivre votre balade, veuillez activer les services de localisation de votre téléphone.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = {
                // Ouvre les paramètres de localisation du téléphone
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                locationSettingsLauncher.launch(intent)
            }) {
                Text("Activer la localisation")
            }
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