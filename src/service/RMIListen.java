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
import utils.OurFile;
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
        byte[] content = new byte[0];

        try {
            content = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        OurFile ourFile = new OurFile(filename, replicationDegree);

        for(int i = 0 ; i < replicationDegree ; i++) {
            String str = filename + "_" + i;
            ChordKey key = new ChordKey(str);

            boolean success = node.putObj(key, content);
            if (success) {
                ourFile.getFileDegreeKey().put(i, key);
            } else {
                i--;
            }
        }
        this.node.addFileNameKeyPair(filename, ourFile);
        return true;
    }

    @Override
    public Object restore(String filename) throws RemoteException {
        byte[] res;
        try {
            for(int i = 0 ; i < this.node.getfNameKeys().get(filename).getTotalReplicationDegree() ; i++) {
                ChordKey k = this.node.getfNameKeys().get(filename).getFileDegreeKey().get(i);
                res = (byte[]) node.getObj(k);
                if(res != null) {
                    FileOutputStream fos = new FileOutputStream(new File(this.node.getRestoreFolder().getAbsolutePath() + "/" + filename));
                    fos.write(res);
                    fos.close();
                    PrintMessage.i("GOT", filename);
                    return res;
                }
            }
        } catch (Exception e) {
            PrintMessage.e("RESTORE", "A requested file was not found.");
            System.exit(10);
        }
        return null;
    }

    @Override
    public Boolean delete(String filename) throws RemoteException {
        for(int i = 0 ; i < this.node.getfNameKeys().get(filename).getTotalReplicationDegree() ; i++) {
            ChordKey key = this.node.getfNameKeys().get(filename).getFileDegreeKey().get(i);
            boolean success = node.delObj(key);
            if (success) {
                this.node.getfNameKeys().get(filename).getFileDegreeKey().remove(i);
            }
        }
        if(this.node.getfNameKeys().get(filename).getFileDegreeKey().size() == 0) {
            this.node.delFileNameKeyPair(filename);
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
        node.getfNameKeys().forEach((String s, OurFile k) -> {
            wrapper.res += s + System.lineSeparator();
        });

        return wrapper.res;
    }

    @Override
    public int disconnect() throws RemoteException {
        return 0;
    }

}