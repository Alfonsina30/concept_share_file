package com.example.reserve_bot_share_file

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.annotation.NonNull


class MainActivity: FlutterActivity() {

    //PDF SERVICE +  SHARED FILE
    private val channelPdf = "com.reserve_bot/compartirPdf"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        //------------------PDF FILE ---------------------
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelPdf
        ).setMethodCallHandler { call, result ->
            if (call.method == "sharePdf") {
                try {

                    // *** CAMBIO CLAVE: RECIBIR LISTA DE MAPAS ***
                    val citas = call.argument<List<Map<String, String>>>("citas")
                    val fileName = call.argument<String>("fileName") ?: "reporte_citas.pdf"
                    val testoPerTablaPdf = call.argument<Map<String, Any>>("testoHeaderPdf") ?: {} as Map<String, Any>
                    val tituloPdf = call.argument<String>("tituloPdf") ?: ""
                    val subtituloPdf = call.argument<String>("subtituloPdf") ?: ""
                    val testoFinalPage = call.argument<String>("testoFinalPage") ?: ""


                    // Generar y compartir el PDF
                    val pdfService = SharedFilePdf(this.applicationContext)
                    pdfService.generatePdfAndSharedFile(
                        fileName,
                        citas ?: emptyList(),
                        testoPerTablaPdf,
                        tituloPdf,
                        subtituloPdf,
                        testoFinalPage
                        )

                    result.success(null)

                } catch (e: Exception) {
                    result.error(
                        "PDF_ERROR",
                        "Error al generar o compartir PDF: ${e.message}",
                        null
                    )
                }

            }  //----MethodChannel pdf

            else {
                result.notImplemented()
            }
        }


        //-----
    }

}
