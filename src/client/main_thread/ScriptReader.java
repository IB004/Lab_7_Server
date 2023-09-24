package client.main_thread;

import data.LabWork;
import data.User;
import exceptions.EmptyFieldException;
import exceptions.WrongInputException;

import java.util.Scanner;

/**
 * ScriptElementReader forms a new data.LabWork using script values.
 */
public class ScriptReader {
    public ScriptReader(){}
    public LabWork readElementFromScript(Scanner fileScanner) throws WrongInputException {
        LabWork labWork = new LabWork();
        labWork.setName(readWord(fileScanner));
        labWork.setMinimalPoint(readWord(fileScanner));
        labWork.setMaximumPoint(readWord(fileScanner));
        labWork.setPersonalQualitiesMaximum(readWord(fileScanner));
        labWork.setCoordinatesX(readWord(fileScanner));
        labWork.setCoordinatesY(readWord(fileScanner));
        labWork.setDisciplineName(readWord(fileScanner));
        labWork.setDisciplineLabsCount(readWord(fileScanner));
        labWork.setDisciplineLectureHours(readWord(fileScanner));
        labWork.setDifficulty(readWord(fileScanner));
        return labWork;
    }
    public String nextScriptLine(Scanner fileScanner){
        fileScanner.useDelimiter(System.lineSeparator());
        if (fileScanner.hasNext()){
            return fileScanner.nextLine();
        }
        else{
            return null;
        }
    }
    private String readWord(Scanner fileScanner){
        String line = nextScriptLine(fileScanner);
        if (line == null){
            return null;
        }
        String[] words = line.split("\\s+");
        for(String word: words){
            if (word.isBlank()){
                continue;
            }
            return word;
        }
        return null;
    }


    public User readUserFromScript(Scanner scriptScanner) throws EmptyFieldException {
        User user = new User();
        user.setName(readWord(scriptScanner));
        user.setPassword(readWord(scriptScanner));
        return user;
    }
}
