package server;

import commands.ReadCommand;
import data.CommandData;
import client.result_thread.Warning;
import labCollection.LabCollection;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    Logger logger = Logger.getLogger(Server.class.getName());
    public Server(LabCollection labCollection) throws Exception{
        FileHandler fileHandler = new FileHandler();
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
        this.warningComponent = new Warning();
        this.labCollection = labCollection;
        startChannelsActions();
        //loadCollectionFromFile();
    }
    final LabCollection labCollection;
    final Warning warningComponent;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(2);


    private int PORT = 8888;
    private SocketAddress socketAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;


    public void doWhileTrue() {
        try {
            while(true){
                serveConnections();
            }
        }
        catch (Exception e){
            logger.log(Level.WARNING,"Something went wrong" , e);
            warningComponent.warningMessage(e.getMessage());
            e.printStackTrace();
        }
    }



    public void serveConnections() throws Exception{
        int keyCount = selector.select(6000);
        if (keyCount == 0) {
            System.out.println("Wait for connection...");
            return;
        }

        ArrayList<SelectionKey> keysList =  new ArrayList<>(selector.selectedKeys().stream().toList());

        forkJoinPool.invoke(new ServeConnections(keysList, this));

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while(iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }

    }

    private void startChannelsActions() throws Exception{
        /*
        Scanner scanner = new Scanner(System.in);
        System.out.print("Type port: ");
        int port = scanner.nextInt();
        PORT = port;
        System.out.println("Port: " + PORT);
         */

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        socketAddress = new InetSocketAddress(PORT);
        serverSocketChannel.socket().bind(socketAddress);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Local address: " + InetAddress.getLocalHost());
    }
    private void loadCollectionFromFile() {
        CommandData commandData = new CommandData();
        commandData.command = new ReadCommand();
        labCollection.execute(commandData);
        logger.info("Load collection from file");
        System.out.println("Load collection from file");

    }

    public void accept() throws Exception{
        SocketChannel socketChannel;
        synchronized (serverSocketChannel) {
            socketChannel = serverSocketChannel.accept();
        }
        if (socketChannel != null){
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Attachment());
            logger.log(Level.INFO, "New client connected: " + socketChannel.getLocalAddress().toString());
            System.out.println("New client connected: " + socketChannel.getLocalAddress().toString());
        }
    }
    public int read(SelectionKey key) throws Exception{
        Attachment attachment = (Attachment) key.attachment();
        ByteBuffer buffer = attachment.readingBuffer;
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int r = socketChannel.read(buffer);
        logger.info("Read " + r + "bytes.");
        logger.info("Buffer state: " + Arrays.toString(buffer.array()));
        if (r == -1) {
            socketChannel.close();
            logger.log(Level.WARNING, "Connection was ended.");
            warningComponent.warningMessage("Connection was ended.");
            return -1;
        }
        return r;
    }
    public void fillAttachment(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        ByteBuffer buffer = attachment.readingBuffer;

        buffer.flip();

        //if this record is first
        if (attachment.writeBytes == 0){
            if (buffer.remaining() > 4){
                attachment.objectLength = buffer.getInt();
                int canRead = Math.min(buffer.remaining(), (attachment.objectLength - attachment.writeBytes));
                byte[] newBytes = new byte[canRead];
                buffer.get(newBytes);
                attachment.byteArrayOutputStream.write(newBytes);
                attachment.writeBytes += canRead;
            }
            else{
                warningComponent.warningMessage("Not enough bytes for object");
                key.cancel();
            }
        }

        //if this record is second+
        else{
            System.out.println("Record is second +");
            int canRead = Math.min(buffer.remaining(), (attachment.objectLength - attachment.writeBytes));
            byte[] newBytes = new byte[canRead];
            buffer.get(newBytes);
            attachment.byteArrayOutputStream.write(newBytes);
            attachment.writeBytes += canRead;
        }

        if (attachment.writeBytes < attachment.objectLength){
            System.out.println("Still have to read something");
        }
        else{
            System.out.println("Attachment is full");
        }

        //compact instead of clear ?
        buffer.flip();
        buffer.clear();
    }
    public CommandData transformToCommandData(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        System.out.println("Bytes in object: " + bytes.length);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        CommandData commandData = (CommandData) objectInputStream.readObject();
        return commandData;
    }
    public void write(SelectionKey key) throws Exception{
        Attachment attachment = (Attachment) key.attachment();
        if (attachment.resultData == null){
            return;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(attachment.resultData);
        ByteBuffer buffer = ByteBuffer.allocate(4 + byteArrayOutputStream.size());
        buffer.putInt(0, byteArrayOutputStream.size());
        buffer.put(4, byteArrayOutputStream.toByteArray());
        buffer.position(0);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(buffer);
        logger.info("Send " + (byteArrayOutputStream.size() + 4) + " bytes to client");
        logger.info("Send: " + Arrays.toString(buffer.array()));
        System.out.println("Send " + (byteArrayOutputStream.size() + 4) + " bytes to client");
        key.attach(new Attachment());
    }

}


