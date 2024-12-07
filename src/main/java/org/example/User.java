package org.example;

import java.util.List;
import java.util.UUID;

public class User {
    private String Uuid;

    public User(){

    }

    public User(String UserUuid){
        this.Uuid = UserUuid;
    }

    public void generateUuid(){
        UUID Uuid = UUID.randomUUID();
        this.Uuid = Uuid.toString();
    }

    public String getUuid() {
        return Uuid;
    }
}