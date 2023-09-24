package abstractions;

import data.CommandData;
import data.ResultData;


public interface ICommand{
    ResultData execute(CommandData commandData);
    boolean isClientCommand();
    boolean hasElement();
    default int hasToReadUser(){
        return 0;
    }
    boolean hasIntDigit();
    boolean hasString();
    default boolean isIgnoreAuthorization(){
        return false;
    };
    String getName();
    default String getDescription(){
        return "";
    }
}
