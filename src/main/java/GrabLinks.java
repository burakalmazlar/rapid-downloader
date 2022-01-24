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

        try (FileWriter fileWriter = new FileWriter("/home/buraka/Docs/links")) {
            String url = "https://gfx-hub.cc/3d-models/3d-prints";
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
            driver.get(url);
            //driver.manage().addCookie(new Cookie("cf_clearance", "qnT.P2sVUeX9AjZMhJZgan1Fo43Rl.bYXgS_Bn6W7Ig-1642928689-0-150"));
            TimeUnit.SECONDS.sleep(2);
            String pageSource = getPageSource(driver);

            for (int i = 1; i < 2; i++) {
                driver.navigate().to(url + "/page/" + i);
                TimeUnit.SECONDS.sleep(2);

                Document page = Jsoup.parse(getPageSource(driver));
                Elements articlePages = page.select(".shotstory-3d-image-new > a");
                for (Element articlePageLink : articlePages) {

                    String articleUrl = articlePageLink.attr("href");
                    driver.navigate().to(articleUrl);
                    TimeUnit.SECONDS.sleep(2);
                    Document article = Jsoup.parse(getPageSource(driver));
                    Elements rapidLinks = article.select(".down-link-block > a");
                    String title = articlePageLink.attr("title");
                    List<String> links = new LinkedList<>();
                    for (Element rapidLink : rapidLinks) {
                        String href = rapidLink.attr("href");
                        String phpsessionid = driver.manage().getCookieNamed("PHPSESSID").getValue();
                        HttpRequest request3 = HttpRequest.newBuilder().GET().uri(new URL(href).toURI()).headers("Cookie", "PHPSESSID="+phpsessionid,"referer",articleUrl).build();
                        HttpResponse<String> send3 = HttpClient.newHttpClient().send(request3, HttpResponse.BodyHandlers.ofString());
                        String location = send3.headers().allValues("location").stream().findFirst().get();
                        links.add("\""+location+"\"");
                        //if (location.contains("hitf.cc") || location.contains("hitfile.net")) {
                            //String fileName = rapidLink.text();
                            //String linkRecord = "Page " + i + "\t" + title + "\t" + location;
                            //System.out.println(linkRecord);
                        //}
                    }
                    String record = String.format("db.links.insertOne({page:%s,title:\"%s\",links:[%s]})", i, title, links.stream().collect(Collectors.joining(",")));
                    System.out.println(record);
                    fileWriter.write(record + ",\n");
                }
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
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