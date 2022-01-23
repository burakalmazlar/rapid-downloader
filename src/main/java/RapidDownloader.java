import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.nio.file.Path.of;
import static java.util.function.Predicate.isEqual;

public class RapidDownloader {

    private String LINKS_FILE = "/tmp/links.txt"; // every line is a link
    private String DOWNLOAD_FOLDER = "/tmp";
    private String RAPID_USER = "[username]";
    private String RAPID_PASSWORD = "[password]";

    private int R = 0;

    public static void main(String[] args) throws IOException {

        RapidDownloader rapidDownloader = new RapidDownloader();
        rapidDownloader.LINKS_FILE = args[0];
        rapidDownloader.DOWNLOAD_FOLDER = args[1];
        rapidDownloader.RAPID_USER = args[2];
        rapidDownloader.RAPID_PASSWORD = args[3];
        rapidDownloader.run(args);
    }

    private void run(String[] args) throws IOException {
        watchTermination();
        clearCookie();

        long count = Files.lines(of(LINKS_FILE))
                .map(this::download)
                .filter(isEqual(true))
                .count();

        System.err.println("Total downloaded file : " + count);

        System.exit(0);
    }

    private void clearCookie() throws IOException {
        Path cookiePath = getCookiePath();
        if (Files.exists(cookiePath)) {
            Files.delete(cookiePath);
        }
    }

    private void watchTermination() {
        new Thread(() -> {
            try {
                System.in.read();
                R = 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean download(String file) {
        if (R == 1) {
            return false;
        }
        try {
            System.out.print(file);
            String cookie = getCookie();

            URL url = new URL(file);

            HttpRequest request3 = HttpRequest.newBuilder().GET().uri(url.toURI()).headers("Cookie", cookie).build();
            HttpResponse<String> send3 = HttpClient.newHttpClient().send(request3, HttpResponse.BodyHandlers.ofString());
            Map<String, List<String>> map3 = send3.headers().map();
            cookie += "; " + map3.get("Set-Cookie").stream().map(h -> h.substring(0, h.indexOf(';'))).collect(Collectors.joining("; "));

            URL location = new URL(map3.get("Location").stream().findFirst().get());
            /*System.out.println(location);*/

            Path downloadDir = of(DOWNLOAD_FOLDER);
            HttpResponse<Path> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().GET().uri(location.toURI())
                            .headers("Cookie", cookie).build(),
                    HttpResponse.BodyHandlers.ofFileDownload(downloadDir, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));

            Path downloadedFile = response.body();
            System.out.println(" downloaded to " + downloadedFile.toString());
            return true;
        } catch (URISyntaxException | InterruptedException | IOException exc) {
            if (exc.getCause().getClass().equals(FileAlreadyExistsException.class)) {
                System.out.println(" already downloaded.");
                return false;
            } else {
                exc.printStackTrace();
            }
        }

        return false;
    }

    private String getCookie() throws IOException, InterruptedException {
        String cookie = null;
        Path cookiePath = getCookiePath();
        if (Files.notExists(cookiePath)) {

            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create("https://rapidgator.net/auth/login")).build();
            HttpResponse<String> send = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            cookie = "  " + send.headers().map().get("Set-Cookie").stream().map(h -> h.substring(0, h.indexOf(';'))).collect(Collectors.joining("; "));

            String username = URLEncoder.encode(RAPID_USER, StandardCharsets.UTF_8);
            String password = URLEncoder.encode(RAPID_PASSWORD, StandardCharsets.UTF_8);

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString("LoginForm%5Bemail%5D=" + username + "&LoginForm%5Bpassword%5D=" + password + "&LoginForm%5Bactivation_code%5D=&LoginForm%5BtwoStepAuthCode%5D=&LoginForm%5BrememberMe%5D=0&g-recaptcha-response=&LoginForm%5Bfp%5D=", Charset.defaultCharset());
            HttpRequest request2 = HttpRequest.newBuilder().POST(bodyPublisher).uri(URI.create("https://rapidgator.net/auth/login"))
                    .headers("Content-Type", "application/x-www-form-urlencoded",
                            "Referer", "https://rapidgator.net/auth/login",
                            "Cookie", cookie).build();

            HttpResponse<String> send2 = HttpClient.newHttpClient().send(request2, HttpResponse.BodyHandlers.ofString());

            cookie += "; " + send2.headers().map().get("Set-Cookie").stream().map(h -> h.substring(0, h.indexOf(';'))).collect(Collectors.joining("; "));

            Files.writeString(cookiePath, cookie);

        } else {
            cookie = Files.readString(cookiePath);
        }
        return cookie;
    }

    private Path getCookiePath() {
        return Paths.get(DOWNLOAD_FOLDER, "cookie");
    }

}