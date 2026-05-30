package com.example.reserve_bot_share_file

import android.content.Intent
import android.net.Uri
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity: FlutterActivity() {

    private val tag = "MainActivity"
    private val channelPrenotazioniName = "com.reserve_bot/compartirPdf"
    private var sharedChannel: SharedPrenotazioniChannel? = null

    // Il nome del canale radio per RICEVERE
    private val EVENT_CHANNEL = "com.reservabot.app/file_events"
    private var eventSink: EventChannel.EventSink? = null
    private var pendingFileData: String? = null

    /*
    Quando avvii l'app cliccando sulla tua icona (avvio normale, nessun file condiviso), Android passa per configureFlutterEngine e chiama handleIntent(intent).

Dato che non c'è nessun file, handleIntent non fa niente. Quindi pendingFileData rimane null.

Un attimo dopo, Flutter (la parte visiva in Dart) si sveglia e si connette al canale. Questo fa scattare il metodo onListen in Kotlin.

Kotlin entra in onListen, guarda dentro pendingFileData, vede che è vuoto (null), e capisce al volo la situazione: cade nel blocco else e lancia il "NO_DATA" a Flutter.
    */
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val binary = flutterEngine.dartExecutor.binaryMessenger

        // 1. Inizializziamo il canale per INVIARE file a WhatsApp
        sharedChannel = SharedPrenotazioniChannel(
            context,
            binary,
            channelPrenotazioniName
        )

        Log.d(tag, "DENTRO DE CONFIGURE FLUTTER ENGINE")

        // 2. Inizializziamo il canale radio (EventChannel) per RICEVERE file
        EventChannel(binary, EVENT_CHANNEL)
            .setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {

                        Log.d(tag, "DENTRO EVENT CHANNEL ON LISTEN")

                       /* eventSink = events
                        pendingFileData?.let { fileData ->
                            eventSink?.success(fileData)
                            pendingFileData = null
                        }
                        */

                        eventSink = events

                        // --- QUI INSERIAMO LA LOGICA DEL SEMAFORO ---
                        if (pendingFileData != null) {
                            // C'è un file parcheggiato! Lo inviamo a Flutter e svuotiamo la variabile
                            Log.d(tag, "Invio pendingFileData a Flutter")
                            eventSink?.success(pendingFileData)
                            pendingFileData = null
                        } else {
                            // NON c'è nessun file parcheggiato! L'utente ha aperto l'app normalmente.
                            // Inviamo il segnale a Flutter per dirgli di non aspettare e sbloccare lo schermo.
                            Log.d(tag, "Nessun file in attesa, invio NO_DATA a Flutter")
                            eventSink?.success("NO_DATA")
                        }
                    }

                    override fun onCancel(arguments: Any?) {

                        Log.d(tag, "DENTRO EVENT CHANNEL ON LISTEN")
                        eventSink = null
                    }
                }
            )

        // SCENARIO 1: L'app era chiusa. Leggiamo l'intent di avvio usando la funzione interna!
        handleIntent(intent)
    }

    // SCENARIO 2: L'app era in background. Android ci passa il nuovo intent qui.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(tag, "onNewIntent ricevuto: $intent")

        setIntent(intent) // Aggiorniamo l'intent di sistema

        // Usiamo SEMPRE la funzione interna per decodificare il file verso l'EventChannel
        handleIntent(intent)
    }

    // --- LOGICA UNICA DI RICEZIONE FILE ---
    // Avendo un'unica funzione qui, gestiamo il file allo stesso identico modo
    // sia che l'app fosse chiusa, sia che fosse aperta.
    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri: Uri? = intent.data
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    inputStream?.close()

                    val fileContent = stringBuilder.toString()

                    // Inviamo allo stream Dart
                    if (eventSink != null) {
                        Log.d(tag, "inviando file content a Flutter")
                        eventSink?.success(fileContent)
                    } else {
                        //---- pendingFileData
                        //Kotlin legge il file di WhatsApp, tenta di inviarlo a Flutter, ma si accorge che eventSink è null. Perché? Perché in un Cold Start, il motore nativo (Kotlin) è molto più veloce ad avviarsi rispetto all'interfaccia grafica (Flutter/Dart). In questo preciso istante, Dart sta ancora caricando e non ha ancora avuto il tempo di "sintonizzarsi" sulla radio (EventChannel).
                        Log.d(tag, "pendingFileData $pendingFileData fileContent $fileContent ")

                        pendingFileData = fileContent

                        Log.d(tag, "pendingFileData CAMBIADO NUEVO ESTADO: $pendingFileData")


                    }

                } catch (e: Exception) {
                    eventSink?.error("FILE_ERROR", e.message, null)
                }
            }
        }
    }
}