import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Importante para MethodChannel

// Estructura de datos más profesional
class Cita {
  final String nombre;
  final String hora;
  final String fecha;
  final String codigo;

  Cita(
      {required this.nombre,
      required this.hora,
      required this.fecha,
      required this.codigo});

  // Método para convertir el objeto Cita a un Map<String, String>
  Map<String, String> toMap() {
    return {
      'nombre': nombre,
      'fecha': fecha,
      'hora': hora,
      'codigo': codigo,
    };
  }
}

// --- Para probar esta página ---
void main() {
  runApp(const MaterialApp(
    home: CitasPage(),
    debugShowCheckedModeBanner: false,
  ));
}

// --- Tu página con la lista ---
class CitasPage extends StatefulWidget {
  const CitasPage({super.key});

  @override
  State<CitasPage> createState() => _CitasPageState();
}

class _CitasPageState extends State<CitasPage> {
  // --- DATOS DE EJEMPLO (TU LISTA DE STRINGS) ---

  final List<Cita> _listaDeCitas = [
    Cita(
        nombre: 'Juan Pérez García García',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García García García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
     Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
         Cita(
        nombre: 'Juan Pérez',
        hora: '10:00 AM',
        fecha: 'giovedi 24 Marzo',
        codigo: ' Consulta General'),
    Cita(
        nombre: 'Ana García',
        hora: '11:30 AM ',
        fecha: 'giovedi 24 Marzo',
        codigo: 'Examen Dental'),
    Cita(
        nombre: ' Carlos Sánchez',
        hora: '01:00 PM',
        fecha: 'giovedi 30 Marzo',
        codigo: 'Seguimiento'),
  ];

  bool _isLoading = false;

  Future<void> shareCitasPdf(List<Cita> citas) async {

    

    DateTime data = DateTime.now();
    
    // --- DEFINIR EL CANAL ---
    // El nombre debe ser único y coincidir exactamente con el de Kotlin
    const MethodChannel _platformChannel = MethodChannel('com.tuapp/compartir');

    if (_isLoading) return; //TODO: PENDIENTE COMPLETAR LA FUNCION _ISLOADING

    try {
      // Convertir la List<Cita> a List<Map<String, String>>
      final List<Map<String, String>> citasMapList =
          citas.map((cita) => cita.toMap()).toList();

      await _platformChannel.invokeMethod('sharePdf', {
        'citas':
            citasMapList, // <--- La llave debe coincidir con 'citas' en Kotlin
        'fileName': 'ListaDeReservas.pdf',
      });
    } on PlatformException catch (e) {
      print("Fallo al compartir el PDF: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis Citas'),
        actions: [
          // Botón para disparar la acción de compartir
          if (_isLoading)
            const Padding(
              padding: EdgeInsets.all(16.0),
              child: SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(color: Colors.white),
              ),
            )
          else
            IconButton(
              icon: const Icon(Icons.share),
              onPressed: () {
                shareCitasPdf(_listaDeCitas);
              },
              tooltip: 'Compartir Lista (PDF)',
            ),
        ],
      ),
      // Mostramos la lista en la UI para que veas los datos
      body: ListView.builder(
        itemCount: _listaDeCitas.length,
        itemBuilder: (context, index) {
          final cita = _listaDeCitas[index];

          return Padding(
            padding: EdgeInsets.symmetric(
              horizontal: 16.0,
            ),
            // Añadimos un poco de estilo para que parezca un reporte
            child: ListTile(
              title: Text(
                cita.nombre,
                style: TextStyle(
                    fontFamily: 'monospace', fontWeight: FontWeight.bold),
              ),
              //
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(Icons.calendar_month),
                      Text('Fecha:  ${cita.hora}'),
                    ],
                  ),
                  Row(children: [
                    Icon(Icons.timer_sharp),
                    Text('Hora:   ${cita.fecha}')
                  ]),
                  Text('Motivo: ${cita.codigo}')
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
