package server;

import client.main_thread.Client;

public class InnerClientThread extends Thread{
    @Override
    public void run() {
        Client client = new Client();
        client.doWhileTrue();
    }
}
