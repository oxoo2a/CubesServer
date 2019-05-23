# Cubes Server

## Verwendung
Aktuell steht ein JAR-File des Servers im Wurzelverzeichnis des Repositories. Der Server wird mit einem Argument, der Portnummer, gestartet:

```
java -jar CubesServer.jar 4321
```

Der Server gibt fleißig aus, was er so empfängt und sendet. Das hilft hoffentlich für das Debugging. Sobald man die Return-Taste drückt, terminiert der Server, nachdem er an alle bekannten Clients eine `End`-Nachricht gesendet hat (siehe unten):

## Nachrichten
Alle Nachrichten sind in JSON kodierte Maps (Liste von
Key-Value-Paaren). Keys und Values sind einfache Strings. Der verwendete
Zeichensatz ist UTF-8.

Der Client meldet sich beim Server mittels einer `Hello`-Nachricht an:

### Hello
- Key = "type", Value = "hello"
- Key = "name", Value = "Name des Clients"
- Beispiel: `{ "type" : "hello", "name" : "me" }`

Der Server antwortet mit einer `Welcome`-Nachricht:

### Welcome
- Key = "type", Value = "welcome"
- Key = "position", Value = "x,y,z"
- Beispiel: `{ "type" : "welcome", "position" : "10,-3,5" }`

Die Koordinate (x,y,z) legt die Position des Clients im globalen
Koordinatensystem fest. Koordinaten werden im Blockraster angegeben und
sind immer ganze Zahlen.

Nach der Anmeldung wartet der Server auf Chat-Aufträge von einem Client
und gibt diese an den jeweiligen Empfänger weiter. Ein Sonderfall sind
Weltnachrichten, die an alle angemeldeten Clients weitergegeben werden.

### Chat-Auftrag (Client -> Server)
- Key = "type", Value = "chat"
- Key = "receiver", Value = "Name des Empfängers"
- Key = "content", Value = "Textnachricht"
- Beispiel: `{ "type" : "chat", "receiver" : "someone", "content" :
  "Hello cube" }`

Der Empfängername `"world"` entspricht einer Weltnachricht, die an alle
angemeldeten Clients versendet wird.

### Chat-Nachricht (Server -> Client)
- Key = "type", Value = "chat"
- Key = "sender", Value = "Name des Senders"
- Key = "content", Value = "Textnachricht"
- Key = "world", Value = "true"|"false"
- Beispiel: `{ "type" : "chat", "sender" : "me", "content" : "Hello
  cube", "world" : "false" }`

### The End (Server -> Client)
Der Server kann durch den Benutzer beendet werden. In diesem Fall sendet er entsprechende Terminierungsnachrichten an alle angeschlossenen Clients.

- Key = "type", Value = "end"
- Beispiel: `{ "type": "end" }`
