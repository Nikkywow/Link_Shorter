package org.example;

import org.w3c.dom.ls.LSOutput;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLOutput;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {


//я закончил на моменте когда юзер вводит длинную ссылку, и в бд нет инфы что он когда-то вводил такую же
//нужно закончить хуйню сверху, написать алгоритм для обрабокт ситуаций когда длинная ссылка уже есть в бд
        // написать алгоритм когда лимит на длинной ссылке в бд закончился и по колво и по времени,
        //написать алгоритм для обработки коротких ссылок
        //Важно!!! разобраться как записывать во время создания обекта класса shortenedlink не текущее время а время из первой записи по ссылке(скорее всего пегрузить конструктор)
// суббота: сделал функцию переадресации, перегрузил конструктор шортенд линка чтобы привводе ориг ссылки в логе правильно писалось время создания
//написал алгоритм когда длииная ссылка уже есть в бд
//по алгаритму когда закончился лимит: надо написать метод мейна для пересоздания короткой ссылки
//
        final Long DAY_IN_LONG = (long)24*60*60*100;
        Scanner scanner = new Scanner(System.in);
        Table table = new Table("data.csv");

        if (table.isEmpty()){
            System.out.println("Эта программа предназначна для укорачивания ссылок...");

            firstContact(table);

        } else {
            notFirstContact(table);
        }
    }
    public static ShortenedLink createShortenedLink(String originalLink){
        Scanner scanner = new Scanner(System.in);
        User user = new User();
        user.generateUuid();
        System.out.println(user.getUuid());


        System.out.print("Введите лимит по использованию: ");
        int countLimit = scanner.nextInt();
        System.out.print("Введите лимит по времени: ");
        int timeLimit = scanner.nextInt();
        ShortenedLink shortenedLink = new ShortenedLink(user.getUuid(), originalLink, countLimit, timeLimit);
        System.out.println(shortenedLink.getShortenURL());
        System.out.println(shortenedLink.getUserUuid());
        return shortenedLink;
    }

    public static ShortenedLink createShortenedLink(String originalLink, String UserUuid){
        Scanner scanner = new Scanner(System.in);
        User user = new User(UserUuid);
        System.out.println(user.getUuid());


        System.out.print("Введите лимит по использованию: ");
        int countLimit = scanner.nextInt();
        System.out.print("Введите лимит по времени: ");
        int timeLimit = scanner.nextInt();
        ShortenedLink shortenedLink = new ShortenedLink(user.getUuid(), originalLink, countLimit, timeLimit);
        System.out.println(shortenedLink.getShortenURL());
        System.out.println(shortenedLink.getUserUuid());
        return shortenedLink;
    }

    public static void firstContact(Table table) throws URISyntaxException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите ссылку: ");
        String originalLink = scanner.nextLine();

        ShortenedLink shortenedlink = createShortenedLink(originalLink);

        table.addRow(shortenedlink.getUserUuid(), shortenedlink.getOriginalURL(), shortenedlink.getShortenURL(), shortenedlink.getVisitLimit(), shortenedlink.getCreationTime(), shortenedlink.getExpirationTime());
        LinkRedirektor(shortenedlink.getOriginalURL());
        return;

    }
    public static void notFirstContact(Table table) throws URISyntaxException, IOException {
        Scanner scanner = new Scanner(System.in);
        Long lastSession = Long.valueOf(table.getData().get(table.getData().size()-1).get(4));
        Instant instant = Instant.ofEpochMilli(lastSession);
        String formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(instant);

        System.out.println("Хотите продолжить последний сеанс завершившийся " + formattedDate);
        System.out.print("Да/Нет ");
        String decision = scanner.nextLine();

        if (decision.equals("Да")){

            String userUuid = table.getData().get(table.getData().size()-1).get(0);

            System.out.print("Введите ссылку: ");

            String Link = scanner.nextLine();

            boolean isOriginal;

            if(Link.startsWith("http://") | Link.startsWith("https://")){
                isOriginal = true;
            } else if (Link.startsWith("clck.ru/")){
                isOriginal = false;
            } else {
                System.out.println("Неверный формат ссылки.");
                return;
            }
            if (isOriginal){
                if (table.getShortenedLink(userUuid, Link).equals("null")) {
                    System.out.println("ссылки нет в бд");
                    ShortenedLink shortenedlink = createShortenedLink(Link, userUuid);
                    table.addRow(shortenedlink.getUserUuid(), shortenedlink.getOriginalURL(), shortenedlink.getShortenURL(), shortenedlink.getVisitLimit(), shortenedlink.getCreationTime(), shortenedlink.getExpirationTime());
                    LinkRedirektor(shortenedlink.getOriginalURL());
                    return;

                } else {
                    System.out.println("ссылка уже есть в бд");
                    String shortenedLink = table.getShortenedLink(userUuid, Link);
                    if(table.countLinkUsage(userUuid, shortenedLink) == table.getShortenedLinkLimit(userUuid, shortenedLink)){
                        System.out.println("лимит по количеству");
                        System.out.println(table.countLinkUsage(userUuid, shortenedLink) + "использований");
                        System.out.println(table.getShortenedLinkLimit(userUuid, shortenedLink) + "лимит");
                    } else if (table.getExpirationTime(userUuid, shortenedLink) < System.currentTimeMillis()){
                        System.out.println("лимит по времени");
                        System.out.println(table.getExpirationTime(userUuid, shortenedLink) + "лимит");
                        System.out.println(System.currentTimeMillis() + "сейчас");
                    } else {
                        ShortenedLink shortenedLink1 = new ShortenedLink(userUuid, Link, shortenedLink, table.getShortenedLinkLimit(userUuid, shortenedLink), table.getCreationTime(userUuid, shortenedLink), table.getExpirationTime(userUuid, shortenedLink));
                        table.addRow(shortenedLink1.getUserUuid(), shortenedLink1.getOriginalURL(), shortenedLink1.getShortenURL(), shortenedLink1.getVisitLimit(), shortenedLink1.getCreationTime(), shortenedLink1.getExpirationTime());
                        LinkRedirektor(Link);
                    }


                }

            } else if (!isOriginal) {
                //для коротких ссылок написать алгоритм как для оригенальных
            }


        }



    }
    public static void LinkRedirektor(String  originalLink) throws URISyntaxException, IOException {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(originalLink)); // Переадресация по указанной ссылке
            System.out.println("Переадресация на URL: " + originalLink);
        } catch (IOException | URISyntaxException e) {
            System.out.println("Ошибка при попытке переадресации: " + e.getMessage());
        }
    }
}