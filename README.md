# ChatServerProject (DE)

ChatServer zum Bestellen von Essen. Die Nutzer können interaktiv Essen bestellen und sich dabei austauschen.


## Setup

1. Project klonen:

      ```bash
      git clone https://github.com/lumo03/ChatServerProject.git
      ```
2. Projekt in IDE öffnen:
      - Intellij (wenn CLI installiert): 
           ```bash
           idea ChatServerProject
           ```
      - Visual Studio Code (wenn CLI installiert): 
           ```bash
           code ChatServerProject
           ```
3. Setup JDK (OpenJDK 19)


## Besondere Merkmale 

- Befehle konnen nur im korrekten Kontext verwendet werden
  
  Bsp: `/add` und `/remove` funktionieren nur, wenn eine Bestellung gestartet wurde

- Interaktive GUI:
  - Verbindung mit Socket
  - Automatisch scrollende "Konsolenausgabe"
  - Zusätzliche Befehle wie `/clear` und `/reconnect`
  - `/quit` verändert das Verhalten der GUI
  - Benutzername wird oben im Titel angezeigt
- Verwendung komplexer Programmierpraktika, welche über das im Unterricht gelernte hinausgehen:
  - Lambda - Funktionen in Kombination mit funktionalen Interfaces
  - ENUM Types
  - ThreadScheduling mithilfe eines ThreadPools
  - Stream API
  - Collections API
- Professionelle Syntax wie `this::broadcast`
- Sinvolle Struktur
- Funktionen beachten (fast) alle Eventualitäten
  
  Bsp: `removeCartItems()` überprüft bei jedem Item, ob es überhaupt in der Liste existiert, um Exceptions zu vermeiden und um klar sagen zu können, welche Items denn wirklich entfernt wurden

- Code-Dopplungen wurden vermieden, dafür wurden viele Funktionen verwendet
- Helfer-Funktionen(Wrapper-Funktionen) zur Vereinfachung der Schnittstellen


## Zusätzliche Informationen
- Verwendete IDE: IntelliJ IDEA 2022.3.3 (Community Edition)
- Verwendte JDK: openjdk version "19.0.2" 2023-01-17