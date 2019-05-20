# Distributed Backup Service for the Internet

## Compiling

In order to compile the project, simply run the shell script `compile.sh`.

```bash
./compile.sh
```

## Launching peers

For the initing the peer, responsible for launching the chord protocol, simply run:

```bash
./run.sh <port>
```

For other peers that join the chord network, run:

```bash
./run.sh <port> <address>
```