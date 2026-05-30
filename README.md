# reserve_bot_share_file

**COMPORTAMIENTO CUANDO SE ABRE EL FILE DESDE(WHATSAPP, DRIVE, GMAIL)**

-  WHATSAPP:
Cuando el file se abre desde whatsapp, android pone arriba de whatsapp la app Reserve bot con la info del file. el mismo compartamiento es si la app ya esta o no abierta(foreground, background). Con la diferencia que la app se abre solo para ponerse arriba no para ponerse como otra ventana. 

- DRIVE | GMAIL : 
COMPORTAMIENTO SEA EN BACKGROUND & FOREGROUND: Cuando el file se abre desde Drive, drive abre en otra pantalla la aplicacion no la pone arriba





Proyecto realizado por Alfonsina para que el usuario en la app ReserveBot pueda compartir las citas(appuntamenti) que registro el bot en whatsapp por ejemplo, con otras app del sistema android 


In Android, ci sono due scenari completamente diversi in cui la tua app può ricevere un file da WhatsApp, e devi gestirli entrambi. Ecco perché l'Intento viene "ascoltato" in due punti diversi:

Scenario 1: L'app è completamente chiusa (Cold Start).
Quando l'utente preme il file su WhatsApp e la tua app era chiusa, Android la avvia da zero. In questo caso, Android "nasconde" il file dentro la variabile di sistema chiamata semplicemente intent. Il posto migliore per leggerla all'avvio è dentro configureFlutterEngine.

Scenario 2: L'app è già aperta, ma in background (Warm Start).
Se l'utente stava già usando la tua app, poi è andato su WhatsApp e ha cliccato il file, Android non riavvia l'app da zero. Salta completamente configureFlutterEngine e chiama invece una funzione speciale: onNewIntent(intent).

**Ecco il flusso logico perfetto che dovresti implementare**

Android spara la stringa JSON.

Un Servizio Globale (inizializzato nel main) ascolta il canale.

Il Servizio decodifica il JSON ed estrae la List<Map>.

Il Servizio invia questa lista al tuo State Manager (Provider, Riverpod, GetX, Bloc, o anche un semplice ValueNotifier).

Lo State Manager salva i dati in SQLite e avvisa la UI, che si aggiorna magicamente da sola mostrando le nuove prenotazioni.

2. Il Filtro in Dart (L'"Ispettore" per i Dati)
Una volta che Kotlin ha confermato che il file è piccolo e sembra testo, lo invia a Dart. Il compito di Dart è ispezionare il contenuto:

È un JSON valido?

È una List<Map>?

Contiene le chiavi giuste (es. nome_evento)?
