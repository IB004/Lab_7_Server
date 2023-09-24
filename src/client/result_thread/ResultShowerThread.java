package client.result_thread;


import client.main_thread.Client;
import data.ResultData;

public class ResultShowerThread extends Thread {
    final Client client;
    final ResultHandler resultHandler;
    public ResultShowerThread(Client client){
        this.client = client;
        this.resultHandler = client.getResultHandler();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ResultData resultData = resultHandler.resultQueue.take();
                setNewUser(resultData);
                resultHandler.showResult(resultData);
            }
        }
        catch (InterruptedException e){
            throw new RuntimeException();
        }

    }

    private void setNewUser(ResultData resultData){
        if(resultData.user != null){
            client.setUser(resultData.user);
        }
    }
}
