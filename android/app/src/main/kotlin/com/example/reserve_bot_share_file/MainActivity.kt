package com.example.reserve_bot_share_file

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.annotation.NonNull

// Imports nativos para PDF y compartir
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity: FlutterActivity() {

    // El nombre del canal DEBE coincidir con el de Dart
    private val CHANNEL = "com.tuapp/compartir"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            if (call.method == "sharePdf") {
                try {

                    // *** CAMBIO CLAVE: RECIBIR LISTA DE MAPAS ***
                    val citas = call.argument<List<Map<String, String>>>("citas")
                    val fileName = call.argument<String>("fileName") ?: "reporte_citas.pdf"
                    val testoPerTablaPdf = call.argument<Map<String, Any>>("testoHeaderPdf") ?: {} as Map<String, Any>
                    val tituloPdf = call.argument<String>("tituloPdf") ?: ""
                    val subtituloPdf = call.argument<String>("subtituloPdf") ?: ""


                    // Generar y compartir el PDF
                    // Pasamos la lista de Map
                    val file = generatePdf(
                        fileName,
                        citas ?: emptyList(),
                        testoPerTablaPdf,
                        tituloPdf,
                        subtituloPdf
                        )

                    shareFile(file)

                    result.success(null)

                } catch (e: Exception) {
                    result.error(
                        "PDF_ERROR",
                        "Error al generar o compartir PDF: ${e.message}",
                        null
                    )
                }
            } else {
                result.notImplemented()
            }
        }
    }

    /**
     * Calcula la altura necesaria para un texto dentro de un ancho límite
     * y devuelve una lista de las líneas que deben dibujarse.
     */
    private fun calculateTextLines(text: String, maxWidth: Float, paint: Paint, lineHeight: Float): Pair<Float, List<String>> {
        val lines = mutableListOf<String>()
        var currentText = text
        var totalHeight = lineHeight // Altura mínima de una línea

        // Si el texto es corto, lo devolvemos tal cual con altura mínima.
        if (paint.measureText(text) <= maxWidth) {
            lines.add(text)
            return Pair(totalHeight, lines)
        }

        // Si el texto es largo, lo separamos por líneas
        while (currentText.isNotEmpty()) {
            val count = paint.breakText(currentText, true, maxWidth, null)
            val line = currentText.substring(0, count)
            lines.add(line)
            currentText = currentText.substring(count).trimStart()

            if (lines.size > 1) {
                totalHeight += lineHeight
            }
        }

        // Devolvemos la altura total requerida y las líneas a dibujar
        return Pair(totalHeight, lines)
    }

    /**
     * Genera un archivo PDF simple a partir de una lista de strings
     * y lo guarda en el directorio caché.
     */
    /**
     * Genera un archivo PDF profesional a partir de una lista de citas estructuradas (Map).
     */
    @Throws(IOException::class)
    private fun generatePdf(
        fileName: String,
        citas: List<Map<String, String>>,
        pdfHeaderText : Map<String, Any>,
        tituloPdf : String,
        subtituloPdf : String
        ): File {

        val testoNome = pdfHeaderText["nome"] as String
        val testoData = pdfHeaderText["data"] as String
        val testoOra = pdfHeaderText["ora"] as String

        //
        val document = PdfDocument()
        val pageHeight = 842 // A4 alto (puntos)
        val pageWidth = 595 // A4 ancho (puntos)
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // --- CONSTANTES DE DISEÑO ---
        val marginX = 40f
        var currentY = 50f // Posición Y inicial
        val rowHeight = 30f // Altura de cada fila de la tabla

        // --- 1. INSERTAR IMAGEN / LOGO ---
        // NOTA: Debes agregar tu archivo de imagen (ej: logo.png) en la carpeta 'res/drawable' de tu módulo de Android.
        try {
            // Usamos contextCompat para compatibilidad y obtener un Drawable
            val logo = ContextCompat.getDrawable(context, R.drawable.splash) // **Reemplaza 'logo'**
            logo?.let {(it)

                val logoWidth = 100
                val logoHeight = 100
                it.setBounds(marginX.toInt(), currentY.toInt(), (marginX + logoWidth).toInt(), (currentY + logoHeight).toInt())
                it.draw(canvas)
                currentY += logoHeight + 10f // Ajustar posición Y después del logo
            }
        } catch (e: Exception) {
            // Si el drawable 'logo' no existe o hay error, no se dibuja el logo.
        }


        // --- 2. TÍTULO PRINCIPAL (LETRAS MÁS GRANDES Y NEGOCIO) ---
        //------ PARA DIBUJAR LAS LINEAS DEL PDF
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1A237E") // Un azul oscuro (puedes usar el color de tu marca)
            textSize = 24f
            isFakeBoldText = true
            strokeWidth = 1f // Grosor
            style = Paint.Style.FILL_AND_STROKE
        }
        // Lo colocamos a la derecha del logo o en el margen si no hay logo
        canvas.drawText(tituloPdf, marginX, currentY + 20f, titlePaint)
        currentY += 60f // Espacio después del título


        // --- 3. SUBTÍTULO Y FECHA DE GENERACIÓN ---
        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
        }
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        canvas.drawText("$subtituloPdf ${dateFormat.format(java.util.Date())}", marginX, currentY, subtitlePaint)
        currentY += 30f // Espacio antes de la tabla

        // --- 4. CREAR LA TABLA CON LA INFORMACIÓN DE LAS CITAS ---
        val textPdfHeaders = listOf(testoNome, testoData, testoOra)

        // Claves del Map de las citas
        val valueTextPdfHeaders = listOf("nome", "data", "ora")

        Log.d("generatePDF", "columnKey: $valueTextPdfHeaders columnHeaders: $textPdfHeaders")

        val totalWidth = pageWidth - 2 * marginX

        // Definición de anchos de columna (ajustar porcentajes según necesidad)
        val columnWidths = floatArrayOf(
            totalWidth * 0.45f, // 30% Nombre
            totalWidth * 0.40f, // 30% Fecha
            totalWidth * 0.25f, // 15% Hora
           // totalWidth * 0.35f  // 35% Motivo
        )

        // --- Pinceles para la Tabla ---
        val headerPaint = Paint().apply { color = Color.parseColor("#EEEEEE") } // Fondo gris claro
        val headerTextPaint = Paint(subtitlePaint).apply { isFakeBoldText = true; textSize = 20f } // Texto de encabezado
        val contentTextPaint = Paint(subtitlePaint).apply { isFakeBoldText = false; textSize = 18f } // Texto de contenido
        val linePaint = Paint().apply { color = Color.parseColor("#BDBDBD"); strokeWidth = 1f } // Línea divisoria

        // --- A. Dibujar Encabezado de la Tabla ---
        var currentX = marginX
        for (i in textPdfHeaders.indices) {
            // Dibujar fondo de encabezado
            canvas.drawRect(currentX, currentY, currentX + columnWidths[i], currentY + rowHeight, headerPaint)
            // Dibujar texto de encabezado
            canvas.drawText(textPdfHeaders[i], currentX + 5f, currentY + (rowHeight / 2) + 5f, headerTextPaint)
            currentX += columnWidths[i]
        }
        currentY += rowHeight

        // --- B. Dibujar Filas de Datos (MODIFICADO) ---
        for ((index, cita) in citas.withIndex()) {

            // 1. CALCULAR LA ALTURA NECESARIA PARA ESTA FILA
            var maxTextHeight = rowHeight // Empezamos con la altura mínima (ej: 30f)
            val lineData = mutableMapOf<String, Pair<Float, List<String>>>()

            // Iteramos sobre las claves para encontrar el texto más largo y calcular la altura máxima
            for (i in valueTextPdfHeaders.indices) {
                val key = valueTextPdfHeaders[i]
                val text = cita[key] ?: ""
                val width = columnWidths[i]

                // Usamos una altura base de línea (e.g., 20f, debe ser mayor que el textSize)
                val lineHeight = contentTextPaint.textSize + 2f // 18 + 2 = 20f

                // Calculamos las líneas y la altura requerida para esta celda
                val (height, lines) = calculateTextLines(text, width - 10f, contentTextPaint, lineHeight)

                lineData[key] = Pair(height, lines)
                if (height > maxTextHeight) {
                    maxTextHeight = height // Encontramos la celda más alta
                }
            }

            // Añadir un padding vertical extra para el "aire"
            val calculatedRowHeight = maxTextHeight + 10f

            // --- 2. Manejo de Salto de Página (USANDO calculatedRowHeight) ---
            if (currentY + calculatedRowHeight > pageHeight - marginX) {
                // Lógica de salto de página (mantenemos la tuya)
                document.finishPage(page)
                // ... (código para crear nueva página y redibujar encabezado) ...
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                currentY = 50f

                // Volver a dibujar encabezado en la nueva página (lo puedes mover a una función separada)
                var headerX = marginX
                for (i in textPdfHeaders.indices) {
                    canvas.drawRect(headerX, currentY, headerX + columnWidths[i], currentY + rowHeight, headerPaint)
                    canvas.drawText(textPdfHeaders[i], headerX + 5f, currentY + (rowHeight / 2) + 5f, headerTextPaint)
                    headerX += columnWidths[i]
                }
                currentY += rowHeight
                // Fin de la lógica de salto de página
            }

            // --- 3. DIBUJAR LA FILA CON LA NUEVA ALTURA ---

            // Dibuja línea horizontal superior de la fila (opcional, si no la cubre el encabezado)
            // canvas.drawLine(marginX, currentY, pageWidth - marginX, currentY, linePaint)

            var currentX = marginX
            for (i in valueTextPdfHeaders.indices) {
                val key = valueTextPdfHeaders[i]
                val (height, lines) = lineData[key]!!
                val width = columnWidths[i]

                // DIBUJAR LÍNEA VERTICAL DIVISORIA (SOLICITADA)
                if (i > 0) {
                    // Dibujar la línea vertical en el borde izquierdo de esta columna
                    canvas.drawLine(currentX, currentY, currentX, currentY + calculatedRowHeight, linePaint)
                }

                // Dibujar el texto línea por línea
                var textY = currentY + (height - lines.size * (contentTextPaint.textSize + 2f)) / 2 + contentTextPaint.textSize
                // La línea anterior centra verticalmente el texto

                for (line in lines) {
                    // currentX + 5f es el padding horizontal
                    canvas.drawText(line, currentX + 5f, textY, contentTextPaint)
                    textY += contentTextPaint.textSize + 2f // Aumenta para la siguiente línea
                }

                currentX += width
            }

            // Dibujar la línea vertical final (borde derecho de la tabla)
            canvas.drawLine(currentX, currentY, currentX, currentY + calculatedRowHeight, linePaint)

            // Dibuja línea horizontal inferior de la fila
            canvas.drawLine(marginX, currentY + calculatedRowHeight, pageWidth - marginX, currentY + calculatedRowHeight, linePaint)

            currentY += calculatedRowHeight // Pasamos a la posición Y de la siguiente fila
        }

        // --- 5. NOTA AL PIE DE PÁGINA ---
        currentY += 40f
        val footerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }
        canvas.drawText("Conserve este documento. No tiene validez legal sin la confirmación final de la aplicación.", marginX, pageHeight - 30f, footerPaint)


        // --- 6. Finalizar y guardar ---
        document.finishPage(page)

        val file = File(context.cacheDir, fileName)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.close()

        return file
    }

    // La función 'shareFile' permanece exactamente igual.


    /**
     * Inicia el Intent nativo de "Compartir" para un archivo dado.
     */
    private fun shareFile(file: File) {
        // 1. Obtener la URI segura usando FileProvider
        // La "autoridad" DEBE coincidir con la definida en AndroidManifest.xml

        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        // 2. Crear el Intent para compartir
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf" // Definimos el tipo de archivo
        intent.putExtra(Intent.EXTRA_STREAM, uri) // La URI del archivo
        intent.putExtra(Intent.EXTRA_SUBJECT, file.name) // Asunto (para email)

        // 3. Otorgar permisos de lectura a la app que reciba el archivo
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // 4. Crear y mostrar el diálogo "Chooser"
        val chooser = Intent.createChooser(intent, "Compartir PDF con...")

        // Iniciar la actividad de compartir
        activity.startActivity(chooser)
    }

}
