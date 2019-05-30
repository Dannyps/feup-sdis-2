package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    Boolean backup(String filename, int replicationDegree) throws RemoteException;

    Object restore(String filename) throws RemoteException;

    Boolean delete(String filename) throws RemoteException;

    String info() throws RemoteException;

    int disconnect() throws RemoteException;

}
