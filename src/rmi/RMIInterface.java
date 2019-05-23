package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    int backup(String filename, int replicationDegree) throws RemoteException;

    int restore(String filename) throws RemoteException;

    int delete(String filename) throws RemoteException;

    String info() throws RemoteException;

    int disconnect() throws RemoteException;

}
