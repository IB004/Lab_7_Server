package labCollection;


import abstractions.IServerCommandExecutor;
import data.*;
import exceptions.IdIsNotUniqueException;
import exceptions.WrongInputException;
import server.Attachment;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * LabCollection stores and manages LabWorks.
 */
public class LabCollection implements IServerCommandExecutor {
    private  LinkedList<LabWork> labsList = new LinkedList<>();
    private  String filePath;
    private LocalDateTime creationDate;
    private int collectionIDPointer = 1;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LabCollection(){
        this.creationDate = LocalDateTime.now();
    }

    public LinkedList<LabWork> getCollection(){
        return labsList;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public BlockingDeque<CommandData> toDoDeque = new LinkedBlockingDeque<>();



    public ResultData execute(CommandData commandData){
        commandData.labCollection = this;
        if (commandData.selectionKey  == null){
            return commandData.command.execute(commandData);
        }
        Attachment attachment = (Attachment) commandData.selectionKey.attachment();
        ResultData resultData = commandData.command.execute(commandData);
        attachment.resultData = resultData;
        return resultData;
    }
    public ResultData show(CommandData commandData){
        ResultData resultData = new ResultData();
        if (labsList.isEmpty()){
            resultData.resultText = "Collection is empty";
            return resultData;
        }
        resultData.labsList = labsList;
        return resultData;
    }
    public ResultData clear(CommandData commandData){
        ResultData resultData = new ResultData();
        labsList = new LinkedList<>();
        resultData.resultText = "Collection is cleared";
        return resultData;
    }
    public ResultData info(CommandData commandData){
        ResultData resultData = new ResultData();
        resultData.resultText = ServerTextFormer.collectionInfo(this);
        return resultData;
    }

    public ResultData add(CommandData commandData){
        LabWork labWork = commandData.element;
        labWork.setId(collectionIDPointer);
        collectionIDPointer++;
        labsList.add(labWork);

        ResultData resultData = new ResultData();
        resultData.labsList.add(labWork);
        resultData.resultText = "Element was successfully added";
        return resultData;
    }
    public ResultData shuffle(CommandData commandData){
        ResultData resultData = new ResultData();
        Collections.shuffle(labsList);
        resultData.resultText = "Shake elements hard";
        return resultData;
    }
    public ResultData reorder(CommandData commandData){
        ResultData resultData = new ResultData();
        Collections.reverse(labsList);
        resultData.resultText = "Collection was reordered";
        return resultData;
    }


    //Stream API
    public ResultData nameContains(CommandData commandData){
        ResultData resultData = new ResultData();
        resultData.labsList = labsList.stream().filter((el) -> el.getName().contains(commandData.string)).collect(Collectors.toCollection(LinkedList::new));

        if (resultData.labsList.isEmpty()){
            resultData.resultText = "There are no elements with such substring in the name";
        }
        return resultData;
    }
    public ResultData countByMinimalPoint(CommandData commandData){
        ResultData resultData = new ResultData();
        long r = labsList.stream().filter((el) -> el.getMinimalPoint().intValue() == commandData.intDigit).count();
        resultData.resultText = "There are " + (r == 0 ? "no" : r) + " elements with such minimal point";
        return resultData;
    }
    public ResultData removeById(CommandData commandData){
        ResultData resultData = new ResultData();
        Integer id = commandData.intDigit;
        boolean haveSuchElement = labsList.stream().anyMatch((el) -> el.getId().equals(id));
        List<LabWork> deleteList = labsList.stream().filter((el) -> el.getId().equals(id)).toList();
        labsList.removeAll(deleteList);
        if (haveSuchElement){
            resultData.resultText = "Element was deleted";
        }
        else{
            resultData.resultText = "There is no element with such id";
        }
        return resultData;
    }
    public ResultData updateById(CommandData commandData){
        ResultData resultData = new ResultData();
        Integer id = commandData.intDigit;
        boolean haveSuchElement = labsList.stream().anyMatch((el) -> el.getId().equals(id));
        labsList.stream().filter((el) -> el.getId().equals(id)).forEach((el) -> el.updateInfoFromElement(commandData.element));

        if (haveSuchElement){
            resultData.resultText = "Element was updated";
        }
        else{
            resultData.resultText = "There is no element with such id";
        }
        return resultData;
    }
    public ResultData removeGreater(CommandData commandData){
        ResultData resultData = new ResultData();
        Comparator<LabWork> pointsComparator = new PointsPerDifficultyComparator();
        long i = labsList.stream().filter((el) -> pointsComparator.compare(el, commandData.element) > 0).count();
        List<LabWork> deleteList = labsList.stream().filter((el) -> pointsComparator.compare(el, commandData.element) > 0).toList();
        labsList.removeAll(deleteList);
        resultData.resultText = "Removed " + i + " elements";
        return resultData;
    }
    public ResultData minById (CommandData commandData){
        ResultData resultData = new ResultData();
        if(labsList.isEmpty()){
            resultData.resultText = "Collection is empty";
            return resultData;
        }
        LabWork el = labsList.stream().min(new IdComparator()).orElse(null);
        resultData.labsList.add(el);
        return resultData;
    }


    public ResultData saveToCSV(CommandData commandData){
        if(filePath == null || filePath.isBlank()){
            return null;
        }
        ResultData resultData = new ResultData();
        try{
            CSVHandler.writeCollectionToCSV(filePath, this);
            resultData.resultText = "Collection is saved";
        }
        catch (IOException e){
            resultData.errorMessage = e.getMessage();
        }
        return resultData;
    }
    public ResultData readCSVFile(CommandData commandData){
        if(filePath == null || filePath.isBlank()){
            return null;
        }
        ResultData resultData = new ResultData();
        try{
            LinkedList<LabWork> labWorks = CSVHandler.readCSV(filePath);
            checkIdUnique(labWorks);
            setIdPointerToMaxId(labWorks);
            labsList.addAll(labWorks);
        }
        catch (WrongInputException e){
            resultData.resultText = e.toString();
        }
        catch (NumberFormatException e){
            String str = "CSV number format exception:\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        catch (NoSuchElementException e){
            String str = "CSV has not enough data\n" + e.getMessage() + e.getLocalizedMessage();
            resultData.errorMessage = str;
        }
        catch(DateTimeParseException e){
            String str = "CSV date format exception:\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        catch (IdIsNotUniqueException e){
            String str = "CSV contains not unique id";
            resultData.errorMessage = str;
        }
        catch (InvalidPathException e){
            String str = "CSV input file path is not correct\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        catch (IllegalArgumentException e){
            String str = "No such enum difficulty value\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        catch (FileSystemNotFoundException e){
            String str = "CSV file not found exception\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        catch (SecurityException e){
            String str = "CSV access denied. File security exception";
            resultData.errorMessage = str;
        }
        catch (IOException e) {
            String str = "CSV some IO exception\n" + e.getMessage();
            resultData.errorMessage = str;
        }
        return resultData;
    }
    private void checkIdUnique(LinkedList<LabWork> list) throws IdIsNotUniqueException {
        boolean idIsUnique = true;
        for (int i = 0; i < list.size()-1; i++){
            if(!(idIsUnique)){
                break;
            }
            for (int j = i+1; j < list.size(); j++){
                if (list.get(i).getId().equals(list.get(j).getId())) {
                    idIsUnique = false;
                    break;
                }
            }
        }
        if (!idIsUnique) {
            throw new IdIsNotUniqueException();
        }
    }
    public void setIdPointerToMaxId(LinkedList<LabWork> list){
        int maxId = 0;
        for (LabWork lab : list){
            maxId = Math.max(maxId, lab.getId());
        }
        collectionIDPointer = maxId + 1;
    }


}
