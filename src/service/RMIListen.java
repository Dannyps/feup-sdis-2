package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import chord.ChordKey;
import chord.Node;
import rmi.RMIInterface;
import utils.PrintMessage;

/**
 * RMIListen
 */
public class RMIListen implements RMIInterface {

    private Node node;

    RMIListen(Node n) {
        this.node = n;
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
            PrintMessage.rmi("Couldn't connect to rmiregistry. This peer will run in standalone mode.");
        }
    }

    @Override
    public Boolean backup(String filename, int replicationDegree) throws RemoteException {

        File file = new File(filename);
        byte[] content = null;
        try {
            content = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        node.putObj(new ChordKey(filename), content);
        return true;
    }

    @Override
    public Object restore(String filename) throws RemoteException {
        String res;
        try {
            res = String.valueOf(node.getObj(new ChordKey(filename)));
            System.out.println(res);
            PrintMessage.i("GOT", res);
            return res;
        } catch (Exception e) {
            PrintMessage.e("RESTORE", "A requested file was not found.");
            System.exit(10);
        }
        return null;
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