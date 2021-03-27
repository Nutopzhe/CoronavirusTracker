package com.nikolayrybakov.covidtracker.service;

import com.nikolayrybakov.covidtracker.model.Location;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class VirusDataService {
    //url от куда берутся данные в виде csv
    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";
    private List<Location> allStats = new ArrayList<>();

    public List<Location> getAllStats() {
        return allStats;
    }

    public int getTotalCases() {
        return allStats.stream().mapToInt(Location::getTotalCases).sum();
    }

    public int getTotalNewCases() {
        return allStats.stream().mapToInt(Location::getDelta).sum();
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * * ")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<Location> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //передает тело ответа StringReader-у
        StringReader cvsReader = new StringReader(response.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(cvsReader);
        //1. Проходимся по всем записям-шапкам и новому объекту location назначаем необходимые
        //2. Добавляем в list
        for (CSVRecord record : records) {
            Location location = new Location();

            location.setCountry(record.get("Country/Region"));
            //получение значения из последнего дня
            int lastDay = Integer.parseInt(record.get(record.size() - 1));
            //получение значения из предпоследнего дня
            int secondToLast = Integer.parseInt(record.get(record.size() - 2));

            location.setTotalCases(lastDay);
            location.setDelta(lastDay - secondToLast);

            //добавление общих случаев только по стране, исключая провинцию
            if (newStats.contains(location)) {
                Location loc = newStats.get(newStats.indexOf(location));
                loc.setTotalCases(loc.getTotalCases() + location.getTotalCases());
                loc.setDelta(loc.getDelta() + location.getDelta());
            } else {
                newStats.add(location);
            }
        }
        this.allStats = newStats;
    }
}
