
import labCollection.LabCollection;
import server.ExecutorThread;
import server.InnerClientThread;
import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        LabCollection labCollection = new LabCollection("root", "1");
        Server server = new Server(labCollection);
        new InnerClientThread().start();
        new ExecutorThread(labCollection).start();
        server.doWhileTrue();
    }

}


