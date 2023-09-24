package labCollection;

import data.CommandData;
import data.LabWork;
import data.User;
import exceptions.users.NotRegisteredUserException;
import exceptions.users.WrongPasswordException;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import static data.User.hashPassword;


public class DataBaseHandler {
    Connection connection;
    public DataBaseHandler(){}

    public Connection connectToDB(String rootUserName, String rootPasswd){

        String url = "jdbc:postgresql://127.0.0.1:5432/labs";

        try {
            System.out.println("Try to connect to DB");
            Class.forName("org.postgresql.Driver");
            System.out.println("DB driver has been loaded.");
            connection = DriverManager.getConnection(url, rootUserName, rootPasswd);
            System.out.println("Connect to DB!");
            connection.setAutoCommit(false);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return connection;
    }
    public LinkedList<LabWork> loadCollection() throws SQLException, Exception{
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    SELECT  id,
                            name, user_name,
                            creation_date,
                            min_point, max_point, pers_qual_max, 
                            coord_x, coord_y, 
                            disc_name, disc_lecture_hours, disc_labs_count, 
                            difficulty
                            FROM lab_works;
                    """
        );
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.commit();

        LinkedList<LabWork> works = new LinkedList<>();
        while(resultSet.next()){
            LabWork labWork = new LabWork();
            labWork.setId(resultSet.getInt(1));
            labWork.setName(resultSet.getString(2));
            User user = new User();
            user.setName(resultSet.getString(3));
            labWork.setUser(user);
            labWork.setCreationDate(resultSet.getTimestamp(4).toLocalDateTime().toString());
            labWork.setMinimalPoint(Long.toString(resultSet.getLong(5)));
            labWork.setMaximumPoint(Double.toString(resultSet.getDouble(6)));
            labWork.setPersonalQualitiesMaximum(Float.toString(resultSet.getFloat(7)));
            labWork.setCoordinatesX(Long.toString(resultSet.getLong(8)));
            labWork.setCoordinatesY(Long.toString(resultSet.getLong(9)));
            labWork.setDisciplineName(resultSet.getString(10));
            labWork.setDisciplineLectureHours(Long.toString(resultSet.getLong(11)));
            labWork.setDisciplineLabsCount(Long.toString(resultSet.getLong(12)));
            labWork.setDifficulty(resultSet.getString(13));
            works.add(labWork);
        }
        return works;
    }

    public User registerNewUser(CommandData commandData) throws SQLException {
        User user = commandData.user;
        String salt = UUID.randomUUID().toString();
        byte[] password = hashPassword(user.getPassword(), salt);
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO users (user_name, passwd, salt, is_admin) VALUES (?, ?, ?, ?)"
            );
            preparedStatement.setString(1, user.getName());
            preparedStatement.setBytes(2, password);
            preparedStatement.setString(3, salt);
            preparedStatement.setBoolean(4, user.isAdmin());
            preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }
        return user;
    }
    public User logInUser(CommandData commandData) throws SQLException, WrongPasswordException, NotRegisteredUserException{
        User user = commandData.user;
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT salt, passwd, is_admin FROM users WHERE user_name = ?"
        );
        preparedStatement.setString(1, user.getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.commit();
        if(!resultSet.next()){
            throw new NotRegisteredUserException();
        }
        user.setAdmin(resultSet.getBoolean("is_admin"));
        String salt = resultSet.getString("salt");
        byte[] tablePasswd = resultSet.getBytes("passwd");
        byte[] userPasswd = hashPassword(user.getPassword(), salt);
        if (!Arrays.equals(tablePasswd, userPasswd)){
            throw new WrongPasswordException();
        }
        return user;
    }



    public void addLabWork(CommandData commandData) throws SQLException{
        try {
            LabWork labWork = commandData.element;

            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                INSERT INTO lab_works   (name, user_name, 
                                        min_point, max_point, pers_qual_max, 
                                        coord_x, coord_y, 
                                        disc_name, disc_lecture_hours, disc_labs_count, 
                                        difficulty) VALUES 
                                        (?, ?, 
                                        ?, ?, ?,
                                        ?, ?, 
                                        ?, ?, ?,
                                        ?);
                """
            );
            preparedStatement.setString(1, labWork.getName());
            preparedStatement.setString(2, commandData.user.getName());
            preparedStatement.setLong(3, labWork.getMinimalPoint());
            preparedStatement.setDouble(4, labWork.getMaximumPoint());
            preparedStatement.setFloat(5, labWork.getPersonalQualitiesMaximum());
            preparedStatement.setLong(6, labWork.getXCoord());
            preparedStatement.setLong(7, labWork.getYCoord());
            preparedStatement.setString(8, labWork.getDisciplineName());
            preparedStatement.setLong(9, labWork.getDisciplineLectureHours());
            preparedStatement.setLong(10, labWork.getDisciplineLabsCount());
            preparedStatement.setString(11, labWork.getDifficulty());
            preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }

    }
    public void clearUserElements(CommandData commandData) throws SQLException{
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM lab_works WHERE user_name = ?;"
            );
            preparedStatement.setString(1, commandData.user.getName());
            preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public void truncate(CommandData commandData) throws SQLException{
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "TRUNCATE TABLE lab_works"
            );
            preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }
    public int removeByID(CommandData commandData) throws SQLException{
        int rawDelete = 0;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM lab_works WHERE  id = ?;"
            );
            preparedStatement.setInt(1, commandData.intDigit);
            rawDelete = preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }
        return rawDelete;
    }
    public int updateByID(CommandData commandData) throws SQLException{
        int rawUpdate = 0;
        try {
                LabWork labWork = commandData.element;
                PreparedStatement preparedStatement = connection.prepareStatement(
                        """
                UPDATE lab_works SET    name = ?,
                
                                        min_point = ?,
                                        max_point = ?,
                                        pers_qual_max = ?, 
                                        
                                        coord_x = ?,
                                        coord_y = ?, 
                                        
                                        disc_name = ?,
                                        disc_lecture_hours = ?,
                                        disc_labs_count = ?, 
                                        
                                        difficulty = ?
                WHERE id = ?                      
                ;
                """
            );
            preparedStatement.setString(1, labWork.getName());

            preparedStatement.setLong(2, labWork.getMinimalPoint());
            preparedStatement.setDouble(3, labWork.getMaximumPoint());
            preparedStatement.setFloat(4, labWork.getPersonalQualitiesMaximum());

            preparedStatement.setLong(5, labWork.getXCoord());
            preparedStatement.setLong(6, labWork.getYCoord());

            preparedStatement.setString(7, labWork.getDisciplineName());
            preparedStatement.setLong(8, labWork.getDisciplineLectureHours());
            preparedStatement.setLong(9, labWork.getDisciplineLabsCount());

            preparedStatement.setString(10, labWork.getDifficulty());

            preparedStatement.setInt(11, commandData.intDigit);

            rawUpdate = preparedStatement.executeUpdate();
            connection.commit();

            connection.commit();
        }
        catch (SQLException e){
            connection.rollback();
            throw e;
        }
        return rawUpdate;
    }



    public int getCurrentID() throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT last_value FROM lab_works_id_seq;"
        );
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.commit();
        resultSet.next();
        int curID = resultSet.getInt(1);
        return curID;

    }
    public boolean permissionCheck(User user, int labID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT user_name FROM lab_works WHERE id = ?;"
        );
        preparedStatement.setInt(1, labID);
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.commit();
        if (resultSet.next()){
            String labUser = resultSet.getString(1);
            return labUser.equals(user.getName());
        }
        return false;
    }
}
