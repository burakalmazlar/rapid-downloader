import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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

public class LostArkServerStatus {
    public LostArkServerStatus() {
    }

    public void getServerStatus() {
        //ChromeOptions options = new ChromeOptions();
        //WebDriver driver = new ChromeDriver(options);


        try {

            String url = "https://www.playlostark.com/en-gb/support/server-status";
            //driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
            //driver.get(url);
            TimeUnit.SECONDS.sleep(2);
            //String pageSource = getPageSource(driver);
            String pageSource = new WebClient().sendTask(url);

            Document page = Jsoup.parse(pageSource);
            Elements servers = page.select(".ags-ServerStatus-content-responses-response-server");
            for (Element server : servers) {

                String serverStatus = server.child(0).child(0).classNames().stream().filter(css -> css.contains("--")).findAny().map(css -> css.substring(css.indexOf("--") + 2, css.length())).orElse("unknown");
                String serverName = server.child(1).text();
                if ("Mokoko".equalsIgnoreCase(serverName)) {
                    String highligter = "";//("*".repeat(100) + "\n").repeat(5);
                    System.err.println(highligter + "\n".repeat(3) + serverName + " -> " + serverStatus + "\n".repeat(3) + highligter);
                }
            }

        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //driver.quit();
    }

    public String getPageSource(WebDriver driver) throws Exception {
        String pageSource = driver.getPageSource();
        if (pageSource.contains("This process is automatic. Your browser will redirect to your requested content shortly.")) {
            throw new Exception("Cloud flare!");
        }
        return pageSource;
    }
}