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
                System.out.println("Try to send something");
                CommandData commandData = webDispatcher.sendingDeque.take();
                System.out.println("Get new command data to send");
                webDispatcher.sendCommandToServer(commandData);
            }
            catch (SocketException e){
                System.out.println("Server is unavailable. Repeat your command after reconnection");
                webDispatcher.isConnected = false;
                webDispatcher.connect("127.0.0.1", 8888, client);
                this.run();
            }
            catch (IOException e){
                e.printStackTrace();
                System.out.println("Server is unavailable. Repeat your command after reconnection");
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
