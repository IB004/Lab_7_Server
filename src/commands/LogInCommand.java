package commands;

import abstractions.ICommand;
import data.CommandData;
import data.ResultData;

import java.io.Serializable;

public class LogInCommand implements ICommand, Serializable {
    @Override
    public ResultData execute(CommandData commandData) {
        return commandData.labCollection.logInUser(commandData);
    }

    @Override
    public boolean isClientCommand() {
        return false;
    }

    @Override
    public boolean hasElement() {
        return false;
    }

    @Override
    public int hasToReadUser() {
        return 1;
    }

    @Override
    public boolean hasIntDigit() {
        return false;
    }

    @Override
    public boolean hasString() {
        return false;
    }

    @Override
    public boolean isIgnoreAuthorization() {
        return true;
    }

    @Override
    public String getName() {
        return "log_in";
    }

    @Override
    public String getDescription() {
        return "log in the user";
    }
}
