package server;

import data.CommandData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.RecursiveAction;

public class ServeConnections extends RecursiveAction {

    public ServeConnections(ArrayList<SelectionKey> selectionKeys, Server server){
        this.selectionKeys = selectionKeys;
        this.server = server;
    }

    private ArrayList<SelectionKey> selectionKeys;
    private final Server server;
    private final int ABLE_TO_SERVE_WITH_ONE_THREAD = 3;
    @Override
    protected void compute() {
        int size = selectionKeys.size();
        if (size <= ABLE_TO_SERVE_WITH_ONE_THREAD){
            try {
                //System.out.printf("Task %s execute in thread %s%n", this, Thread.currentThread().getName());
                serveConnections(selectionKeys);
            }
            catch (Exception e){
                throw new RuntimeException();
            }

        }
        else{
            int mid = size/2;
            ArrayList<SelectionKey> list1 = new ArrayList<>(selectionKeys.subList(0, mid));
            ArrayList<SelectionKey> list2 = new ArrayList<>(selectionKeys.subList(mid, size));
            ServeConnections first = new ServeConnections(list1, server);
            ServeConnections second = new ServeConnections(list2, server);
            invokeAll(first, second);
        }
    }

    private void serveConnections(ArrayList<SelectionKey> selectionKeys){
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            Attachment attachment = (Attachment) key.attachment();
            try {
                if (key.isAcceptable()) {
                    server.accept();
                }
                if (key.isReadable()) {
                    System.out.println("Read from channel");
                    server.read(key);
                    server.fillAttachment(key);
                    if (attachment.isFull()){
                        CommandData commandData = server.transformToCommandData(attachment.getUsefulBytes());
                        attachment.byteArrayOutputStream = new ByteArrayOutputStream();
                        attachment.writeBytes = 0;
                        attachment.objectLength = 0;
                        server.logger.info("Read command: " + commandData.command.getName());
                        System.out.println("Read command: " + commandData.command.getName());
                        commandData.selectionKey = key;
                        server.labCollection.toDoQueue.put(commandData);
                    }
                }
                if (key.isWritable()){
                    server.write(key);
                }
                iterator.remove();
            }
            catch (IOException e){
                //e.printStackTrace();
                server.warningComponent.warningMessage("IOException. Connection reset");
                key.cancel();
            }
            catch (Exception e){
                e.printStackTrace();
                server.warningComponent.warningMessage("Something went wrong. Connection reset");
                key.cancel();
            }
        }

    }
}