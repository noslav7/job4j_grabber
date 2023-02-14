package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
       try {
           return Jsoup.connect(link).get().select(".vacancy-description__text").text();
       } catch (IOException e) {
           throw new IllegalArgumentException("Incorrect link");
       }
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> postsList = new ArrayList<>();
        AtomicInteger id = new AtomicInteger();
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
                String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
                String stringDate = String.format("%s ", date.attr("datetime"));
                postsList.add(new Post(id.incrementAndGet(), vacancyName, vacancyLink,
                        vacancyName, dateTimeParser.parse(stringDate)));
            });
        }
        return postsList;
    }
}
