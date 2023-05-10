package labCollection;

import data.LabWork;
import exceptions.WrongInputException;

import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * CSVHandler read and parse CSV files.
 */
public class CSVHandler {
    static String regExp = "[\"']?\\s*,\\s*[\"']?|\\A[\"']|[\"']";
    public static LinkedList<LabWork> readCSV(String filePath) throws   IOException, WrongInputException, NumberFormatException{
        LinkedList<LabWork> labList = new LinkedList<>();
        Scanner scanner = new Scanner(Paths.get(filePath));
        scanner.useDelimiter(System.lineSeparator());
        boolean haveToCheckFirst = true;
        while(scanner.hasNext()){
            boolean haveToRead = true;
            String nextLine = scanner.nextLine();
            while (nextLine == null || nextLine.isBlank()){
                if (scanner.hasNext()) {
                    nextLine = scanner.nextLine();
                }
                else{
                    haveToRead = false;
                    break;
                }
            }
            if (haveToRead) {
                if(haveToCheckFirst){
                    String[] words = nextLine.split(regExp);
                    int wordsCount = 0;
                    for(String word : words){
                        try{
                            Double.valueOf(word);
                        }
                        catch(NumberFormatException e){
                            wordsCount++;
                        }
                    }
                    if ((wordsCount+0.1)/words.length < 0.35){
                        labList.add(parseCSVLine(nextLine));
                    }
                    haveToCheckFirst = false;
                }
                else{
                    labList.add(parseCSVLine(nextLine));
                }
            }
        }
        scanner.close();
        return labList;
    }
    public static LabWork parseCSVLine(String line) throws WrongInputException, NumberFormatException, NoSuchElementException {
        LabWork labWork = new LabWork();
        String[] words = line.split(regExp);
        if (words.length < 12){
            throw new NoSuchElementException();
        }
        labWork.setId(Integer.parseInt(words[0]));
        labWork.setCreationDate(words[1]);
        labWork.setName(words[2]);
        labWork.setMinimalPoint(words[3]);
        labWork.setMaximumPoint(words[4]);
        labWork.setPersonalQualitiesMaximum(words[5]);
        labWork.setCoordinatesX(words[6]);
        labWork.setCoordinatesY(words[7]);
        labWork.setDisciplineName(words[8]);
        labWork.setDisciplineLabsCount(words[9]);
        labWork.setDisciplineLectureHours(words[10]);
        labWork.setDifficulty(words[11]);
        return labWork;
    }
    public static void writeCollectionToCSV(String filePath, LabCollection labCollection) throws IOException {
        OutputStream os = new FileOutputStream(filePath);
        Writer osr = new OutputStreamWriter(os);
        for (LabWork lab : labCollection.getCollection()){
            osr.write(labToCSV(lab));
        }
        osr.close();
    }
    private static String labToCSV(LabWork labWork){
        String delimiter = ", ";
        String lineDelimiter = System.getProperty("line.separator");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i< labWork.toStringArray().length; i++){
            String word = labWork.toStringArray()[i];
            if(word.equals("null") || word.isBlank()){
                word = "";
            }
            str.append(word);
            if (i != labWork.toStringArray().length-1){
                str.append(delimiter);
            }
        }
        str.append(lineDelimiter);
        return str.toString();
    }
}
