package abstractions;

import data.CommandData;
import data.ResultData;

public interface IServerCommandExecutor extends ICommandExecutor {
    ResultData add(CommandData commandData);
    ResultData clear(CommandData commandData);
    ResultData countByMinimalPoint(CommandData commandData);
    ResultData nameContains(CommandData commandData);
    ResultData info (CommandData commandData);
    ResultData minById (CommandData commandData);
    ResultData readCSVFile (CommandData commandData);
    ResultData removeById (CommandData commandData);
    ResultData removeGreater (CommandData commandData);
    ResultData reorder (CommandData commandData);
    ResultData saveToCSV (CommandData commandData);
    ResultData show (CommandData commandData);
    ResultData shuffle (CommandData commandData);
    ResultData updateById (CommandData commandData);

}
