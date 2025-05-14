package com.example.appgps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.Manifest

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nomeDispositivo = intent?.getStringExtra("nome_dispositivo") ?: "desconhecido"
        val intervaloSegundos = intent?.getIntExtra("intervalo_envio", 5) ?: 5

        Log.d("SERVICE_DEBUG", "Iniciando LocationService para: $nomeDispositivo, intervalo: $intervaloSegundos segundos")

        startForeground(1, createNotification())
        iniciarAtualizacaoLocalizacao(nomeDispositivo, intervaloSegundos)

        return START_STICKY
    }

    private fun iniciarAtualizacaoLocalizacao(nomeDispositivo: String, intervaloSegundos: Int) {
        val locationRequest = LocationRequest.create().apply {
            interval = intervaloSegundos * 1000L
            fastestInterval = (intervaloSegundos * 0.8).toLong() * 1000L
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        // Permissão extra de segurança
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SERVICE_DEBUG", "Permissão de localização não concedida. Serviço será finalizado.")
            stopSelf()
            return
        }

        Log.d("SERVICE_DEBUG", "Solicitando atualizações de localização...")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                Log.d("SERVICE_DEBUG", "Localização recebida: lat=${location.latitude}, lon=${location.longitude}")

                val database = Firebase.database
                val ref = database.getReference("coordenadas").child(nomeDispositivo)

                val dados = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )

                Log.d("SERVICE_DEBUG", "Enviando dados para o Firebase...")
                ref.setValue(dados)
                    .addOnSuccessListener {
                        Log.d("SERVICE_DEBUG", "Dados enviados com sucesso!")
                    }
                    .addOnFailureListener {
                        Log.e("SERVICE_DEBUG", "Falha ao enviar coordenadas: ${it.message}")
                    }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SERVICE_DEBUG", "Permissão de localização não concedida. Serviço encerrado.")
            stopSelf()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun createNotification(): Notification {
        val channelId = "gps_channel"
        val channelName = "Localização em segundo plano"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("Transmissão de GPS ativa")
            .setContentText("Seu dispositivo está enviando localização...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
