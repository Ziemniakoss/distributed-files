# Rozproszony system plików

System do przechowywania wielu kopii plików na różnych serwerach.

## Klient 

Klient służy do przeglądania wszystkich dostępnych plików w systemie.
Można za jego pomocą również dodać nowe pliki do poszczególnych serwerów. 
Po wybraniu pliku z systemu można sprawdzić jego informacje i z jakich serwerów można go
pobrać.

## Server plików

Serwer plików służy jedynie do przechowywania plików i serwowania ich.
Nie ma on żadnych informacji o innych maszynach w systemie poza serwerem z bazą danych.

## Uruchamianie

Aby uruchomić system złożony z 3 serweów plików, bazy danych i klienta należy wykonać w folderze
głównym projektu wpisać

```bash
docker-compose build --parallel
docker-compose up
```

lub na maszynach z zainstalowaną wersją docker-compose niewspierającą 
równoległego budoawania kontenerów 

```bash
docker-compose up --build
```

Po wykonaniu komend na localhost:8080 powinien być dostępny klient