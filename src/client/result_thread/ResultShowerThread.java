package client.result_thread;


public class ResultShowerThread extends Thread {
    final ResultHandler resultHandler;
    public ResultShowerThread(ResultHandler resultHandler){
        this.resultHandler = resultHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                resultHandler.showResult(resultHandler.resultDeque.take());
            }
        }
        catch (InterruptedException e){
            throw new RuntimeException();
        }

    }
}
