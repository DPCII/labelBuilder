package labelBuilder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EliphindSearch {
    private ArrayList<String> headlines = new ArrayList<>();
    private static String[] urls;



    // construct search query, number of pages, list of urls as a result of those variables, and execute
    public EliphindSearch(int pages, String query) {
        urls = new String[pages];
        urls = buildUrls(pages, query);
    }

    private String[] buildUrls(int pages, String query) {
        if(pages == 1)
            return new String[]{"https://elephind.com/?a=q&results=1&r=100&e=-------en-100--1--txt-txINtxCO-" + query + "---------"};
        else {
            String[] temp = new String[pages];
            temp[0] = "https://elephind.com/?a=q&results=1&r=100&e=-------en-100--1--txt-txINtxCO-" + query + "---------";

            for(int i = 1; i< pages; i++) {
                temp[i] = "https://elephind.com/?a=q&results=1&r=" + i + "01&e=-------en-100--1--txt-txINtxCO-" + query + "---------";
            }
            return temp;
        }
    }


    public static void main(String[] args) throws IOException {
        // Multiple words must have '+' between them in the query
        EliphindSearch eliphindSearch = new EliphindSearch(15, "crimea+united+kingdom");

        List<PageRequest> pageRequestsList = new ArrayList<>(urls.length);
        for(String url : urls) {
            PageRequest page = new PageRequest(url);
            pageRequestsList.add(page);
            new Thread(page).start();
        }

        for(PageRequest request : pageRequestsList) {
            Elements results = request.waitForResults();
            if(results != null) {
                for(Element each : results) {
                    if(each.text().length() > 10)
                        eliphindSearch.headlines.add(each.text());
                }
                    System.out.println("found " + eliphindSearch.headlines.size() + " records");
            } else {
                System.out.println("Error occurred");
            }
        }

//        System.out.println(eliphindSearch.getHeadlines());
        eliphindSearch.printHeadlinesToFile();

    }



    static class PageRequest implements Runnable {
        private String url;
        private Elements results;
        private final Object lock = new Object();

        public PageRequest(String url) throws IOException {
            this.url = url;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            try {
                synchronized (lock) {
                    Connection connect = Jsoup.connect(url);

                    connect.timeout(120 * 1000);

                    this.results = connect
                            .userAgent("Mozilla/5.0")
                            .timeout(400000)
                            .get()
                            .getElementsByClass("veridiansearchresultcell")
                            .select("div")
                            .select("a")
                            .select("span");

                    System.out.println("Done crawling " + url + ", took " + (System.currentTimeMillis() - time) + " millis");
                    System.out.println("Content: " + this.results);

                    // Page specific parsing
//                    Elements content = doc.getElementsByClass("veridiansearchresultcell");

//                    this.results = content.select("div").select("a").select("span");
                    lock.notifyAll();
                }

            } catch(IOException e) {
                System.out.println("Failed after " + (System.currentTimeMillis() - time) + " millis");
//                System.out.println(e);
            }
        }

        public Elements waitForResults() {
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

    public ArrayList<String> getHeadlines() {
        return headlines;
    }

    public void printHeadlinesToFile() throws IOException {
        File f = new File("test2.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f.getAbsolutePath()));

        for(int i = 0; i < headlines.size(); i++) {
            bufferedWriter.write(headlines.get(i));
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

}
