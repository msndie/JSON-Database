# JSON-Database
Данные хранятся в файле на стороне сервера, сервер мультипоточный, для параллелизации используется Executor.
В клиентской части используется JCommander для парсинга аргументов командной строки.

# Аргументы для клиентской части

* -t - тип команды, exit для отключения сервера, set для добавления новой записи или изменения существующей, get для получения значения, delete для удаления ключа и значения
* -k - ключ к значению
* -v - значение
* -in - использовать готовую команду из файла

# Компиляция
mvn clean package

Готовые команды есть в клиентской части в папке data

# Пример использования
```
> java -jar Server/target/JSON-server.jar
Server started!
> java -jar Client/target/JSON-client.jar -in Client/data/setAdvanced.json 
Client started!
Sent: {"type":"set","key":"person","value":{"name":"Elon Musk","car":{"model":"Tesla Roadster","year":"2018"},"rocket":{"name":"Falcon 9","launches":"87"}}}
Received: {
  "response": "OK"
}
```
