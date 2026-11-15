// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get fileName => 'ListaDeReservas';

  @override
  String get tituloPdf => 'REPORTE DE CITAS AGENDADAS';

  @override
  String get subtituloPdf => 'Documento generado el: ';

  @override
  String get testoNomeHeaderPdf => 'Nombre';

  @override
  String get testoDataHeaderPdf => 'Fecha';

  @override
  String get testoOraHeaderPdf => 'Hora';

  @override
  String get testoFinalPage =>
      'Conserve este documento. No tiene validez legal sin la confirmación final de la aplicación.';
}
