package client.web_thread;

import client.main_thread.Client;
import data.CommandData;

import java.io.IOException;
import java.net.SocketException;

public class WebSendThread extends Thread{
    private final WebDispatcher webDispatcher;
    private  final Client client;
    public WebSendThread(WebDispatcher webDispatcher, Client client){
        this.webDispatcher = webDispatcher;
        this.client = client;
    }
    @Override
    public void run() {
        while (true){
            try {
                CommandData commandData = webDispatcher.sendingQueue.take();
                webDispatcher.sendCommandToServer(commandData);
            }
            catch (SocketException e){
                client.getWarningComponent().serverIsUnavailable();
                webDispatcher.isConnected = false;
                webDispatcher.connect("127.0.0.1", 8888, client);
                this.run();
            }
            catch (IOException e){
                e.printStackTrace();
                client.getWarningComponent().serverIsUnavailable();
                webDispatcher.isConnected = false;
                webDispatcher.connect("127.0.0.1", 8888, client);
                this.run();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
