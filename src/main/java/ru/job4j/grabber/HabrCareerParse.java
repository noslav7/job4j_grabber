package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;
    private final int quantityPages = 5;
    private final int rowsOnPage = 25;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());

        for (int i = 1; i <= habrCareerParse.quantityPages; i++) {
            System.out.println(habrCareerParse.list(
                    "https://career.habr.com/vacancies/java_developer?page=", i)
                    + System.lineSeparator());
        }
    }

    private String retrieveDescription(String link) {
       try {
           return Jsoup.connect(link).get().select(".vacancy-description__text").text();
       } catch (IOException e) {
           throw new IllegalArgumentException("Incorrect link");
       }
    }

    @Override
    public List<Post> list(String link, int pageNumber) {
        List<Post> postsList = new ArrayList<>();
        for (int i = 1; i <= rowsOnPage; i++) {
            try {
                Document document = Jsoup.connect(PAGE_LINK + "?page=" + pageNumber).get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element date = dateElement.child(0);
                    Element linkElement = titleElement.child(0);
                    Element descriptionLink = row.select(".vacancy-card_title-link").first();
                    Element details = descriptionLink.child(0);
                    String description = details.select(".vacancy-description").first();
                    String vacancyName = titleElement.text();
                    String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
                    String vacancyDescription = retrieveDescription("https://career.habr.com/vacancies/"
                            + descriptionElement.text());
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
