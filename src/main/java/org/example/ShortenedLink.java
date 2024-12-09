package org.example;

public class ShortenedLink {
    private String OriginalURL;
    private String ShortenURL;
    private int VisitLimit;
    private Long ExpirationTime;
    private Long CreationTime;
    private String UserUuid;

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
        Table table = new Table("data.csv");
        String generatedLink;
        do {

            generatedLink = "clck.ru/" + Integer.toHexString(((int) Math.round(Math.random() * 1000000000))).toUpperCase();
        } while (table.getAllShortenedLinks().contains(generatedLink));
        return generatedLink;
    }
}