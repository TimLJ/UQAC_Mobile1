package uqac.catwalk.walk

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uqac.catwalk.R
import uqac.catwalk.ui.theme.CatwalkTheme

class WalkActivity : ComponentActivity() {

    private val viewModel: walkViewModel by viewModels()
    private var locationService: LocationService? = null
    private var isBound = false

    // Connecte le service de localisation
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isBound = true

            lifecycleScope.launch {
                locationService?.locationUpdates?.collect { location ->
                    location?.let { viewModel.updateLocation(it) }
                }
            }
        }

        // Déconnecte le service de localisation
        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            isBound = false
        }
    }

    // UI Composable pour l'activité de marche
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatwalkTheme {
                Scaffold { padding ->
                    ProgressContent(
                        modifier = Modifier.padding(padding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// Vérifie si la localisation est activée
fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}


// UI Composable pour l'activité de marche
@Composable
fun ProgressContent(
    modifier: Modifier = Modifier,
    viewModel: walkViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val serviceIntent = remember { Intent(context, LocationService::class.java) }

    var locationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationEnabled = granted
        if (granted) {
            ContextCompat.startForegroundService(context, serviceIntent)

            context.bindService(
                serviceIntent,
                (context as WalkActivity).connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    // Lanceur pour les paramètres de localisation
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isLocationEnabled(context)) {
            locationEnabled = true
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }

    // Demande de permission au lancement
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (locationEnabled) {
        // arrondir et afficher les mètres
        val meters = uiState.progress.toInt()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colorResource(R.color.yellow_white))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Text("Balade en cours...", fontSize = 24.sp)

            // affichage du nombre de mètres
            Text(
                text = "$meters m",
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )

            if (uiState.isTooFast) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.red))
                ) {
                    Text(
                        "Vous allez trop vite !",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.walk_icon),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
            // barre de progression
            StepProgressBar(progressMeters = uiState.progress.toFloat())

            Button(
                onClick = {
                    context.stopService(serviceIntent)
                    context.startActivity(
                        Intent(context, EndWalkActivity::class.java).apply {
                            putExtra("progress", uiState.progress)
                        }
                    )
                    (context as Activity).finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.red))

            ) {
                Text("STOP")
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Géolocalisation requise", fontSize = 24.sp)
            Button(
                onClick = {
                    settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            ) {
                Text("Activer la localisation")
            }
        }
    }
}

// Service de localisation en avant-plan
class LocationService : LifecycleService() {

    private lateinit var fusedClient: FusedLocationProviderClient

    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates: StateFlow<Location?> = _locationUpdates.asStateFlow()

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    private val binder = LocationBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            ).build()

            fusedClient.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        _locationUpdates.value = result.lastLocation
                    }
                },
                mainLooper
            )
        }
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Location",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Suivi de la balade")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}
