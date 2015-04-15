# nosql
Example programs from a database seminar at AIFB, KIT provided for the attendees
As the audience was German, the following README is in German.

## Überblick
Code Beispiele für einfache Integration der folgenden vier NoSQL-Datenbanksysteme:
* Redis
* MongoDB
* Cassandra
* Virtuoso

Redis und MongoDB waren dabei lokal (OS X) installiert. Cassandra und Virtuoso wurden in einer Debian/Ubuntu VM betrieben.

## Installation der Datenbanksysteme
Redis: unter OS X mit Homebrew
```bash
brew install redis
```

MongoDB: unter OS X mit Homebrew
```bash
brew install mongodb
```

Cassandra:
Siehe https://wiki.apache.org/cassandra/GettingStarted

Virtuoso:
incl. lokalem DBPedia Dump: https://joernhees.de/blog/2014/11/10/setting-up-a-local-dbpedia-2014-mirror-with-virtuoso-7-1-0/


## Nutzen der Code-Beispiele
Die vorliegenden Code-Beispiele setzen sich auch einer einfachen Webapplikation auf Basis von untertow (Sehr schneller moduarer Java Webserver, http://undertow.io/).
Für die einzelnen Datenbanksysteme existiert jeweils ein Handler.
Nach dem Checkout, müssen direkt im Code die IP-Adressen der Cassandra bzw. Virtuoso Server angepasst werden.
Als Build Management Tool wird Gradle (https://gradle.org/) eingesetzt. Vor dem ersten Start müssen noch notwendigen Abhängigkeiten ausgelöst werden:
```bash
gradle build
```
Danach kann das Programm wie jede andere Java Applikation gestartet werden.


