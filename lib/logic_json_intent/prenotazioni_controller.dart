import 'dart:async';
import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PrenotazioniController extends ChangeNotifier {
  //
  final List<Map<String, dynamic>> _prenotazioni = [];

  List<Map<String, dynamic>> get prenotazioni => _prenotazioni;

  bool isLoading = true;

  // Ora usiamo EventChannel invece di MethodChannel
  static const EventChannel _eventChannel =
      EventChannel('com.reservabot.app/file_events');

  // Variabile per mantenere viva l'iscrizione allo stream
  static StreamSubscription? _subscription;

  PrenotazioniController() {
    _iniziaAscolto();
  }

  _iniziaAscolto() {
    _subscription = _eventChannel.receiveBroadcastStream().listen(
      (dynamic event) async {
        final String jsonString = event as String;

        if (jsonString == "NO_DATA") {
          await _caricareVecchiDatiDatabase();

          return;
        }

        try {
          final dynamic decodedData = jsonDecode(jsonString);

          if (decodedData is List && decodedData.isNotEmpty) {
            //
            List<Map<String, dynamic>> listaFinale =
                List<Map<String, dynamic>>.from(decodedData);

            print('lista recibidaaaa $listaFinale');

//---FILTRO PARA NO RECIBIR ARCHIVOS QUE NO SON DE LA APP
            final hasKey = listaFinale[0].containsKey("fileName") &&
                listaFinale[0].containsValue("eventi.reservabot");

            if (!hasKey) return;

            listaFinale.removeAt(0);

            log('listaFinale $listaFinale');

            importaNuovePrenotazioni(listaFinale);
          }
        } catch (e) {
          print("Il file non è un JSON valido: $e");
        }
      }, // Questo va dentro l'onError dell'EventChannel
      onError: (dynamic error) {
        if (error is PlatformException) {
          if (error.code == "FILE_TOO_LARGE") {
            print("Errore: Il file selezionato è troppo pesante.");
            // TODO: Mostra una SnackBar rossa: "File non supportato"
          } else {
            print("Errore generico in lettura: ${error.message}");
          }
        }
      },
      cancelOnError:
          false, // Se c'è un errore, continua ad ascoltare per futuri file
    );
  }

//TODO: SOLO PARA SIMULACION CAMBIAR CON LA LLAMADA AL DATABASE
  Future<void> _caricareVecchiDatiDatabase() async {
    isLoading = false;

    notifyListeners();
  }

  // Funzione chiamata quando arriva un file da WhatsApp
  Future<void> importaNuovePrenotazioni(
      List<Map<String, dynamic>> nuoviDati) async {
    // 1. Aggiungiamo i dati alla lista in memoria
    _prenotazioni.addAll(nuoviDati);
    isLoading = false;

    //  TODO: Salvali nel tuo database SQLite locale
    // await DatabaseHelper.insertList(nuoviDati);

    // 3. Notifica la UI che i dati sono cambiati!
    notifyListeners();
  }

  @override
  void dispose() {
    _subscription
        ?.cancel(); // Spegne l'ascolto nativo quando il controller muore
    super.dispose();
  }
}
