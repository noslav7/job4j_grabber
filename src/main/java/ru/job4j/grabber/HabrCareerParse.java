package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringJoiner;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Document document = Jsoup.connect(PAGE_LINK + "?page=" + i).get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element titleElement = row.select(".vacancy-card__title").first();
                Element date = dateElement.child(0);
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String vacancyDate = dateElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String stringDate = String.format("%s ", date.attr("datetime"));
                System.out.printf("%s %s %s %n", vacancyName, link, stringDate);
            });
        }
    }

    private String retrieveDescription(String link) {
       try {
           return Jsoup.connect(link).get().select(".vacancy-description__text").text();
       } catch (IOException e) {
           throw new IllegalArgumentException("Incorrect link");
       }
    }
}
