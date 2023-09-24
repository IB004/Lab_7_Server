package server;

import labCollection.LabCollection;

public class ExecutorThread extends Thread{
    final LabCollection labCollection;

    public ExecutorThread(LabCollection labCollection){
        this.labCollection = labCollection;
    }
    @Override
    public void run() {
        try {
            while (true) {
                labCollection.execute(labCollection.toDoQueue.take());
            }
        }
        catch (InterruptedException e){
            throw new RuntimeException();
        }
    }
}
