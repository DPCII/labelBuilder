package labelBuilder;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Appear to have an error where only the last of requested pages is being requested, resulting in lots of duplicates of those responses
// Should build a test method or unit test to map results after completion and make sure there aren't duplicates

public class ContextualWebSearch2 {
    private static String[] urls;

//  "https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/NewsSearchAPI?autoCorrect=false&pageNumber=" + + "&pageSize=50&q="+ +"&safeSearch=false"

    // construct search query, number of pages, list of urls as a result of those variables, and execute
    public ContextualWebSearch2(int pages, String query) {
        urls = new String[pages];
        urls = buildUrls(pages, query);
    }

    private String[] buildUrls(int pages, String query) {
        if(pages == 1)
            return new String[]{"https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/NewsSearchAPI?autoCorrect=false&pageNumber=" + pages + "&pageSize=50&q="+ query +"&safeSearch=false"};
        else {
            String[] temp = new String[pages];
            temp[0] = "https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/NewsSearchAPI?autoCorrect=false&pageNumber=1&pageSize=50&q="+ query +"&safeSearch=false";
            for(int i = 1; i < pages; i++) {
                temp[i] = "https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/NewsSearchAPI?autoCorrect=false&pageNumber=" + (i + 1) + "&pageSize=50&q="+ query +"&safeSearch=false";
            }
            return temp;
        }
    }


    public static void main(String[] args) throws IOException {
        // Multiple words must have '+' between them in the query
        ContextualWebSearch2 contextualWebSearch2 = new ContextualWebSearch2(5, "judicial+executive+legislative");

        List<PageRequest> pageRequestsList = new ArrayList<>(urls.length);
        for(String url : urls) {
            PageRequest page = new PageRequest(url);
            pageRequestsList.add(page);
            new Thread(page).start();
        }

        for(PageRequest request : pageRequestsList) {
            List<String> results = request.waitForResults();
            if(results != null) {
                for(String each : results) {
                    if(each.length() > 24)
                        contextualWebSearch2.printHeadlinesToFile(each);
                }
            } else {
                System.out.println("Error occurred");
            }
        }

    }



    static class PageRequest implements Runnable {
        private String url;
        private List<String> results;
        private final Object lock = new Object();

        public PageRequest(String url) throws IOException {
            this.url = url;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            try {
                synchronized (lock) {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .addHeader("x-rapidapi-host", "contextualwebsearch-websearch-v1.p.rapidapi.com")
                            .addHeader("x-rapidapi-key", System.getenv("CONTEXTUALWEB_KEY"))
                            .url(url)
                            .get()
                            .build();

                    String jsonBlob = client.newCall(request).execute().body().string();

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = objectMapper.readTree(jsonBlob);
                    JsonNode valueNode = jsonNode.get("value");

                    List<String> titleList = new ArrayList<>();

                    String temp = "";

                    // This process digs into the JSON response and pulls out only the titles from 50 responses
                    // Two regex cleaning steps
                    for(int i = 0; i<valueNode.size(); i++) {
                        temp = valueNode.get(i).findPath("title").toString().replaceAll("[\"\\\\]", "").replace("-", " ");
                        titleList.add(Jsoup.parse(temp).text());
                    }

                    this.results = titleList;
                    lock.notifyAll();
                }

            } catch(IOException e) {
                e.printStackTrace();
                System.out.println("Failed after " + (System.currentTimeMillis() - time) + " millis");
            }
        }

        public List<String> waitForResults() {
            synchronized (lock) {
                try {
                    while(this.results == null) {
                        lock.wait();
                    }
                    return this.results;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

    }


    public void printHeadlinesToFile(String headline) throws IOException {
        String labeledDataPath = "/Users/imac/IdeaProjects/documentClassifier/src/main/java/data/paravec/labeled/politics/";
        int num = 1;

        File f = new File(labeledDataPath + num + ".txt");

        while(f.exists()) {
            f  = new File(labeledDataPath + (++num) + ".txt");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f.getAbsolutePath()));

        bufferedWriter.write(headline);
        bufferedWriter.close();
    }

}
