para jugar zatacka Distribuido:
SERVIDOR:
Primero compilar todos los archivos:
$javac cl/uchile/dcc/cc5303/*.java
en una terminal ejecutar el servidor
$java cl/uchile/dcc/cc5303/Server
para jugar de más que 2 jugadores usa -n
$java cl/uchile/dcc/cc5303/Server -n 5  (así inicia con 5 jugadores)

CLIENTE
Primero compilar todos los archivos si es una máquina diferente:
$javac cl/uchile/dcc/cc5303/*.java
en una terminal ejecutar el cliente
$java cl/uchile/dcc/cc5303/Main "rmi://xxx.xxx.xxx.xxx:1099/zatackaServer"
xxx.xxx.xxx.xxx es la ip del servidor(puedes ver la url cuandoe jecutas el servidor)

para jugar: moverse con las flechas de arriba y abajo.
Cuando todos los jugadores terminan aparece un mensaje para reintentarlo
apretar Y para reintentar o N para abandonar el juego.
El juego se reiniciará con todos los jugadores que digan Y. pero no iniciará hasta que todos respondan



