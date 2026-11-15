import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:reserve_bot_share_file/domain/entity/citas_entity.dart';
import 'package:reserve_bot_share_file/presentation/l10n/app_localizations.dart';
import 'package:reserve_bot_share_file/infrastructure/model/citas_model.dart';

class SharePdf {
  static Future<void> shareCitasPdf(
      {required List<Cita> citas, required BuildContext context}) async {
    //
    const MethodChannel platformChannel =
        MethodChannel('com.example_share_Pdf');

    final fileName = AppLocalizations.of(context)!.fileName;
    final tituloPdf = AppLocalizations.of(context)!.tituloPdf;
    final subtituloPdf = AppLocalizations.of(context)!.subtituloPdf;
    final locName = AppLocalizations.of(context)!.testoNomeHeaderPdf;
    final locData = AppLocalizations.of(context)!.testoDataHeaderPdf;
    final locHour = AppLocalizations.of(context)!.testoOraHeaderPdf;
    final finalPageText = AppLocalizations.of(context)!.testoFinalPage;

    try {
      // Convertir la List<Cita> a List<Map<String, String>>
      final List<Map<String, String>> citasMapList = citas.map((cita) {
        return CitaModel(
          name: cita.name,
          hour: cita.hour,
          date: cita.date,
        ).toMap();
      }).toList();

      await platformChannel.invokeMethod('sharePdf', {
        'citas':
            citasMapList, // <--- La llave de la clase de Cita debe coincidir con 'citas' en Kotlin
        'fileName': '$fileName.pdf',
        'tituloPdf': tituloPdf,
        'subtituloPdf': subtituloPdf,
        'testoFinalPage': finalPageText,
        'testoHeaderPdf': {"nome": locName, "data": locData, "ora": locHour}

        //,
        //
      });
    } on PlatformException catch (e) {
      print("Fallo al compartir el PDF: ${e.message}");
    }
  }
}
