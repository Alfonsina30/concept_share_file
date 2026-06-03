import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:reserve_bot_share_file/l10n/app_localizations.dart';
import 'package:reserve_bot_share_file/logic_json_intent/prenotazioni_controller.dart';
import 'package:reserve_bot_share_file/logica_vieja/lista_citas.dart';
import 'package:provider/provider.dart';

void main() {
  //
  WidgetsFlutterBinding.ensureInitialized();

  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => PrenotazioniController()),
    ],
    child: const MaterialApp(
      home: CitasPage(),
      debugShowCheckedModeBanner: false,
      locale: Locale('es'),
      supportedLocales: [Locale('es')],
      localizationsDelegates: [
        AppLocalizations.delegate,
        //-- widgets de material soporten diferentes idiomas
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate
      ],
    ),
  ) as Widget);
}

// --- Tu página con la lista ---
class CitasPage extends StatefulWidget {
  const CitasPage({super.key});

  @override
  State<CitasPage> createState() => _CitasPageState();
}

class _CitasPageState extends State<CitasPage> {
  @override
  Widget build(BuildContext context) {
    //
    final prenotazioniController = context.watch<PrenotazioniController>();
    //
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis Citas'),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () {
              shareFile();
            },
            tooltip: 'Compartir Lista (PDF)',
          ),
        ],
      ),
      body: (prenotazioniController.isLoading)
          ? Center(
              child: CircularProgressIndicator(),
            )
          : ListView.builder(
              itemCount: prenotazioniController.prenotazioni.length,
              itemBuilder: (context, index) {
                //
                final map = prenotazioniController.prenotazioni[index];
                final nome = map["nome"];
                final data = map["data"];
                final ora = map["ora"];

                return Padding(
                  padding: EdgeInsets.symmetric(
                    horizontal: 16.0,
                  ),
                  child: ListTile(
                    leading: Icon(Icons.person),
                    title: Text(
                      nome,
                      style: TextStyle(
                          fontFamily: 'monospace', fontWeight: FontWeight.bold),
                    ),
                    //
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          data,
                          style: TextStyle(
                              fontFamily: 'monospace',
                              fontWeight: FontWeight.bold),
                        ),
                        //
                        Text(
                          ora,
                          style: TextStyle(
                              fontFamily: 'monospace',
                              fontWeight: FontWeight.bold),
                        ),
                        //
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }

//************************************************** USAR  *****************************/
  Future<void> shareFile() async {
//
    const MethodChannel platformChannel =
        MethodChannel('com.reserve_bot/compartirPdf');
//TODO:
    try {
      //**IMPORTANTE */
      final items = [
        {
          "fileName": "eventi.reservabot"
        } //--cuando se recibe el file se filtra este key & value
      ];

      // Convertir la List<Cita> a List<Map<String, String>>
      final List<Map<String, String>> citasMapList =
          ListaCitas.citas().map((cita) {
        return cita.toMap();
      }).toList();

      items.addAll(citasMapList);

      log("CITASSS $items");

      final toJson = jsonEncode(items);

      await platformChannel.invokeMethod('sharePdf', toJson);
    } on PlatformException catch (e) {
      print("Fallo al compartir el PDF: ${e.message}");
    }
  }
}
