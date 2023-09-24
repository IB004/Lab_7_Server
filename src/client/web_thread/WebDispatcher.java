package client.web_thread;

import client.main_thread.Client;
import client.result_thread.Message;
import client.result_thread.Warning;
import data.CommandData;
import data.ResultData;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebDispatcher {
    private volatile SocketChannel socketChannel;
    private ByteBuffer readBuf = ByteBuffer.allocate(8192);
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private int writeBytes = 0;
    private int objectLength = 0;

    public volatile BlockingQueue<CommandData> sendingQueue = new LinkedBlockingQueue<>();
    public volatile boolean isConnected = false;
    private final Message messageComponent;

    private final Warning warningComponent;
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    public WebDispatcher(Message messageComponent, Warning warningComponent){
        this.messageComponent = messageComponent;
        this.warningComponent = warningComponent;
    }


    //main thread, get thread, send thread
    //only returns able to read to true if reused without setting isConnected false
    public synchronized SocketChannel connect(String host, int port, Client client){
        InetSocketAddress address = new InetSocketAddress(host, port);
        client.ableToRead = false;
        //try to connect
        while (!isConnected) {
            try {
                socketChannel = SocketChannel.open(address);
                isConnected = socketChannel.isConnected();
                messageComponent.connectionSuccess();
                socketChannel.configureBlocking(false);
            }
            catch (IOException e) {
                System.out.println("Try to connect ... ");
                try{
                    Thread.sleep(3000);
                }catch (InterruptedException ie){
                    throw new RuntimeException();
                }
            }
        }

        client.ableToRead = true;
        return socketChannel;
    }

    //main thread
    public void sendCommandDataToExecutor(CommandData commandData) throws InterruptedException {
        if (commandData == null || commandData.command == null){
            return;
        }
        if (commandData.command.isClientCommand()){
            sendCommandToClient(commandData);
        }
        else{
            sendingQueue.put(commandData);
        }
    }

    //main thread
    private void sendCommandToClient(CommandData commandData){
        commandData.client.execute(commandData);
    }



    //send thread
    public void sendCommandToServer(CommandData commandData) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(commandData);

        //add 4 to write length
        ByteBuffer buffer = ByteBuffer.allocate(4 + byteArrayOutputStream.size());
        buffer.putInt(0, byteArrayOutputStream.size());
        buffer.put(4, byteArrayOutputStream.toByteArray());
        buffer.position(0);
        synchronized (socketChannel){
            socketChannel.write(buffer);
        }
        System.out.println("Send " + buffer.capacity() + " bytes to server");

    }



    //get thread
    public ResultData getResultDataFromServer() throws IOException, ClassNotFoundException {

        int r = socketChannel.read(readBuf);
        if (r == -1) {
            socketChannel.close();
            warningComponent.warningMessage("Connection was ended.");
            return null;
        }
        if(r == 0){
            return null;
        }
        System.out.println("Get " + r + " bytes from server");

        readBuf.flip();
        //if this record is first
        if (writeBytes == 0){
            if (readBuf.remaining() > 4){
                objectLength = readBuf.getInt();
                int canRead = Math.min(readBuf.remaining(), (objectLength - writeBytes));
                byte[] newBytes = new byte[canRead];
                readBuf.get(newBytes);
                byteArrayOutputStream.write(newBytes);
                writeBytes += canRead;
            }
            else{
                warningComponent.warningMessage("Not enough bytes for object");
                return null;
            }
        }
        //if this record is second+
        else{
            int canRead = Math.min(readBuf.remaining(), (objectLength - writeBytes));
            byte[] newBytes = new byte[canRead];
            readBuf.get(newBytes);
            byteArrayOutputStream.write(newBytes);
            writeBytes += canRead;
        }
        //if object was not ended
        if (writeBytes < objectLength){
            messageComponent.printText("Still have to read something");
        }

        // compact instead of clear?
        readBuf.flip();
        readBuf.clear();

        ResultData resultData = transformToResultData();
        if (resultData != null){
            readBuf = ByteBuffer.allocate(8192);
            byteArrayOutputStream = new ByteArrayOutputStream();
            writeBytes = 0;
            objectLength = 0;
        }
        return resultData;
    }

    //get thread
    private ResultData transformToResultData() throws IOException, ClassNotFoundException {
        byte[] usefulBytes = byteArrayOutputStream.toByteArray();
        if (usefulBytes.length == objectLength){
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(usefulBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            ResultData resultData = (ResultData) objectInputStream.readObject();
            return resultData;
        }
        return null;
    }
}
