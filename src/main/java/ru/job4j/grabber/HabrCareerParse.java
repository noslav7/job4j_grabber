package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String
            .format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;
    private final int quantityPages = 5;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        habrCareerParse.list(PAGE_LINK, habrCareerParse.quantityPages).stream()
                .map(e -> e.toString().getBytes(Charset.forName("Windows-1251")))
                .forEach(System.out::println);
    }

    private String retrieveDescription(String link) {
       try {
           return Jsoup.connect(link).get().select(".vacancy-description__text").text();
       } catch (IOException e) {
           throw new IllegalArgumentException("Incorrect link");
       }
    }

    @Override
    public List<Post> list(String link, int pages) {
        List<Post> postsList = new ArrayList<>();
        for (int i = 1; i <= pages; i++) {
            try {
                Document document = Jsoup.connect(PAGE_LINK + pages).get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element date = dateElement.child(0);
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String vacancyDescription = retrieveDescription(vacancyLink);
                    String stringDate = date.attr("datetime");
                    postsList.add(new Post(vacancyName, vacancyLink,
                            vacancyDescription, dateTimeParser.parse(stringDate)));
                });
            } catch (IOException ioException) {
                throw new IllegalArgumentException();
            }
        }
        return postsList;
    }
}
