package uqac.catwalk

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uqac.catwalk.ui.theme.CatwalkTheme


class WalkActivity : ComponentActivity() {
    private val viewModel: walkViewModel by viewModels()
    private var locationService: LocationService? by mutableStateOf(null)
    private var isBound by mutableStateOf(false)

    // Définit les callbacks pour la connexion au service
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isBound = true

            // Commencer à observer les mises à jour de localisation
            // et les transmettre au ViewModel
            locationService?.locationUpdates?.let { flow ->
                lifecycleScope.launch {
                    flow.collect { location ->
                        location?.let {
                            viewModel.updateLocation(it)
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        // Lier au service lorsque l'activité devient visible
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Détacher le service lorsque l'activité n'est plus visible
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

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
                        // On passe directement le ViewModel
                        viewModel = viewModel,
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
fun ProgressContent(modifier: Modifier = Modifier, viewModel: walkViewModel) {
    val context = LocalContext.current
    // Observez l'état depuis le ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // La gestion de l'activation de la localisation reste ici, car elle est liée à l'UI
    var locationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }
    val serviceIntent = remember { Intent(context, LocationService::class.java) }

    // --- Les launchers pour la permission et les paramètres restent inchangés ---
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // L'utilisateur revient des paramètres système. On vérifie si la localisation est maintenant active.
        if (isLocationEnabled(context)) {
            locationEnabled = true
            // Si la permission est déjà accordée, on s'assure que le service est démarré.
            // C'est sans risque d'appeler startService plusieurs fois.
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                context.startService(serviceIntent)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            locationEnabled = granted
            if (granted) {
                context.startService(serviceIntent)
            }
        }
    )


    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            locationEnabled = isLocationEnabled(context)
            if (locationEnabled) {
                context.startService(serviceIntent)
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
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
            if (uiState.isTooFast){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.red)
                    )
                ) {
                    Text(
                        text = "Vous allez trop vite ! La balade est mise en pause.",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
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
                    text = "Marché ${uiState.progress.toInt()} mètres", // Afficher en entier pour plus de lisibilité
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val remplissage = (uiState.progress / 2500.0).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { remplissage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            }
            Button(
                onClick = {
                    val intent = Intent(context, EndWalkActivity::class.java)
                    intent.putExtra("progress", uiState.progress)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

                    // L'activity va se détacher automatiquement via onStop(),
                    // mais on arrête le service pour qu'il ne tourne plus en fond.
                    context.stopService(serviceIntent)

                    context.startActivity(intent)
                    (context as? Activity)?.finish() // Vous voudrez peut-être aussi fermer WalkActivity
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
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
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
        // This preview is simplified and won't show the real progress.
        // For a more complete preview, you might need to create a fake ViewModel.
        ProgressContent(
            viewModel = walkViewModel() // Assuming a default constructor for preview
        )
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
    inner class LocationBinder : Binder() {
        // Retourne l'instance du service pour que les clients puissent appeler ses méthodes publiques
        fun getService(): LocationService = this@LocationService
    }

    // 2. Créer une instance du Binder
    private val binder = LocationBinder()

    // 3. Implémenter onBind pour retourner le binder
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates: StateFlow<Location?> = _locationUpdates.asStateFlow()

    override fun onCreate() {
        super.onCreate()

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
            @Suppress("DEPRECATION")
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
    }
}