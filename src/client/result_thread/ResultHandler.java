package client.result_thread;

import data.LabWork;
import data.ResultData;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ResultHandler shows the result of the command.
 */
public class ResultHandler {
    private final Message messageComponent;
    private final Warning warningComponent;
    public ResultHandler(Message messageComponent, Warning warningComponent){
        this.messageComponent = messageComponent;
        this.warningComponent = warningComponent;
    }

    private LinkedList<ResultData> results = new LinkedList<>();
    public BlockingQueue<ResultData> resultQueue = new LinkedBlockingQueue<>();

    public void addResult(ResultData resultData){
        results.addLast(resultData);
    }

    public boolean showResult(ResultData resultData){
        if (ResultData.isEmpty(resultData)){
            messageComponent.printNothing();
            return false;
        }
        if (resultData.hasElements()){
            for (LabWork labWork : resultData.labsList){
                messageComponent.printElement(labWork);
            }
        }
        if(resultData.hasText()) {
            messageComponent.printText(resultData.resultText);
        }
        if(resultData.hasErrorMessage()){
            warningComponent.warningMessage(resultData.errorMessage);
        }
        return true;
    }
}
