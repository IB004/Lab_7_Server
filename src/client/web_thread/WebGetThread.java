package client.web_thread;

import client.main_thread.Client;
import client.result_thread.ResultHandler;
import data.ResultData;

import java.io.IOException;
import java.net.SocketException;

public class WebGetThread extends Thread{
    private final WebDispatcher webDispatcher;
    private final ResultHandler resultHandler;
    private final Client client;

    public WebGetThread(WebDispatcher webDispatcher, ResultHandler resultHandler, Client client){
        this.webDispatcher = webDispatcher;
        this.resultHandler = resultHandler;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1500);
                while(webDispatcher.isConnected){
                    ResultData resultData = webDispatcher.getResultDataFromServer();
                    if (resultData != null)
                        resultHandler.resultQueue.put(resultData);
                }
            }
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
        catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
