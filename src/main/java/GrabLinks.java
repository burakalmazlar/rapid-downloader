import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileWriter;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GrabLinks {
    public GrabLinks() {
    }
    public void grabLinks() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try (MongoClient mongoClient = MongoClients.create("mongodb://user:pass@localhost:27017/database")) {

            MongoDatabase database = mongoClient.getDatabase("database");
            MongoCollection<org.bson.Document> links = database.getCollection("links");

            org.bson.Document existingLink = links.find(new org.bson.Document("title", "Final Fantasy – Aerith v1 – 3D Print")).first();
            if (null == existingLink) {
                org.bson.Document link = new org.bson.Document("_id", new ObjectId());
                link.append("page", 1).append("title", "Final Fantasy – Aerith v1 – 3D Print")
                        .append("links",
                                Arrays.asList("https://hot4share.com/yc7ov15iqbaw/Final_Fantasy_Aerith_v1.rar.html",
                                        "https://hitf.cc/9bPBgIK/Final%20Fantasy%20Aerith_v1.rar.html"));

                links.insertOne(link);
            }
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb://user:pass@localhost:27017/database")) {
            MongoDatabase database = mongoClient.getDatabase("database");
            MongoCollection<org.bson.Document> collection = database.getCollection("links");

            String url = "https://gfx-hub.cc/3d-models/3d-prints";
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
            driver.get(url);
            TimeUnit.SECONDS.sleep(2);
            String pageSource = getPageSource(driver);

            for (int i = 1; i < 117; i++) {
                String pageUrl = url + "/page/" + i;
                System.out.println("Navigation to => " + pageUrl);
                driver.navigate().to(pageUrl);
                TimeUnit.SECONDS.sleep(2);

                Document page = Jsoup.parse(getPageSource(driver));
                Elements articlePages = page.select(".shotstory-3d-image-new > a");
                for (Element articlePageLink : articlePages) {

                    String articleUrl = articlePageLink.attr("href");
                    String title = articlePageLink.attr("title");

                    org.bson.Document existingLink = collection.find(new org.bson.Document("title", title)).first();
                    if (null == existingLink) {
                        System.out.println("Navigation to => " + articleUrl);
                        driver.navigate().to(articleUrl);
                        TimeUnit.SECONDS.sleep(2);
                        Document article = Jsoup.parse(getPageSource(driver));
                        Elements rapidLinks = article.select(".down-link-block > a");
                        List<String> links = new LinkedList<>();
                        for (Element rapidLink : rapidLinks) {
                            String href = rapidLink.attr("href");
                            System.out.println("Navigation to => " + href);
                            if(href.startsWith("https://gfx-hub.cc/index.php?do=go&url=")) {
                                String phpsessionid = driver.manage().getCookieNamed("PHPSESSID").getValue();
                                HttpRequest request3 = HttpRequest.newBuilder().GET().uri(new URL(href).toURI()).headers("Cookie", "PHPSESSID="+phpsessionid,"referer",articleUrl).build();
                                HttpResponse<String> send3 = HttpClient.newHttpClient().send(request3, HttpResponse.BodyHandlers.ofString());
                                String location = send3.headers().allValues("location").stream().findFirst().get();
                                links.add("\""+location+"\"");
                            } else {
                                links.add("\""+href+"\"");
                            }
                        }
                        String record = String.format("\n\n\t\t{page:%s,title:\"%s\",links:[%s]}\n\n", i, title, links.stream().collect(Collectors.joining(",")));
                        System.err.println(record);
                        org.bson.Document link = new org.bson.Document("_id", new ObjectId());
                        link.append("page", i).append("title", title) .append("links", links);
                        collection.insertOne(link);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        driver.quit();
    }

    public String getPageSource(WebDriver driver) throws Exception {
        String pageSource = driver.getPageSource();
        if (pageSource.contains("This process is automatic. Your browser will redirect to your requested content shortly.")) {
            throw new Exception("Cloud flare!");
        }
        return pageSource;
    }
}