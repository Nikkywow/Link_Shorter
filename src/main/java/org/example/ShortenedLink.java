package org.example;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortenedLink {
    private String OriginalURL;
    private String ShortenURL;
    private int VisitLimit;
    private Long ExpirationTime;
    private Long CreationTime;
    private String UserUuid;
   // final Long TIME = System.currentTimeMillis();
   // final Long DAY = (long)24*60*60*100;

    public ShortenedLink(String UserUuid, String OriginalURL, int VisitLimit, int TimeLimit){
        this.OriginalURL = OriginalURL;
        this.ShortenURL = generateShortenedURL();
        this.VisitLimit = VisitLimit;
        this.CreationTime = System.currentTimeMillis();
        this.ExpirationTime = System.currentTimeMillis() + ((long)24*60*60*100)*(long)TimeLimit;
        this.UserUuid = UserUuid;
    }

    public ShortenedLink(String UserUuid, String OriginalURL, String ShortenURL, int VisitLimit, Long CreationTime, Long ExpirationTime){
        this.OriginalURL = OriginalURL;
        this.ShortenURL = ShortenURL;
        this.VisitLimit = VisitLimit;
        this.CreationTime = CreationTime;
        this.ExpirationTime = ExpirationTime;
        this.UserUuid = UserUuid;
    }


    public String getOriginalURL() {
        return OriginalURL;
    }

    public String getShortenURL() {
        return ShortenURL;
    }

    public String getUserUuid() {
        return UserUuid;
    }

    public int getVisitLimit() {
        return VisitLimit;
    }

    public Long getCreationTime() {
        return CreationTime;
    }

    public Long getExpirationTime() {
        return ExpirationTime;
    }

    private String generateShortenedURL(){
        return "clck.ru/" + Integer.toHexString(((int) Math.round(Math.random() * 1000000000))).toUpperCase();
    }

    public List<String> getAttributes(){
        List<String> attributes = Arrays.asList(UserUuid, OriginalURL, ShortenURL, String.valueOf(VisitLimit), String.valueOf(CreationTime), String.valueOf(ExpirationTime));
        //String Uuid, String OriginalURL, String ShortenURL, int VisitLimit, long CreationTime, long ExpirationTime - список для передачи в метод addrow класса table
        return attributes;
    }
}