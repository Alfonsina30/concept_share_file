package com.example.reserve_bot_share_file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import io.flutter.plugin.common.EventChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class FileReceiverManager(private val context: Context) : EventChannel.StreamHandler {

    private val tag = "FileReceiverManager"
    private var eventSink: EventChannel.EventSink? = null
    private var pendingFileData: String? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.d(tag, "DENTRO EVENT CHANNEL ON LISTEN")
        eventSink = events

        if (pendingFileData != null) {
            Log.d(tag, "Invio pendingFileData a Flutter da onListen")
            eventSink?.success(pendingFileData)
            pendingFileData = null
        } else {
            Log.d(tag, "Nessun file in attesa, invio NO_DATA")
            eventSink?.success("NO_DATA")
        }
    }

    override fun onCancel(arguments: Any?) {
        Log.d(tag, "DENTRO EVENT CHANNEL ON CANCEL")
        eventSink = null
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val uri: Uri? = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }

        if (uri != null) {
            // (Dispatchers.IO) per non bloccare la UI e prevenire ANR
            scope.launch(Dispatchers.IO) {
                try {
                    var fileSize = 0L
                    val maxSafeSize = 26214400L // 25 MB

                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIndex != -1) {
                                fileSize = it.getLong(sizeIndex)
                            }
                        }
                    }

                    if (fileSize > maxSafeSize) {
                        withContext(Dispatchers.Main) {
                            eventSink?.error("FILE_TOO_LARGE", "Il file supera i 25MB.", null)
                        }
                        return@launch
                    }

                    val inputStream = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    inputStream?.close()

                    val fileContent = stringBuilder.toString()

                    // Torniamo sul Thread Principale per inviare i dati a Flutter
                    withContext(Dispatchers.Main) {
                        if (eventSink != null) {
                            Log.d(tag, "Inviando file content a Flutter in tempo reale")
                            eventSink?.success(fileContent)
                        } else {
                            Log.d(tag, "pendingFileData salvato in background: $fileContent")
                            pendingFileData = fileContent
                           // eventSink?.success(pendingFileData)
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        eventSink?.error("FILE_ERROR", e.message, null)
                    }
                    Log.e(tag, "Errore durante la lettura: ${e.message}")
                }
            }
        }
    }
}
//
//    // --- 2. LOGICA DI LETTURA DEL FILE ---
//    fun handleIntent(intent: Intent?) {
//        if (intent?.action == Intent.ACTION_VIEW) {
//            val uri: Uri? = intent.data
//            if (uri != null) {
//                try {
//                    // 1. CONTROLLO DI SICUREZZA: Verifichiamo la dimensione del file
//                    var fileSize = 0L
//                    val maxSafeSize = 26214400L // 25 MB
//
//                    // Usiamo context.contentResolver
//                    val cursor = context.contentResolver.query(uri, null, null, null, null)
//                    cursor?.use {
//                        if (it.moveToFirst()) {
//                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
//                            if (sizeIndex != -1) {
//                                fileSize = it.getLong(sizeIndex)
//                            }
//                        }
//                    }
//
//                    if (fileSize > maxSafeSize) {
//                        eventSink?.error(
//                            "FILE_TOO_LARGE",
//                            "Il file supera i 25MB. Non è un file di dati Reservabot valido.", null
//                        )
//                        Log.d(tag, "FILE_TOO_LARGE")
//                        return // Blocchiamo la lettura
//                    }
//
//                    // 2. Lettura del file
//                    val inputStream = context.contentResolver.openInputStream(uri)
//                    val reader = BufferedReader(InputStreamReader(inputStream))
//                    val stringBuilder = StringBuilder()
//                    var line: String?
//
//                    while (reader.readLine().also { line = it } != null) {
//                        stringBuilder.append(line)
//                    }
//                    inputStream?.close()
//
//                    val fileContent = stringBuilder.toString()
//
//                    // Inviamo allo stream Dart
//                    if (eventSink != null) {
//                        Log.d(tag, "inviando file content a Flutter")
//                        eventSink?.success(fileContent)
//                    } else {
//                        Log.d(tag, "pendingFileData salvato: $fileContent")
//                        pendingFileData = fileContent
//
//                    }
//
//                } catch (e: Exception) {
//                    eventSink?.error("FILE_ERROR", e.message, null)
//                    Log.e(tag, "Errore durante la lettura: ${e.message}")
//                }
//            }
//        }
//    }
