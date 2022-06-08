# JSON-Database
Program uses java sockets to get a jsons from clients. Jsons are stored in files on server field. Program is multithreaded and uses libraries such as google.gson API to serialize jsons and jCommander framework on client field to parse commands.
Данные хранятся в файле на стороне сервера, сервер мультипоточный, для параллелизации используется Executor.
В клиентской части используется JCommander для парсинга аргументов командной строки.

# Аргументы для клиентской части

* -t - тип команды, exit для отключения сервера, set для добавления новой записи или изменения существующей, get для получения значения, delete для удаления ключа и значения
* -k - ключ к значению
* -v - значение
* -in - использовать готовую комманду из файла

# Компиляция
mvn clean package

# Запуск
```
java -jar target/JSON-client.jar ...(аргументы как показано выше)
java -jar target/JSON-server.jar
```

Готовые команды есть в клиентской части в папке data

# Пример использования
```
> java -jar target/JSON-client.jar -in data/get.json 
Client started!
Sent: {"type":"get","key":"person"}
Received:
{
   "response":"OK",
   "value":{
      "name":"Elon Musk",
      "car":{
         "model":"Tesla Roadster",
         "year":"2018"
      },
      "rocket":{
         "name":"Falcon 9",
         "launches":"87"
      }
   }
}
```
