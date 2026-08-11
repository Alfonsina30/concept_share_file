package com.example.reserve_bot_share_file

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File

class ShareJsonFile(
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
                "shareJson"-> {
                try {

                    val jsonCitas = call.arguments as String
                    //--creiamo il file con il json
                    val file = File(context.cacheDir, "file1235"

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
    private fun shareFile(file: File) {
        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        // 1. Definisci i pacchetti che vuoi mostrare tassativamente
        val allowedPackages = listOf(
            "com.whatsapp",                 // WhatsApp normale
            "com.whatsapp.w4b",             // WhatsApp Business
            "com.google.android.gm",        // Gmail
            "com.google.android.apps.docs"  // Google Drive
        )

        // 2. Crea l'Intent base di riferimento
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/x-reservabot"
            putExtra(Intent.EXTRA_STREAM, uri)
            setClipData(ClipData.newRawUri("", uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. Chiedi al sistema quali di queste app sono realmente installate sul telefono
        val resInfoList = context.packageManager.queryIntentActivities(
            baseIntent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )

        val targetedIntents = mutableListOf<Intent>()

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName

            // Se l'app trovata è nella tua lista consentita
            if (allowedPackages.contains(packageName)) {
                val targetedIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/x-reservabot"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    setClipData(ClipData.newRawUri("", uri)) // FONDAMENTALE anche qui
                    setPackage(packageName)
                    setClassName(packageName, resolveInfo.activityInfo.name)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // FONDAMENTALE: Concedi formalmente il permesso temporaneo a questo pacchetto specifico
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                targetedIntents.add(targetedIntent)
            }
        }

        // 4. Se abbiamo trovato almeno un'app compatibile, mostra il selettore ristretto
        if (targetedIntents.isNotEmpty()) {
            val mainIntent = targetedIntents.removeAt(0)
            val chooser = Intent.createChooser(mainIntent, "Compartir datos de evento").apply {
                if (targetedIntents.isNotEmpty()) {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } else {
            println("Nessuna delle app consentite è installata sul dispositivo.")
        }
    }
//    ///
//    private fun shareFile(file: File) {
//        val authority = "${context.packageName}.provider"
//        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
//
//        // 1. Definisci i pacchetti autorizzati
//        val allowedPackages = listOf(
//            "com.whatsapp",                 // WhatsApp
//            "com.whatsapp.w4b",             // WhatsApp Business
//            "com.google.android.gm",        // Gmail
//            "com.google.android.apps.docs"  // Google Drive
//        )
//
//        // 2. Crea l'Intent base
//        val baseIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/x-reservabot"
//
//            // FONDAMENTALE: Questo dà i permessi di lettura al sistema e alle altre app
//            setClipData(ClipData.newRawUri("", uri))
//
//            putExtra(Intent.EXTRA_STREAM, uri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//
//        // 3. Chiedi ad Android: "Quali app sul telefono sanno gestire questo file?"
//        val resInfoList = context.packageManager.queryIntentActivities(
//            baseIntent,
//            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
//        )
//
//        val targetedIntents = mutableListOf<Intent>()
//
//        // 4. Filtra la lista: tieni SOLO le app che hai autorizzato
//        for (resolveInfo in resInfoList) {
//            val packageName = resolveInfo.activityInfo.packageName
//
//            if (allowedPackages.contains(packageName)) {
//                // Ricrea un intent specifico e mirato per questa esatta app
//                val targetedIntent = Intent(Intent.ACTION_SEND).apply {
//                    type = baseIntent.type
//                    putExtra(Intent.EXTRA_STREAM, uri)
//                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                    // Setta il pacchetto e la classe esatta dell'app bersaglio
//                    setPackage(packageName)
//                    setClassName(packageName, resolveInfo.activityInfo.name)
//                }
//                targetedIntents.add(targetedIntent)
//            }
//        }
//
//        // 5. Costruisci il menu personalizzato
//        if (targetedIntents.isNotEmpty()) {
//            val mainIntent = targetedIntents.removeAt(0)
//            val chooser = Intent.createChooser(mainIntent, "Condividi dati evento")
//
//            if (targetedIntents.isNotEmpty()) {
//                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
//            }
//
//            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(chooser)
//        } else {
//            println("Nessuna app di condivisione compatibile trovata sul dispositivo.")
//            // LOG DI DEBUG: Stampiamo cosa Android ha effettivamente trovato
//            val appTrovate = resInfoList.map { it.activityInfo.packageName }
//            println("Android ha trovato queste app per il file: $appTrovate")
//        }
//    }

}





