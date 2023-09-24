package abstractions;

import client.main_thread.CommandDataFormer;
import client.main_thread.InputHandler;
import client.main_thread.ScriptReader;
import client.result_thread.Message;
import client.result_thread.ResultHandler;
import client.result_thread.Warning;
import client.web_thread.WebDispatcher;
import data.CommandData;
import data.ResultData;
import data.User;


public interface IClientCommandExecutor extends ICommandExecutor{
    boolean isReadingScript();
    boolean userHadLoggedIn();
    int getNestingLevel();
    void setNestingLevel(int num);

    CommandDataFormer getCommandDataFormer();
    InputHandler getInputHandler();
    ResultHandler getResultHandler();
    Message getMessageComponent();
    Warning getWarningComponent();
    ScriptReader getScriptReader();
    WebDispatcher getWebDispatcher();
    void setUser(User user);

    ResultData logOut(CommandData commandData);
    ResultData help(CommandData commandData);
    ResultData exit(CommandData commandData);
    ResultData executeScript(CommandData commandData);

    User getCurrentUser();
}
