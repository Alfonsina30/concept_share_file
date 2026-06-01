package com.example.reserve_bot_share_file

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel

class MainActivity: FlutterActivity() {

    private val tagMainActivity = "MainActivity"
    private val channelPrenotazioniName = "com.reserve_bot/compartirPdf"
   // private var sharedChannel: SharedPrenotazioniChannel? = null
    private val eventChannel = "com.reservabot.app/file_events"
    private var fileReceiverManager : FileReceiverManager? = null

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
        SharedPrenotazioniChannel(
            context,
            binary,
            channelPrenotazioniName
        )
    // 2. Canale per RICEVERE file (Inizializziamo la nostra nuova classe)
        fileReceiverManager = FileReceiverManager(context)

        // Passiamo la nostra classe come gestore dello Stream
        EventChannel(binary, eventChannel).setStreamHandler(fileReceiverManager)

        // SCENARIO 1: L'app era chiusa. Leggiamo l'intent di avvio
        fileReceiverManager?.handleIntent(intent)

    }

    // SCENARIO 2: L'app era in background. Android ci passa il nuovo intent qui.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(tagMainActivity, "onNewIntent ricevuto: $intent")

        setIntent(intent) // Aggiorniamo l'intent di sistema

        // Usiamo SEMPRE la funzione interna per decodificare il file verso l'EventChannel
       fileReceiverManager?.handleIntent(intent)
    }

}