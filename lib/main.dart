import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:reserve_bot_share_file/l10n/app_localizations.dart';
import 'package:reserve_bot_share_file/share_json/json_controller.dart';
import 'package:provider/provider.dart';
import 'package:reserve_bot_share_file/share_pdf/share_pdf.dart';
import 'package:reserve_bot_share_file/shared/example_citas.dart';

void main() {
  //
  WidgetsFlutterBinding.ensureInitialized();

  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => JsonController()),
    ],
    child: const MaterialApp(
      home: CitasPage(),
      debugShowCheckedModeBanner: false,
      locale: Locale('es'),
      supportedLocales: [Locale('es')],
      localizationsDelegates: [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate
      ],
    ),
  ) as Widget);
}

class CitasPage extends StatefulWidget {
  const CitasPage({super.key});

  @override
  State<CitasPage> createState() => _CitasPageState();
}

class _CitasPageState extends State<CitasPage> {
  @override
  Widget build(BuildContext context) {
    //
    final citasController = context.watch<JsonController>();
    //
    return Scaffold(
      appBar: AppBar(
        title: const Text('My home'),
        actions: [
          TextButton(
            onPressed: () async =>
                await context.read<JsonController>().shareJson(),
            child: Text('share Json'),
          ),
          //
          TextButton(
            onPressed: () async {
              await SharePdf.shareCitasPdf(
                  citas: ExampleCitas.citas(), context: context);
            },
            child: Text('share PDF'),
          ),
        ],
      ),
      body: (citasController.isLoading)
          ? Center(
              child: CircularProgressIndicator(),
            )
          : ListView.builder(
              itemCount: citasController.citas.length,
              itemBuilder: (context, index) {
                //
                final citas = citasController.citas[index];

                return Padding(
                  padding: EdgeInsets.symmetric(
                    horizontal: 16.0,
                  ),
                  child: ListTile(
                    leading: Icon(Icons.person),
                    title: Text(
                      citas.name,
                      style: TextStyle(
                          fontFamily: 'monospace', fontWeight: FontWeight.bold),
                    ),
                    //
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          citas.date,
                          style: TextStyle(
                              fontFamily: 'monospace',
                              fontWeight: FontWeight.bold),
                        ),
                        //
                        Text(
                          citas.hour,
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
}
