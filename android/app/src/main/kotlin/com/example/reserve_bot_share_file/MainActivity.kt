package com.example.reserve_bot_share_file

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel

class MainActivity: FlutterActivity() {

    private val tagMainActivity = "MainActivity"
    private val channelJson = "com.example.sharejson"
    private val eventChannel = "com.example.app_share/file"
    private var fileReceiverManager : FileReceiverManager? = null


    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val binary = flutterEngine.dartExecutor.binaryMessenger

        //----- SHARE PDF
        SharePdfFile().configureFlutterEngine(flutterEngine, context)

        // 1. Inizializziamo il canale per INVIARE file a WhatsApp
        ShareJsonFile(
            context,
            binary,
            channelJson
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