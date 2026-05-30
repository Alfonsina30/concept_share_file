package com.example.reserve_bot_share_file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SharedPrenotazioniChannel(
   private val context : Context,
    binary : BinaryMessenger,
   channelName : String) {

  private val methodChannel = MethodChannel(binary, channelName)

    init {
        methodChannel.setMethodCallHandler{
            call, result -> handleMethodCall(call, result)
        }
    }



        private fun handleMethodCall(call: MethodCall, result: MethodChannel.Result) {
            when (call.method ) {
                "sharePdf"-> {
                try {

                    val jsonCitas = call.arguments as String
                    //--creiamo il file con il json
                    val file = File(context.cacheDir, "eventi.reservabot"

                    )
                    file.writeText(jsonCitas)

                    shareFile(file
                    ) //-- questo mimeType è lo stesso di androidManifest nell intentFilter

                    result.success(null)

                } catch (e: Exception) {
                    result.error(
                        "PDF_ERROR",
                        "Error al generar o compartir PDF: ${e.message}",
                        null
                    )
                }

            }  //----MethodChannel pdf



            else ->  result.notImplemented()

        }
    }

    ///
    private fun shareFile(file: File) {
        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        // 1. Definisci i pacchetti autorizzati
        val allowedPackages = listOf(
            "com.whatsapp",                 // WhatsApp
            "com.whatsapp.w4b",             // WhatsApp Business
            "com.google.android.gm",        // Gmail
            "com.google.android.apps.docs"  // Google Drive
        )

        // 2. Crea l'Intent base
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/x-reservabot"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. Chiedi ad Android: "Quali app sul telefono sanno gestire questo file?"
        val resInfoList = context.packageManager.queryIntentActivities(
            baseIntent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )

        val targetedIntents = mutableListOf<Intent>()

        // 4. Filtra la lista: tieni SOLO le app che hai autorizzato
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName

            if (allowedPackages.contains(packageName)) {
                // Ricrea un intent specifico e mirato per questa esatta app
                val targetedIntent = Intent(Intent.ACTION_SEND).apply {
                    type = baseIntent.type
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    // Setta il pacchetto e la classe esatta dell'app bersaglio
                    setPackage(packageName)
                    setClassName(packageName, resolveInfo.activityInfo.name)
                }
                targetedIntents.add(targetedIntent)
            }
        }

        // 5. Costruisci il menu personalizzato
        if (targetedIntents.isNotEmpty()) {
            val mainIntent = targetedIntents.removeAt(0)
            val chooser = Intent.createChooser(mainIntent, "Condividi dati evento")

            if (targetedIntents.isNotEmpty()) {
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
            }

            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            println("Nessuna app di condivisione compatibile trovata sul dispositivo.")
            // LOG DI DEBUG: Stampiamo cosa Android ha effettivamente trovato
            val appTrovate = resInfoList.map { it.activityInfo.packageName }
            println("Android ha trovato queste app per il file: $appTrovate")
        }
    }



//    private fun shareFile(file: File) {
//            val authority = "${context.packageName}.provider"
//            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
//
//            val intent = Intent(Intent.ACTION_SEND)
//            // CRUCIALE: Non usare "*/*". Usa un MIME type binario o personalizzato
//            // Questo dice a WhatsApp: "Questo è un file specifico, non toccare il nome"
//            intent.type = "application/x-reservabot"
//
//           // intent.type = "application/x-reservabot"
//            intent.putExtra(Intent.EXTRA_STREAM, uri)
//
////            // 2. CRÍTICO: Usar ClipData para adjuntar metadatos del tipo de archivo
////            val clipData = android.content.ClipData.newRawUri("Reservabot Data", uri)
////            intent.clipData = clipData
//
//
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//            println("DENTRO SHARE FILE INTENT $intent") //I/System.out(22371): DENTRO SHARE FILE INTENT Intent { act=android.intent.action.SEND dat=content://com.example.reserve_bot_share_file.provider/cache/lista_partecipanti.reservabot typ=application/x-reservabot flg=0x1 }
//
//            //-- apre show dialog
//            val chooser = Intent.createChooser(intent, "Condividi dati evento")
//          chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//            context.startActivity(chooser)
//        }


}





