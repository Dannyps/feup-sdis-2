package service;

import java.io.File;
import java.io.FileOutputStream;
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
        ChordKey key = new ChordKey(filename);
        boolean success = node.putObj(key, content);
        if (success) {
            this.node.addFileNameKeyPair(filename, key);
        }
        return true;
    }

    @Override
    public Object restore(String filename) throws RemoteException {
        byte[] res;
        try {
            res = (byte[]) node.getObj(node.getfNameKeys().get(filename));
            FileOutputStream fos = new FileOutputStream(new File(node.getRestoreFolder().getAbsolutePath() + "/" + filename));
            fos.write(res);
            fos.close();

            System.out.println(res);
            // PrintMessage.i("GOT", res);
            return res;
        } catch (Exception e) {
            PrintMessage.e("RESTORE", "A requested file was not found.");
            System.exit(10);
        }
        return null;
    }

    @Override
    public Boolean delete(String filename) throws RemoteException {
        ChordKey key = new ChordKey(filename);
        boolean success = node.delObj(key);
        if (success) {
            this.node.delFileNameKeyPair(filename, key);
        }
        return true;
    }

    @Override
    public String info() throws RemoteException {
        // get State
        var wrapper = new Object() {
            public String res ="";
        };

        wrapper.res += "Backed up files:" + System.lineSeparator();
        node.getfNameKeys().forEach((String s, ChordKey k) -> {
            wrapper.res += s + System.lineSeparator();
        });

        return wrapper.res;
    }

    @Override
    public int disconnect() throws RemoteException {
        return 0;
    }

}