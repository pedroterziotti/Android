package br.delt.acess;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;

public class User
{

    public static ArrayList<User> userArrayList =new ArrayList<>();
    public static String USER_EDIT_EXTRA =  "noteEdit";
    public static String USER_RFID_EXTRA =  "rfidExtra";

    private int id;
    private String rfidKey;
    private String cargo;
    private String pictureURI;
    private Date lastAcess;
    private String nome;

    public User(int id, String rfidKey, String cargo, String picture, Date lastAcess,String nome) {
        this.id = id;
        this.rfidKey = rfidKey;
        this.cargo = cargo;
        this.pictureURI = picture;
        this.lastAcess = lastAcess;
        this.nome=nome;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRfidKey() {
        return rfidKey;
    }

    public void setRfidKey(String rfidKey) {
        this.rfidKey = rfidKey;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getPictureURI() {
        return pictureURI;
    }

    public void setPictureURI(String  pictureURI) {
        this.pictureURI = pictureURI;
    }

    public Date getLastAcess() {
        return lastAcess;
    }

    public void setLastAcess(Date lastAcess) {
        this.lastAcess = lastAcess;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public static User getUserForID(int passedNoteID)
    {
        for (User note : userArrayList)
        {
            if(note.getId() == passedNoteID)
                return note;
        }
        return null;
    }



}
