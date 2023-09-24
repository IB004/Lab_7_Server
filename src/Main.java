
import labCollection.LabCollection;
import server.ExecutorThread;
import server.InnerClientThread;
import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        LabCollection labCollection = new LabCollection(args[0], args[1]);
        Server server = new Server(labCollection);
        new InnerClientThread().start();
        new ExecutorThread(labCollection).start();
        server.doWhileTrue();
    }

}


