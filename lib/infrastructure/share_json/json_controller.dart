import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:reserve_bot_share_file/domain/entity/citas_entity.dart';
import 'package:reserve_bot_share_file/infrastructure/model/citas_model.dart';
import 'package:reserve_bot_share_file/presentation/shared/example_citas.dart';

class JsonController extends ChangeNotifier {
  //
  final List<Cita> _citas = [];
  static String fileName = 'file1235';
  List<Cita> get citas => _citas;

  bool isLoading = true;

  // Ora usiamo EventChannel invece di MethodChannel
  static const EventChannel _eventChannel =
      EventChannel('com.example.app_share/file');

  // Variabile per mantenere viva l'iscrizione allo stream
  static StreamSubscription? _subscription;

  JsonController() {
    _startListen();
  }

  _startListen() {
    _subscription = _eventChannel.receiveBroadcastStream().listen(
      (dynamic event) async {
        final String jsonString = event as String;

        if (jsonString == "NO_DATA") {
          await _loadDatabase();

          return;
        }

        try {
          final dynamic decodedData = jsonDecode(jsonString);

          if (decodedData is List && decodedData.isNotEmpty) {
            //
            List<Map<String, dynamic>> response =
                List<Map<String, dynamic>>.from(decodedData);

            print('lista recibidaaaa $response');

//---FILTRO PARA NO RECIBIR ARCHIVOS QUE NO SON DE LA APP
            final hasKey = response[0].containsKey("fileName") &&
                response[0].containsValue(fileName);

            if (!hasKey) {
              print("Il file non appartiene a questa app.");
              // IMPORTANTE: Anche se fallisce, sblocchiamo la UI per evitare la schermata nera
              isLoading = false;
              notifyListeners();
              return;
            }

            response.removeAt(0);

            log('listaFinale $response');

            importNewCitas(response);
          }
        } catch (e) {
          print("Il file non è un JSON valido: $e");
        }
      }, // Questo va dentro l'onError dell'EventChannel
      onError: (dynamic error) {
        if (error is PlatformException) {
          if (error.code == "FILE_TOO_LARGE") {
            print("Errore: Il file selezionato è troppo pesante.");
          } else {
            print("Errore generico in lettura: ${error.message}");
          }
        }
      },
      cancelOnError:
          false, // Se c'è un errore, continua ad ascoltare per futuri file
    );
  }

  Future<void> _loadDatabase() async {
    isLoading = false;

    notifyListeners();
  }

  // Funzione chiamata quando arriva un file da WhatsApp
  Future<void> importNewCitas(List<Map<String, dynamic>> data) async {
    final citas = data.map((map) => CitaModel.fromJson(map)).toList();
    // citas.clear();
    _citas.addAll(citas);
    isLoading = false;

    notifyListeners();
  }

  Future<void> shareJson() async {
//
    const MethodChannel platformChannel =
        MethodChannel('com.example.sharejson');

    try {
      //**IMPORTANTE */
      final items = [
        {
          "fileName": fileName
        } //Quando il file viene ricevuto, questa chiave e questo valore vengono filtrati.
      ];

      final List<Map<String, String>> citasMapList =
          ExampleCitas.citas().map((cita) {
        return CitaModel(name: cita.name, hour: cita.hour, date: cita.date)
            .toMap();
      }).toList();

      items.addAll(citasMapList);

      log("CITASSS $items");

      final toJson = jsonEncode(items);

      await platformChannel.invokeMethod('shareJson', toJson);
    } on PlatformException catch (e) {
      print("Exception PDF: ${e.message}");
    }
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}
