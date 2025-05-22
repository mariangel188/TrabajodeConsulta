import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class URLSHilosVirtuales {

    public static void main(String[] args) throws Exception {
        List<String> urls = Files.readAllLines(Paths.get("C:/Users/Maria Angel/OneDrive/Escritorio/urls.txt"));
        Map<String, Integer> resultMap = new ConcurrentHashMap<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for (String url : urls) {
                Future<?> future = executor.submit(() -> {
                    try {
                        int count = countInternalLinks(url);
                        resultMap.put(url, count);
                        System.out.println("Procesado: " + url + " → " + count + " enlaces internos");
                    } catch (Exception e) {
                        System.err.println("Error procesando " + url + ": " + e.getMessage());
                        resultMap.put(url, -1); // -1 para indicar error
                    }
                });
                futures.add(future);
            }

            // Esperar que todos terminen
            for (Future<?> f : futures) f.get();
        }

        // Guardar resultados
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("resultados.txt"))) {
            for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    // cuántos enlaces internos hay en la página
    public static int countInternalLinks(String pageUrl) throws IOException {
        Document doc = Jsoup.connect(pageUrl).get();
        URL base = new URL(pageUrl);
        String baseHost = base.getHost();

        Elements links = doc.select("a[href]");
        int count = 0;

        for (Element link : links) {
            String href = link.absUrl("href");
            if (!href.isEmpty() && new URL(href).getHost().equals(baseHost)) {
                count++;
            }
        }

        return count;
    }
}
