package commands;

import abstractions.ICommand;
import data.CommandData;
import data.ResultData;

import java.io.Serializable;

public class SingUpCommand implements ICommand, Serializable {
    @Override
    public ResultData execute(CommandData commandData) {
        return commandData.labCollection.singUpNewUser(commandData);
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
        return 2;
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
        return "sing_up";
    }

    @Override
    public String getDescription() {
        return "sing up a new user";
    }
}
