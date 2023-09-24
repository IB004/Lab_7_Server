package commands;

import abstractions.ICommand;
import data.CommandData;
import data.ResultData;

import java.io.Serializable;

public class LogOutCommand implements ICommand, Serializable {
    @Override
    public ResultData execute(CommandData commandData) {
        return commandData.client.logOut(null);
    }

    @Override
    public boolean isClientCommand() {
        return true;
    }

    @Override
    public boolean hasElement() {
        return false;
    }

    @Override
    public int hasToReadUser() {
        return 0;
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
        return false;
    }

    @Override
    public String getName() {
        return "log_out";
    }

    @Override
    public String getDescription() {
        return "allow you to change current account";
    }
}
