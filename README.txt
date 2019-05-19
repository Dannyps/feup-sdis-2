Para correr o primeiro peer:

java main.Main <server_port>

Para correr os peers seguintes:

java main.Main <server_port> <ip_peer1> <port_peer1>


p.ex: primeiro peer -> java 8080
      segundo peer  -> java 8081 localhost 8080