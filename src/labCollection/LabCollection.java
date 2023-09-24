package labCollection;


import abstractions.IServerCommandExecutor;
import data.*;
import exceptions.IdIsNotUniqueException;
import exceptions.WrongInputException;
import exceptions.users.NotRegisteredUserException;
import exceptions.users.WrongPasswordException;
import server.Attachment;

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * LabCollection stores and manages LabWorks.
 */
public class LabCollection implements IServerCommandExecutor {
    private DataBaseHandler dataBaseHandler = new DataBaseHandler();
    private  LinkedList<LabWork> labsList = new LinkedList<>();
    private  String filePath;
    private LocalDateTime creationDate;
    private int collectionIDPointer = 1;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LabCollection(String rootName, String rootPasswd){
        this.creationDate = LocalDateTime.now();
        dataBaseHandler.connectToDB(rootName, rootPasswd);
        loadCollectionFromDB();
    }

    public LinkedList<LabWork> getCollection(){
        return labsList;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public BlockingQueue<CommandData> toDoQueue = new LinkedBlockingQueue<>();



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

        try {
            dataBaseHandler.clearUserElements(commandData);
            if (commandData.user.isAdmin()){
                dataBaseHandler.truncate(commandData);
            }
        }
        catch (SQLException e){
            resultData.errorMessage = e.getMessage();
            return resultData;
        }

        List<LabWork> deleteList = labsList.stream().filter((el) -> (permissionCollectionCheck(commandData.user, el))).toList();
        labsList.removeAll(deleteList);
        resultData.resultText = "Remove " + deleteList.size() + " elements";

        return resultData;
    }
    public ResultData info(CommandData commandData){
        ResultData resultData = new ResultData();
        resultData.resultText = ServerTextFormer.collectionInfo(this);
        return resultData;
    }

    public ResultData add(CommandData commandData){
        ResultData resultData = new ResultData();
        try{
            dataBaseHandler.addLabWork(commandData);
        }
        catch (SQLException e){
            resultData.errorMessage = e.getMessage();
            return resultData;
        }
        LabWork labWork = commandData.element;
        labWork.setId(getCurrentID());
        collectionIDPointer++;
        labsList.add(labWork);


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
        int id = commandData.intDigit;

        int rawDelete = 0;
        try{
            if (permissionDBCheck(commandData.user, commandData.intDigit))
                rawDelete =  dataBaseHandler.removeByID(commandData);
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        List<LabWork> deleteList = labsList.stream().filter((el) -> el.getId().equals(id)).filter((el) -> (permissionCollectionCheck(commandData.user, el))).toList();
        labsList.removeAll(deleteList);
        resultData.resultText = "Delete " + rawDelete + " elements of user " + commandData.user.getName();
        return resultData;
    }
    public ResultData updateById(CommandData commandData){
        ResultData resultData = new ResultData();
        Integer id = commandData.intDigit;

        int rawUpdate = 0;
        try{
            if (permissionDBCheck(commandData.user, commandData.intDigit))
                rawUpdate =  dataBaseHandler.updateByID(commandData);
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        //boolean haveSuchElement = labsList.stream().anyMatch((el) -> el.getId().equals(id));
        labsList.stream().filter((el) -> el.getId().equals(id)).filter((el) -> (permissionCollectionCheck(commandData.user, el))).forEach((el) -> el.updateInfoFromElement(commandData.element));

        resultData.resultText = "Update " + rawUpdate + " elements";

        return resultData;
    }
    public ResultData removeGreater(CommandData commandData){
        ResultData resultData = new ResultData();
        Comparator<LabWork> pointsComparator = new PointsPerDifficultyComparator();
        long i = labsList.stream().filter((el) -> pointsComparator.compare(el, commandData.element) > 0).filter((el) -> (permissionCollectionCheck(commandData.user, el))).count();

        List<LabWork> deleteList = labsList.stream().filter((el) -> pointsComparator.compare(el, commandData.element) > 0).filter((el) -> (permissionCollectionCheck(commandData.user, el))).toList();
        for (LabWork lab : deleteList){
            commandData.element = lab;
            commandData.intDigit = lab.getId();
            try {
                dataBaseHandler.removeByID(commandData);
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }

        labsList.removeAll(deleteList);
        resultData.resultText = "Remove " + i + " elements";
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


    public ResultData singUpNewUser(CommandData commandData) {
        ResultData resultData = new ResultData();
        try{
            dataBaseHandler.registerNewUser(commandData);
            resultData.user = commandData.user;
            resultData.resultText = "Register a new user " + commandData.user.getName();
        }
        catch (SQLException e){
            resultData.errorMessage = e.getMessage();
            return resultData;
        }
        catch (Exception  e){
            e.printStackTrace();
            throw new RuntimeException();
        }
        return resultData;
    }
    public ResultData logInUser(CommandData commandData){
        ResultData resultData = new ResultData();
        try{
            User setUser = dataBaseHandler.logInUser(commandData);
            resultData.user = setUser;
            resultData.resultText = "Welcome, " + commandData.user.getName();
        }
        catch (SQLException e){
            resultData.errorMessage = e.getMessage();
            return resultData;
        }
        catch (WrongPasswordException e){
            resultData.errorMessage = "Passwords is wrong!";
            return resultData;
        }
        catch (NotRegisteredUserException e){
            resultData.errorMessage = "User with this name was not registered!";
            return resultData;
        }
        catch (Exception  e){
            e.printStackTrace();
            throw new RuntimeException();
        }
        return resultData;
    }
    private void loadCollectionFromDB(){
        try{
            labsList = dataBaseHandler.loadCollection();
            System.out.println("Load collection from DB!");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getCurrentID(){
        int curID = -1;
        try{
            curID = dataBaseHandler.getCurrentID();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            return curID;
        }
    }
    private boolean permissionDBCheck(User user, int labID){
        boolean permitted = false;
        if (user.isAdmin()){
            System.out.println("Allow all operations with DB. User is admin!");
            return true;
        }
        try{
            permitted = dataBaseHandler.permissionCheck(user, labID);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return permitted;
    }

    private boolean permissionCollectionCheck(User user, LabWork labWork){
        boolean permitted = false;
        if (user.isAdmin()){
            System.out.println("Allow all operations with Collection. User is admin!");
            return true;
        }
        permitted = labWork.getUser().getName().equals(user.getName());
        return permitted;
    }
}
