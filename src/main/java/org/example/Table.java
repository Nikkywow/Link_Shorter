package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Table {
    private List<List<String>> data = new ArrayList<>();
    private String filePath;

    public Table(String filePath){
        this.filePath = filePath;
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> row = Arrays.asList(line.split(","));
                data.add(row);
            }
        } catch (IOException e) {
            System.out.println("Ошибка: Файл отсутствует: " + e.getMessage());
        }
    }

    public void addRow(String Uuid, String OriginalURL, String ShortenURL, int VisitLimit, long CreationTime, long ExpirationTime) {
        try {
            List<String> row = Arrays.asList(Uuid, OriginalURL, ShortenURL, String.valueOf(VisitLimit), String.valueOf(CreationTime), String.valueOf(ExpirationTime));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true));
            bufferedWriter.write(String.join(",", row));
            bufferedWriter.newLine();
            bufferedWriter.close();

            data.add(row);
        } catch (IOException e){
            System.out.println("Ошибка: Данные не были записаны");
        }
    }

    public List<List<String>> getUserData(String Uuid){
        List<List<String>> userdata = new ArrayList<>();
        for(List<String> userDataLine: data) {
            if (userDataLine.get(0).equals(Uuid)) {
                userdata.add(userDataLine);
            }
        }
        return userdata;
    }

    public int countLinkUsage(String Uuid, String ShortenURL){
        int linkUsageCounter = 0;
        for (List<String> userDataLine : getUserData(Uuid)){
            if (userDataLine.get(2).equals(ShortenURL)){
                linkUsageCounter++;
            }
        }
    return linkUsageCounter;
    }

    public List<List<String>> getData(){
        return data;
    }

    public boolean isEmpty(){
        return getData().size() == 0;
    }

    public String getShortenedLink(String userUuid, String originalLink){
        String shortenedLink = "null";
        for(List<String> dataLine: data){
            if(dataLine.get(1).equals(originalLink) & dataLine.get(0).equals(userUuid)){
                shortenedLink = dataLine.get(2);
            }
        }
    return shortenedLink;
    }

    public String getOriginalLink(String userUuid, String shortenedLink){
        String originalLink = "null";
        for(List<String> dataLine: data){
            if(dataLine.get(2).equals(shortenedLink) && dataLine.get(0).equals(userUuid)){
                originalLink = dataLine.get(1);
            }
        }
        return originalLink;
    }

    public int getShortenedLinkLimit(String userUuid, String shortenedLink){
        for(List<String> dataLine: data){
            if(dataLine.get(2).equals(shortenedLink) && dataLine.get(0).equals(userUuid)){
                int shortenedLinkLimit = Integer.valueOf(dataLine.get(3));
                return shortenedLinkLimit;
            }
        }
        return 333;
    }

    public Long getCreationTime(String userUuid, String shortenedLink){
        for(List<String> dataLine: data){
            if(dataLine.get(2).equals(shortenedLink) && dataLine.get(0).equals(userUuid)){
                Long creationTime = Long.valueOf(dataLine.get(4));
                return creationTime;
            }
        }
        return 0L;
    }

    public Long getExpirationTime(String userUuid, String shortenedLink){
        for(List<String> dataLine: data){
            if(dataLine.get(2).equals(shortenedLink) && dataLine.get(0).equals(userUuid)){
                Long expiration = Long.valueOf(dataLine.get(5));
                return expiration;
            }
        }
        return 0L;
    }

    public List<String> getAllShortenedLinks(){
        List<String> shortenedListSet = new ArrayList<>();
        for (List<String> dataLine: data){
            shortenedListSet.add(dataLine.get(2));
        }
        return shortenedListSet;
    }
}
