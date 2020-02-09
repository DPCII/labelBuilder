package labelBuilder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NewsApiOrgSearch {
    private static String[] urls;

//    https://newsapi.org/v2/everything?q={query}&apiKey={apikey}

    // construct search query, number of pages, list of urls as a result of those variables, and execute
    public NewsApiOrgSearch(int pages, String query) {
        urls = new String[pages];
        urls = buildUrls(pages, query);
    }

    private String[] buildUrls(int pages, String query) {

        return new String[]{"https://newsapi.org/v2/everything?q=" + query + "&apiKey=" + System.getenv("NEWSAPIORG_KEY")};
    }


    public static void main(String[] args) throws IOException {

        NewsApiOrgSearch newsApiOrgSearch = new NewsApiOrgSearch(1, "iran");

        String jsonString = newsApiOrgSearch.getJsonResponse();

//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> stories = mapper.readValue(jsonString, Map.class);

//        System.out.println(stories);
        String parentPath = "/Users/imac/IdeaProjects/labelBuilder/src/main/java/labelBuilder/";

        File jsonFile = new File(parentPath + "jsonTest.json");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jsonFile));

        bufferedWriter.write(jsonString);

        bufferedWriter.flush();
        bufferedWriter.close();


    }


    private String getJsonResponse() throws IOException {
        String result = "";

        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urls[0])
                .build();

        try(Response response = httpClient.newCall(request).execute()) {
            result = response.body().string();
        } catch (IllegalStateException e) {
            System.out.println(e);
        }
        return result;
    }


}
