package client.main_thread;

import abstractions.IClientCommandExecutor;
import data.CommandData;
import exceptions.NestingLevelException;
import exceptions.WrongInputException;
import exceptions.users.NotAuthorizedException;
import exceptions.users.PasswordsDoNotMatchException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

public class ScriptExecutor {
    public ScriptExecutor(){}
    public void executeScript(CommandData commandData) throws NestingLevelException, IOException, WrongInputException, ClassNotFoundException, InterruptedException, NotAuthorizedException, PasswordsDoNotMatchException {

    /*
                while (true) {
                while(ableToRead){
                    CommandData commandData = commandDataFormer.getNewCommandData();
                    commandData.client = this;
                    commandData.user = currentUser;
                    String[] words = inputHandler.readLine();
                    commandDataFormer.fillCommandData(words, commandData);
                    if (!commandData.isEmpty()) {
                        commandDataFormer.validateCommand(commandData);
                        if (commandData.command.hasElement()) {
                            commandData.element.setUser(currentUser);
                        }
                        webDispatcher.sendCommandDataToExecutor(commandData);
                    }
                    //showCurrentThreads();
                }
            }
     */

        IClientCommandExecutor client = commandData.client;
        client.setNestingLevel(client.getNestingLevel() + 1);
        LinkedList<CommandData> commandsList = new LinkedList<>();
            if (client.getNestingLevel() > 5){
                throw new NestingLevelException();
            }
            String filePath = commandData.string;
            Scanner scanner = new Scanner(Paths.get(filePath));
            String nextLine = client.getScriptReader().nextScriptLine(scanner);
            while (nextLine != null){
                CommandData newCommandData = client.getCommandDataFormer().getNewCommandData();
                newCommandData.client = client;
                newCommandData.user = client.getCurrentUser();
                newCommandData.scriptScanner = scanner;

                String[] words = nextLine.split("\\s+");
                client.getCommandDataFormer().fillCommandData(words, newCommandData);
                if (!newCommandData.isEmpty()) {
                    client.getCommandDataFormer().validateCommand(newCommandData);
                    System.out.println("Read command " + newCommandData.command.getName());
                }

                commandsList.addLast(newCommandData);

                nextLine = client.getScriptReader().nextScriptLine(scanner);
            }
            while (commandsList.iterator().hasNext()){
                Thread.sleep(1000);
                CommandData currentCommand = commandsList.removeFirst();
                client.getWebDispatcher().sendCommandDataToExecutor(currentCommand);
                //commandData.client.showServerRespond();
            }
            client.getMessageComponent().printEmptyLine();
            client.setNestingLevel(client.getNestingLevel() - 1);;
    }

}