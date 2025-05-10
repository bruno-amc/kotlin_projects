package com.example.appgps

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.*
import kotlin.math.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult




class MainActivity : AppCompatActivity() {

    private lateinit var editNomeDispositivo: EditText
    private lateinit var editIntervaloEnvio: EditText
    private lateinit var editDistanciaAlerta: EditText
    private lateinit var checkboxAlertaSonoro: CheckBox
    private lateinit var btnAtivarTransmissao: Button
    private lateinit var textDistanciaAtual: TextView

    private var transmissaoAtiva = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa os componentes da interface
        editNomeDispositivo = findViewById(R.id.editNomeDispositivo)
        editIntervaloEnvio = findViewById(R.id.editIntervaloEnvio)
        editDistanciaAlerta = findViewById(R.id.editDistanciaAlerta)
        checkboxAlertaSonoro = findViewById(R.id.checkboxAlertaSonoro)
        btnAtivarTransmissao = findViewById(R.id.btnAtivarTransmissao)
        btnAtivarTransmissao.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0)

        textDistanciaAtual = findViewById(R.id.textDistanciaAtual)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // recuperar o nome do dispositivo
        val prefs = getSharedPreferences("app_config", MODE_PRIVATE)
        editNomeDispositivo.setText(prefs.getString("nome_dispositivo", ""))

        val intervaloSalvo = prefs.getInt("intervalo_envio", 5)
        editIntervaloEnvio.setText(intervaloSalvo.toString())

        val distanciaSalva = prefs.getFloat("distancia_alerta", 20f)
        editDistanciaAlerta.setText(distanciaSalva.toString())

        checkboxAlertaSonoro.isChecked = prefs.getBoolean("alerta_sonoro", false)



        btnAtivarTransmissao.setOnClickListener {
            if (!transmissaoAtiva) {
                iniciarTransmissao()
            } else {
                pararTransmissao()
            }
        }
    }

    private fun iniciarTransmissao() {
        val nome = editNomeDispositivo.text.toString().trim()
        val intervalo = editIntervaloEnvio.text.toString().toIntOrNull()
        val distanciaMinima = editDistanciaAlerta.text.toString().toDoubleOrNull()
        val prefs = getSharedPreferences("app_config", MODE_PRIVATE)
        prefs.edit()
            .putString("nome_dispositivo", nome)
            .putInt("intervalo_envio", intervalo ?: 5)
            .putFloat("distancia_alerta", distanciaMinima!!.toFloat())
            .putBoolean("alerta_sonoro", checkboxAlertaSonoro.isChecked)
            .apply()


        if (nome.isEmpty() || intervalo == null || distanciaMinima == null) {
            Toast.makeText(this, "Preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        transmissaoAtiva = true
        btnAtivarTransmissao.text = "PARAR TRANSMISSÃO"
        btnAtivarTransmissao.setBackgroundColor(ContextCompat.getColor(this, R.color.vermelho_parado))
        btnAtivarTransmissao.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop, 0, 0, 0)

        Toast.makeText(this, "Transmissão iniciada.", Toast.LENGTH_SHORT).show()

        runnable = object : Runnable {
            override fun run() {
                enviarCoordenadas(nome)
                lerCoordenadasDoOutro(nome, distanciaMinima)
                handler.postDelayed(this, intervalo * 1000L)
            }
        }
        handler.post(runnable)
    }

    private fun pararTransmissao() {
        transmissaoAtiva = false
        handler.removeCallbacks(runnable)
        btnAtivarTransmissao.text = "ATIVAR TRANSMISSÃO"
        btnAtivarTransmissao.setBackgroundColor(ContextCompat.getColor(this, R.color.verde_ativo))
        btnAtivarTransmissao.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow, 0, 0, 0)
        Toast.makeText(this, "Transmissão encerrada.", Toast.LENGTH_SHORT).show()
    }

    // 🚧 Stub - envia a coordenada para o Firebase
    private fun enviarCoordenadas(nomeDispositivo: String) {
        obterLocalizacaoAtual { latitude, longitude ->

            val database = Firebase.database
            val ref = database.getReference("coordenadas").child(nomeDispositivo)

            val dados = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to System.currentTimeMillis()
            )

            ref.setValue(dados)
                .addOnSuccessListener {
                    Log.d("FIREBASE_LOG", "[$nomeDispositivo] Coordenadas salvas com sucesso")
                }
                .addOnFailureListener {
                    Log.e("FIREBASE_LOG", "Erro ao salvar coordenadas: ${it.message}")
                }
        }
    }


    private fun calcularDistanciaEmMetros(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Raio da Terra em metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }





    // 🚧 Stub - lê coordenadas do outro e calcula distância
    private fun lerCoordenadasDoOutro(nomeDispositivoAtual: String, distanciaMinima: Double) {
        obterLocalizacaoAtual { minhaLat, minhaLon ->

            val database = Firebase.database
            val ref = database.getReference("coordenadas")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (filho in snapshot.children) {
                        val nomeOutro = filho.key
                        if (nomeOutro != null && nomeOutro != nomeDispositivoAtual) {
                            val lat = filho.child("latitude").getValue(Double::class.java)
                            val lon = filho.child("longitude").getValue(Double::class.java)

                            if (lat != null && lon != null) {
                                val distancia = calcularDistanciaEmMetros(minhaLat, minhaLon, lat, lon)

                                Log.d("DISTANCIA_DEBUG", "Distância calculada: $distancia metros entre $nomeDispositivoAtual e $nomeOutro")

                                runOnUiThread {
                                    textDistanciaAtual.text = "Distância até $nomeOutro: %.2f m".format(distancia)
                                    if (checkboxAlertaSonoro.isChecked && distancia < distanciaMinima) {
                                        emitirAlertaSonoro()
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FIREBASE_LOG", "Erro ao ler coordenadas: ${error.message}")
                }
            })
        }
    }




    private fun emitirAlertaSonoro() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alerta)
        }

        // Se estiver tocando, reinicia do começo
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.seekTo(0)
            } else {
                it.start()
            }
        }
    }

    private fun obterLocalizacaoAtual(onResult: (latitude: Double, longitude: Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000 // 1 segundo (pode ajustar)
            numUpdates = 1  // Queremos só 1 coordenada nova
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        onResult(location.latitude, location.longitude)
                    } else {
                        Toast.makeText(applicationContext, "Localização nula", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Looper.getMainLooper()
        )
    }



    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}



