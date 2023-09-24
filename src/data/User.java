package data;

import exceptions.EmptyFieldException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PEPPER = "%a@*g^h7er8){s[:t#u;";
    private String password;

    private String name;
    private boolean isAdmin;


    public void setName(String name) throws EmptyFieldException{
        if(name == null || name.isBlank())
            throw new EmptyFieldException();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPassword(String password){
        if(password == null){
            password = "";
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public static byte[] hashPassword(String password, String salt){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest((PEPPER + password + salt).getBytes("UTF-8"));
            System.out.println("Set password: " + new String(hash, StandardCharsets.UTF_8));
            return hash;
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
