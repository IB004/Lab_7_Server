package client.main_thread;

import client.result_thread.Warning;
import data.LabWork;
import data.User;
import exceptions.WrongInputException;
import exceptions.users.PasswordsDoNotMatchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * InputElementReader forms a new data.LabWork using input values.
 */
public class InputElementReader {
    public InputElementReader(Scanner scanner, Warning warningComponent){
        this.scanner = scanner;
        this.warningComponent = warningComponent;
    }
    private final Scanner scanner;
    Warning warningComponent;

    public LabWork readElementFromInput() {
        LabWork labWork = new LabWork();
        readName(labWork);
        readMinimalPoint(labWork);
        readMaximumPoint(labWork);
        readPersonalQualitiesMaximum(labWork);
        readCoordinatesX(labWork);
        readCoordinatesY(labWork);
        readDisciplineName(labWork);
        readDisciplineLabsCount(labWork);
        readDisciplineLectureHours(labWork);
        readDifficulty(labWork);
        return labWork;
    }

    public User readUser(){
        User user = new User();
        user.setName(readWord("Type user name"));
        user.setPassword(readPassword("Type the password"));
        return user;
    }

    public User readNewUser() throws PasswordsDoNotMatchException {
        User user = new User();
        user.setName(readWord("Type user name"));
        String password =  readPassword("Type the password");
        String repeatedPassword = readPassword("Repeat password");
        if (!password.equals(repeatedPassword)){
            throw new PasswordsDoNotMatchException();
        }
        return user;
    }
    private String readPassword(String helpMessage){
        if(!isNullOrEmpty(helpMessage)){
            System.out.print(helpMessage + ": ");
        }

        HidingPasswordThread thread = new HidingPasswordThread();
        thread.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String password = "";

        try {
            password = in.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // stop masking
        thread.stopMasking();
        // return the password entered by the user
        return password;
    }
    private void readName(LabWork labWork){
        try {
            labWork.setName(readWord("Type the name"));
        }catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readName(labWork);
        }
    }
    private void readMinimalPoint(LabWork labWork){
        try{
            labWork.setMinimalPoint(readWord("Type the minimal point"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readMinimalPoint(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("long");
            readMinimalPoint(labWork);
        }
    }
    private void readMaximumPoint(LabWork labWork){
        try{
            labWork.setMaximumPoint(readWord("Type the maximum point"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readMaximumPoint(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("double");
            readMaximumPoint(labWork);
        }
    }
    private void readPersonalQualitiesMaximum(LabWork labWork){
        try{
            labWork.setPersonalQualitiesMaximum(readWord("Type the personal qualities maximum"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readPersonalQualitiesMaximum(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("float");
            readPersonalQualitiesMaximum(labWork);
        }
    }
    private void readCoordinatesX(LabWork labWork){
        try{
            labWork.setCoordinatesX(readWord("Type the x coordinate"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readCoordinatesX(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("long");
            readCoordinatesX(labWork);
        }
    }
    private void readCoordinatesY(LabWork labWork){
        try{
            labWork.setCoordinatesY(readWord("Type the y coordinate"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readCoordinatesY(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("long");
            readCoordinatesY(labWork);
        }
    }
    private void readDifficulty(LabWork labWork) {
        try {
            String str = readWord("Type the difficulty level.\n" +
                                             "VERY_HARD, INSANE or TERRIBLE");
            labWork.setDifficulty(str);
        }catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readDifficulty(labWork);
        }
        catch (IllegalArgumentException e){
             warningComponent.wrongEnumValue();
            readDifficulty(labWork);
        }
    }
    private void readDisciplineName(LabWork labWork){
        try {
            labWork.setDisciplineName(readWord("Type the discipline name"));
        }catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readDisciplineName(labWork);
        }
    }
    private void readDisciplineLectureHours(LabWork labWork){
        try{
            labWork.setDisciplineLectureHours(readWord("Type the discipline lecture hours"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readDisciplineLectureHours(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("long");
            readDisciplineLectureHours(labWork);
        }
    }
    private void readDisciplineLabsCount(LabWork labWork){
        try{
            labWork.setDisciplineLabsCount(readWord("Type the discipline labs count"));
        }
        catch (WrongInputException e){
            warningComponent.showExceptionWarning(e);
            readDisciplineLabsCount(labWork);
        }
        catch (NumberFormatException e){
            warningComponent.mustBeType("long");
            readDisciplineLabsCount(labWork);
        }
    }
    private String readWord(String helpMessage){
        if(!isNullOrEmpty(helpMessage)){
            System.out.print(helpMessage + ": ");
        }
        String str = scanner.nextLine();
        String[] words = str.split("\\s+");
        for(String word: words){
            if (isNullOrEmpty(word)){
                continue;
            }
            return word;
        }
        return null;
    }
    private boolean isNullOrEmpty(String str){
        return (str == null || str.isBlank());
    }


}
