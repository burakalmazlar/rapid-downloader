//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.openqa.selenium.Cookie;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//
//import java.io.FileWriter;
//import java.time.Duration;
//import java.util.concurrent.TimeUnit;

public class GrabLinks {
    public GrabLinks() {
    }
/*
    public void grabLinks() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try (FileWriter fileWriter = new FileWriter("/home/buraka/Docs/links")) {
            String url = "https://gfxdomain.co/category/3d-models";
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
            driver.get(url);
            driver.manage().addCookie(new Cookie("cf_clearance", "qnT.P2sVUeX9AjZMhJZgan1Fo43Rl.bYXgS_Bn6W7Ig-1642928689-0-150"));
            TimeUnit.SECONDS.sleep(8);
            String pageSource = getPageSource(driver);

            for (int i = 26; i < 27; i++) {
                driver.navigate().to(url + "/page/" + i);
                TimeUnit.SECONDS.sleep(2);

                Document page = Jsoup.parse(getPageSource(driver));
                Elements articlePages = page.select("article.post > div.post-info > header.entry-header > h1.entry-title > a");
                for (Element articlePageLink : articlePages) {

                    String articleUrl = articlePageLink.attr("href");
                    driver.navigate().to(articleUrl);
                    TimeUnit.SECONDS.sleep(2);
                    Document article = Jsoup.parse(getPageSource(driver));
                    Elements rapidLinks = article.select("a");
                    for (Element rapidLink : rapidLinks) {
                        String href = rapidLink.attr("href");
                        if (href.contains("rg.to") || href.contains("rapidgator.net")) {
                            String title = articlePageLink.text();
                            String fileName = rapidLink.text();
                            String linkRecord = "Page " + i + "\t" + title + "\t" + fileName + "\t" + href;
                            System.out.println(linkRecord);
                            fileWriter.write(linkRecord + "\n");
                        }
                    }
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
    */
}