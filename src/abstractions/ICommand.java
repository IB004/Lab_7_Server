package abstractions;

import data.CommandData;
import data.ResultData;


public interface ICommand{
    ResultData execute(CommandData commandData);
    boolean isClientCommand();
    boolean hasElement();
    default boolean hasToReadUser(){
        return false;
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
