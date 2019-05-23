package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import chord.Node;
import rmi.RMIInterface;
import utils.PrintMessage;

/**
 * RMIListen
 */
public class RMIListen implements RMIInterface {

    private Node node;

    RMIListen(Node n) {
        this.node=n;
        bindToRMI();
    }

    private void bindToRMI() {
        try {
            Registry registry;
            registry = LocateRegistry.getRegistry();
            Remote stub = UnicastRemoteObject.exportObject(this, 0);
            // Bind the remote object's stub in the registry
            registry.rebind(Integer.toString(node.getKey().getSucc()), stub);
            PrintMessage.rmi("Peer ready on " + Integer.toString(node.getKey().getSucc()));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int backup(String filename, int replicationDegree) throws RemoteException {
        return 0;
    }

    @Override
    public int restore(String filename) throws RemoteException {
        return 0;
    }

    @Override
    public int delete(String filename) throws RemoteException {
        return 0;
    }

    @Override
    public String info() throws RemoteException {
        return null;
    }

    @Override
    public int disconnect() throws RemoteException {
        return 0;
    }

    
}