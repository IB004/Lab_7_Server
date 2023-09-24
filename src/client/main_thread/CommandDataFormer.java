package client.main_thread;

import abstractions.ICommand;
import data.CommandData;
import data.User;
import exceptions.DigitRequiredException;
import exceptions.StringRequiredException;
import exceptions.WrongInputException;
import exceptions.users.NotAuthorizedException;
import exceptions.users.PasswordsDoNotMatchException;

public class CommandDataFormer {
    public CommandDataFormer(){}
    public CommandData getNewCommandData(){
        return new CommandData();
    }
    public void validateCommand(CommandData commandData) throws WrongInputException, NumberFormatException, NotAuthorizedException, PasswordsDoNotMatchException {
        ICommand command = commandData.command;
        if (command.hasIntDigit() && commandData.intDigit == null) {
            throw new DigitRequiredException(command.getName());
        }
        if (command.hasString() && commandData.string == null) {
            throw new StringRequiredException(command.getName());
        }
        if (!command.isIgnoreAuthorization() && !commandData.client.userHadLoggedIn()){
           throw new NotAuthorizedException();
        }
        if (command.hasToReadUser() == 1){
            if (commandData.client.isReadingScript()) {
                commandData.user = commandData.client.getInputHandler().readScriptUser(commandData.scriptScanner);
            }
            else {
                User user = commandData.client.getInputHandler().readUser();
                commandData.user = user;
            }
        }
        if (command.hasToReadUser() == 2){
            if (commandData.client.isReadingScript()) {
                commandData.user = commandData.client.getInputHandler().readScriptUser(commandData.scriptScanner);
            }
            else {
                User user = commandData.client.getInputHandler().readNewUser();
                commandData.user = user;
            }
        }
        if (command.hasElement()) {
            commandData.user = commandData.client.getCurrentUser();
            if (commandData.client.isReadingScript()) {
                commandData.element = commandData.client.getInputHandler().readScriptElement(commandData.scriptScanner);
                System.out.println("read element: \n");
                commandData.client.getMessageComponent().printElement(commandData.element);
            } else {
                commandData.element = commandData.client.getInputHandler().readInputElement();
            }
        }
    }
    public void fillCommandData(String[] words, CommandData commandData) throws WrongInputException, NumberFormatException {
        int rememberI = 0;
        for (int i = 0; i<words.length; i++) {
            String word = words[i];
            if (word.isBlank()) {
                continue;
            }
            if (commandData.commandName == null) {
                commandData.commandName = word;
                rememberI = i + 1;
                break;
            }
        }
        if (commandData.commandName == null || commandData.commandName.isBlank()){
            return;
        }
        commandData.command = CommandManager.getCommand(commandData.commandName);
        for (int i = rememberI; i<words.length; i++){
            String word = words[i];
            if (word.isBlank()) {
                continue;
            }
            if(commandData.command.hasIntDigit()) {
                try {
                    commandData.intDigit = Integer.valueOf(word);
                } catch (NumberFormatException e) {
                    commandData.intDigit = null;
                }
            }
            if (commandData.command.hasString()) {
                commandData.string = word;
                continue;
            }
            break;
        }

    }
}
