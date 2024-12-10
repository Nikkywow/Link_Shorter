package org.example;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Table table = new Table("data.csv");

        if (table.isEmpty()){
            System.out.println("Приветствую! Данная программа предназначена для работы с короткими ссылками.");
            firstContact(table);
        } else {
            notFirstContact(table);
        }
    }

    public static ShortenedLink createShortenedLink(String originalLink){ // метод возвращает экземпляр класса ShortenedLink по данным, которые вводит пользователь
        Scanner scanner = new Scanner(System.in);
        User user = new User();
        user.generateUuid();
        System.out.printf("Вам был присвоен UUID: %s", user.getUuid());
        System.out.println();
        System.out.print("Введите лимит по количеству использований ссылки: ");
        int countLimit = scanner.nextInt();
        System.out.print("Введите лимит по времени(в днях): ");
        int timeLimit = scanner.nextInt();
        ShortenedLink shortenedLink = new ShortenedLink(user.getUuid(), originalLink, countLimit, timeLimit);
        System.out.printf("Ваша короткая ссылка на ресурс: %s", shortenedLink.getShortenURL());
        System.out.println();
        return shortenedLink;
    }

    public static ShortenedLink createShortenedLink(String originalLink, String UserUuid){ // перегрузка прошлого метода, в случае если мы берем ЮЮИД пользователя из бд, а не генерируем новый
        Scanner scanner = new Scanner(System.in);
        User user = new User(UserUuid);
        System.out.print("Введите лимит по количеству использований ссылки: ");
        int countLimit = scanner.nextInt();
        System.out.print("Введите лимит по времени(в днях): ");
        int timeLimit = scanner.nextInt();
        ShortenedLink shortenedLink = new ShortenedLink(user.getUuid(), originalLink, countLimit, timeLimit);
        return shortenedLink;
    }

    public static void firstContact(Table table) { // если для пользователя создается новый ЮЮИД и не нужно ничего проверять вызывается этот метод
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ссылку: ");
        String originalLink = scanner.nextLine();
        ShortenedLink shortenedlink = createShortenedLink(originalLink);
        System.out.print("Нажмите ENTER для переадресации: ");
        //scanner.nextLine();
        String string = scanner.nextLine();
        if(string.trim().isEmpty()){
            LinkDirector(originalLink);
        }
        table.addRow(shortenedlink.getUserUuid(), shortenedlink.getOriginalURL(), shortenedlink.getShortenURL(), shortenedlink.getVisitLimit(), shortenedlink.getCreationTime(), shortenedlink.getExpirationTime());
    }

    public static String timeLongToTimestamp(Long time){ // метод для преобразования времени формата лонг в таймстемп
        Instant instant = Instant.ofEpochMilli(time);
        String formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(instant);
        return formattedDate;
    }

    public static void notFirstContact(Table table) { // если в бд уже есть записи
        Scanner scanner = new Scanner(System.in);
        long lastSession = Long.parseLong(table.getData().get(table.getData().size() - 1).get(4));
        System.out.printf("Хотите продолжить последний сеанс(%s) Да/Нет: ", timeLongToTimestamp(lastSession));
        String decision = scanner.nextLine();

        if (decision.equalsIgnoreCase("да")){ // если пользователь хочет продолжить сессию по ЮЮИД из предыдущей сессии
            String userUuid = table.getData().get(table.getData().size()-1).get(0);
            System.out.print("Введите ссылку(вы можете ввести оригинальную ссылку на ресурс для получения короткой, или короткую для переадресации): ");
            String Link = scanner.nextLine();
            boolean isOriginal;
            if(Link.startsWith("http://") | Link.startsWith("https://")){ // проверяем какого формата ссылку ввел пользователь
                isOriginal = true;
            } else if (Link.startsWith("clck.ru/")){
                isOriginal = false;
            } else {
                System.out.println("Неверный формат ссылки.");
                return;
            }
            if (isOriginal){ // если пользователь ввел длинную ссылку
                if (table.getShortenedLink(userUuid, Link).equals("null")) { // Если такой ссылки привязанной к данному пользователю нет в нашей бд
                    ShortenedLink shortenedlink = createShortenedLink(Link, userUuid);
                    table.addRow(shortenedlink.getUserUuid(), shortenedlink.getOriginalURL(), shortenedlink.getShortenURL(), shortenedlink.getVisitLimit(), shortenedlink.getCreationTime(), shortenedlink.getExpirationTime());
                    LinkDirector(shortenedlink.getOriginalURL());
                } else { // Если такая ссылка уже есть (предполагаем, что пользователь хочет чтобы ему напомнили короткую ссылку, привязанную к той, которую он ввел)
                    String shortenedLink = table.getShortenedLink(userUuid, Link);
                    if(table.countLinkUsage(userUuid, shortenedLink) == table.getShortenedLinkLimit(userUuid, shortenedLink)){ // проверяем, превышен ли лимит по кол-ву посещений
                        System.out.printf("Превышен лимит по количеству использований для ссылки: %s(%s/%s использований)", shortenedLink, table.countLinkUsage(userUuid, shortenedLink), table.getShortenedLinkLimit(userUuid, shortenedLink));
                        System.out.println();
                        createNewLink(userUuid, Link, table);
                    } else if (table.getExpirationTime(userUuid, shortenedLink) < System.currentTimeMillis()){ // проверяем, превышен ли лимит по времени
                        System.out.printf("Превышен лимит по времени для ссылки: %s(действует до %s)", shortenedLink, timeLongToTimestamp(table.getExpirationTime(userUuid, shortenedLink)));
                        System.out.println();
//                        System.out.println(table.getExpirationTime(userUuid, shortenedLink) + "лимит");
//                        System.out.println(System.currentTimeMillis() + "сейчас");
                        createNewLink(userUuid, Link, table);
                    } else { // если лимиты не превышены, то делаем запись в бд и переходим по ссылке
                        ShortenedLink shortenedLink1 = new ShortenedLink(userUuid, Link, shortenedLink, table.getShortenedLinkLimit(userUuid, shortenedLink), table.getCreationTime(userUuid, shortenedLink), table.getExpirationTime(userUuid, shortenedLink));
                        System.out.printf("Ваша короткая ссылка на ресурс: %s", shortenedLink1.getShortenURL());
                        System.out.println();
                        System.out.print("Нажмите ENTER для переадресации: ");
//                        scanner.nextLine();
                        String string = scanner.nextLine();
                        if(string.trim().isEmpty()){
                            LinkDirector(Link);
                        }
                        table.addRow(shortenedLink1.getUserUuid(), shortenedLink1.getOriginalURL(), shortenedLink1.getShortenURL(), shortenedLink1.getVisitLimit(), shortenedLink1.getCreationTime(), shortenedLink1.getExpirationTime());
                    }
                }
            } else if (!isOriginal){ // если пользователь ввел созданную прогой короткую ссылку
                String shortenedLink = Link;
                String originalLink = table.getOriginalLink(userUuid, shortenedLink);
                if(table.countLinkUsage(userUuid, shortenedLink) == table.getShortenedLinkLimit(userUuid, shortenedLink)){ // проверяем, превышен ли лимит по кол-ву посещений
                    System.out.printf("Превышен лимит по количеству использований для ссылки: %s(%s/%s использований)", shortenedLink, table.countLinkUsage(userUuid, shortenedLink), table.getShortenedLinkLimit(userUuid, shortenedLink));
                    System.out.println();
                    createNewLink(userUuid, originalLink, table);
                } else if (table.getExpirationTime(userUuid, shortenedLink) < System.currentTimeMillis()){ // проверяем, превышен ли лимит по времени
                    System.out.printf("Превышен лимит по времени для ссылки: %s(%s)", shortenedLink, timeLongToTimestamp(table.getExpirationTime(userUuid, shortenedLink)));
                    System.out.println();
//                    System.out.println(table.getExpirationTime(userUuid, shortenedLink) + "лимит");
//                    System.out.println(System.currentTimeMillis() + "сейчас");
                    createNewLink(userUuid, originalLink, table);
                } else { // если лимиты не превышены
                    ShortenedLink shortenedLink1 = new ShortenedLink(userUuid, originalLink, shortenedLink, table.getShortenedLinkLimit(userUuid, shortenedLink), table.getCreationTime(userUuid, shortenedLink), table.getExpirationTime(userUuid, shortenedLink));
                    System.out.print("Нажмите ENTER для переадресации: ");
                    //scanner.nextLine();
                    String string = scanner.nextLine();
                    if(string.trim().isEmpty()){
                        LinkDirector(originalLink);
                    }
                    table.addRow(shortenedLink1.getUserUuid(), shortenedLink1.getOriginalURL(), shortenedLink1.getShortenURL(), shortenedLink1.getVisitLimit(), shortenedLink1.getCreationTime(), shortenedLink1.getExpirationTime());
                }
            } else {
                System.out.println("Неверный формат ввода данных.");
                return;
            }
        } else { // если пользователь хочет начать новую сессию
            firstContact(table);
        }
    }
    public static void LinkDirector(String  originalLink) { // метод для перехода по ссылке
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(originalLink)); // Переадресация по указанной ссылке
            System.out.println("Переадресация на URL: " + originalLink);
        } catch (IOException | URISyntaxException e) {
            System.out.println("Ошибка при попытке переадресации: " + e.getMessage());
        }
    }

    public static void createNewLink(String userUuid, String originalLink, Table table){ // метод для создания новой ссылки, если у старой закончился лимит
        Scanner scanner1 = new Scanner(System.in);
        System.out.println("Создаю новую короткую ссылку...");
        System.out.print("Введите лимит по количеству использований ссылки: ");
        int countLimit = scanner1.nextInt();
        System.out.print("Введите лимит по времени использования ссылки(в днях): ");
        int timeLimit = scanner1.nextInt();
        ShortenedLink shortenedLink = new ShortenedLink(userUuid, originalLink, countLimit, timeLimit);
        System.out.printf("Ваша новая ссылка на ресурс: %s", shortenedLink.getShortenURL());
        System.out.println();
        System.out.print("Нажмите ENTER для переадресации: ");
        scanner1.nextLine();
        String string = scanner1.nextLine();
        if(string.trim().isEmpty()){
            LinkDirector(originalLink);
        }
        table.addRow(shortenedLink.getUserUuid(), shortenedLink.getOriginalURL(), shortenedLink.getShortenURL(), shortenedLink.getVisitLimit(), shortenedLink.getCreationTime(), shortenedLink.getExpirationTime());
    }
}
