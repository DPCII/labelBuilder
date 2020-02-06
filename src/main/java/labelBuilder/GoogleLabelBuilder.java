package labelBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GoogleLabelBuilder {
    private String grabUrl;
    private ArrayList<String> headlines;

    public GoogleLabelBuilder(String url) throws IOException {
        this.grabUrl = url;
        this.headlines = retrieveHeadlines();

    }

//    https://news.google.com/search?q=iran%20diplomacy&hl=en-US&gl=US&ceid=US%3Aen

    private ArrayList<String> retrieveHeadlines() throws IOException {

        Document doc = Jsoup.connect(grabUrl)
                .get();

        Elements content = doc.getElementsByTag("main");


        // Declare headlines list to return to instance field
        ArrayList<String> headlines = new ArrayList<>();
        Elements articles = content.select("article").select("h3");


        // Cleaning steps: Remove comment and timestamp divs inside articles
//        articles.select("[class*=comment-count]").remove();
//        articles.select("[class*=timestamp]").remove();
        //


        for (Element each : articles) {
            headlines.add(each.text());
        }

        return headlines;
    }


    public ArrayList<String> getHeadlines() {
        return headlines;
    }


    public static void main(String[] args) throws IOException {
        String diplomacyUrl = "https://news.google.com/search?q=iran%20diplomacy&hl=en-US&gl=US&ceid=US%3Aen";
        String conflictUrl = "https://news.google.com/topics/CAAqIQgKIhtDQkFTRGdvSUwyMHZNRE56YUhBU0FtVnVLQUFQAQ?hl=en-US&gl=US&ceid=US%3Aen";
        GoogleLabelBuilder glb = new GoogleLabelBuilder(conflictUrl);
        System.out.println(glb.getHeadlines());

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("/Users/imac/IdeaProjects/newsCrawler/src/main/java/newsCrawler/google/iran_conflict.txt"));

        ArrayList<String> temp = glb.getHeadlines();


        for(int i = 0; i<temp.size(); i++) {
            bufferedWriter.write(temp.get(i));
            bufferedWriter.newLine();
            System.out.println(temp.get(i));
        }
        bufferedWriter.flush();
        bufferedWriter.close();

    }

}
