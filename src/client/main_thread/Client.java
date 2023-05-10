package client.main_thread;

import abstractions.IClientCommandExecutor;
import abstractions.ICommand;
import client.result_thread.Message;
import client.result_thread.ResultHandler;
import client.result_thread.ResultShowerThread;
import client.result_thread.Warning;
import client.web_thread.WebDispatcher;
import client.web_thread.WebGetThread;
import client.web_thread.WebSendThread;
import data.CommandData;
import data.ResultData;
import data.User;
import exceptions.WrongInputException;
import exceptions.users.NotAuthorizedException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Client read input values, form commandData and send it to the executor.
 * Client also register all commands.
 * Client can execute some commands, like 'help' and 'exit'.
 */
public class Client implements IClientCommandExecutor {
    public Client(){
        messageComponent = new Message();
        warningComponent = new Warning();
        commandDataFormer = new CommandDataFormer();
        inputHandler = new InputHandler(warningComponent);
        resultHandler = new ResultHandler(messageComponent, warningComponent);
        scriptExecutor = new ScriptExecutor();
        webDispatcher = new WebDispatcher(messageComponent, warningComponent);
        new WebGetThread(webDispatcher, resultHandler, this).start();
        new WebSendThread(webDispatcher, this).start();
        new ResultShowerThread(resultHandler).start();
    }
    private boolean scriptReading = false;
    private int nestingLevel = 0;
    private final CommandDataFormer commandDataFormer;
    private final InputHandler inputHandler;
    private final ResultHandler resultHandler;
    private final Message messageComponent;
    private final Warning warningComponent;
    private final ScriptExecutor scriptExecutor;
    private final WebDispatcher webDispatcher;

    public User currentUser;

    public volatile boolean ableToRead;
    public boolean isReadingScript(){
        return scriptReading;
    }

    public boolean userHadLoggedIn(){
        return currentUser != null;
    }
    public int getNestingLevel(){
        return nestingLevel;
    }
    public void setNestingLevel(int num){
        nestingLevel = num;
    }
    public CommandDataFormer getCommandDataFormer() {
        return commandDataFormer;
    }
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    public ResultHandler getResultHandler(){return  resultHandler;}
    public Message getMessageComponent() {
        return messageComponent;
    }
    public Warning getWarningComponent() {
        return warningComponent;
    }
    public ScriptReader getScriptReader() {
        return inputHandler.getScriptReader();
    }
    public WebDispatcher getWebDispatcher() {
        return webDispatcher;
    }


    public void doWhileTrue(){
        try {
            webDispatcher.connect("127.0.0.1", 8888, this);
            while (true) {
                while(ableToRead){
                    CommandData commandData = commandDataFormer.getNewCommandData();
                    commandData.client = this;
                    String[] words = inputHandler.readLine();
                    commandDataFormer.fillCommandData(words, commandData);
                    if (!commandData.isEmpty()) {
                        commandDataFormer.validateCommand(commandData);
                        webDispatcher.sendCommandDataToExecutor(commandData);
                    }
                }
            }
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            this.doWhileTrue();
        }
        catch (NotAuthorizedException e){
            warningComponent.notAuthorizedWarning();
            this.doWhileTrue();
        }
        catch (Exception e){
            e.printStackTrace();
            this.doWhileTrue();
        }
    }


    /*
    public void doWhileTrue(){
        try {
            webDispatcher.connect("127.0.0.1", 8888);
            while (true) {
                showServerRespond();
                CommandData commandData = commandDataFormer.getNewCommandData();
                commandData.client = this;
                String[] words = inputHandler.readLine();
                commandDataFormer.fillCommandData(words, commandData);
                if (!commandData.isEmpty()) {
                    commandDataFormer.validateCommand(commandData);
                    webDispatcher.sendCommandDataToExecutor(commandData);
                }
            }
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            this.doWhileTrue();
        }
        catch (IOException e){
            warningComponent.showWarning(e);
            warningComponent.warningMessage("Server is unavailable. Repeat your command after reconnection");
            webDispatcher.isConnected = false;
            this.doWhileTrue();
        }
        catch (Exception e){
            e.printStackTrace();
            this.doWhileTrue();
        }
    }
     */

    /*
    public void showServerRespond() throws IOException, ClassNotFoundException, InterruptedException {
        Thread.sleep(500);
        ResultData resultData = webDispatcher.getResultDataFromServer();
        resultHandler.addResult(resultData);
        resultHandler.showResults();
    }
     */

    // in sender thread
    public ResultData execute(CommandData commandData){
        ResultData resultData = commandData.command.execute(commandData);
        try {
            if (resultData != null)
                resultHandler.resultDeque.put(resultData);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return resultData;
    }
    public ResultData help(CommandData commandData){
        HashMap<String, ICommand> commandMap = CommandManager.getCommandMap();
        Collection<ICommand> values = commandMap.values();
        for (ICommand command : values){
            messageComponent.showCommandDescription(command);
        }
        messageComponent.printEmptyLine();
        return null;
    }
    public ResultData exit(CommandData commandData){
        try {
            webDispatcher.getSocketChannel().close();
        }
        catch (IOException e){
            warningComponent.showWarning(e);
        }
        System.exit(0);
        return null;
    }
    public ResultData executeScript(CommandData commandData){
        try {
            scriptReading = true;
            scriptExecutor.executeScript(commandData);
            scriptReading = false;
            nestingLevel = 0;
        }
        catch (Exception e){
            warningComponent.showExceptionWarning(e);
        }
        ResultData resultData = new ResultData();
        resultData.resultText = "Script was successfully finished";
        return resultData;
    }
}
