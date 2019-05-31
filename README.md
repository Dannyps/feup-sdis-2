# Distributed Backup Service for the Internet

## Compiling

In order to compile the project, simply run the shell script `compile.sh` at the root of the project directory.

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

## Interface for the protocols (TestApp)

The implemented services/protocols are:
- Backup
- Restore
- Delete
- Status

### Backup

In order to perform a file backup, launch the `TestApp` with the following arguments:

```bash
./run_app.sh backup <rmi id> <filename> <replication degree>
```

| Arguments | Description |
| --------- | ----------- |
| `RMI_ID` | The peer to initiate the service, in this case, the peer who originally requested a file to be backed up |
| `filename` | The original filename |
| `replication_degree` | The replication degree desired, i.e., the minimum number of peers who should store backup the file |

### Restore

If a given peer has requested to backup a file, it can restore it. The interface for this service is:

```bash
./run_app.sh restore <RMI_ID> <filename>
```

| Arguments | Description |
| --------- | ----------- |
| `RMI_ID` | The peer to initiate the service, in this case, the peer who originally requested a file to be backed up |
| `filename` | The original filename |

### Delete

A previously backed up file can be removed from the Chord Network using the delete service.

```bash
./run_app.sh delete <RMI_ID> <filename>
```

| Arguments | Description |
| --------- | ----------- |
| `RMI_ID` | The peer to initiate the service, in this case, the peer who originally requested a file to be backed up. It will request all peers who backed up the file to delete the backup |
| `filename` | The original filename |

### State

In order to view the state of the peer, such as which files were backed up, run the state service.

```bash
./run_app.sh state <RMI_ID>
```