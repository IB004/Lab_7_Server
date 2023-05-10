package client.result_thread;

import data.LabWork;
import data.ResultData;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

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
    public BlockingDeque<ResultData> resultDeque = new LinkedBlockingDeque<>();

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
