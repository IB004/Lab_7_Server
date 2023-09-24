package server;

import client.main_thread.Client;
import com.sun.source.tree.YieldTree;
import data.User;


public class InnerClientThread extends Thread{
    @Override
    public void run() {
        Client client = new Client();
        try {
            User admin = new User();
            admin.setAdmin(true);
            admin.setName("admin");
            admin.setPassword("admin");
        client.setUser(admin);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        client.doWhileTrue();
    }
}
