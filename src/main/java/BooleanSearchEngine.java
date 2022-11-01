import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> mapWordsPageEntry;
    protected List<String> listStopWords;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        mapWordsPageEntry = new HashMap<>();
        listStopWords = readStopWords(new File("stop-ru.txt"));

        if (pdfsDir.isDirectory()) {
            List<File> filesInFolder;
            try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(pdfsDir)))) {
                filesInFolder = paths
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }
            for (File file : filesInFolder) {
                int pageNumber;
                var doc = new PdfDocument(new PdfReader(file));
                for (int i = 1; i < doc.getNumberOfPages(); i++) {
                    var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                    for (var word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        word = word.toLowerCase();

                        if (!listStopWords.contains(word)) {
                            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                        }
                    }
                    for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                        pageNumber = i;
                        mapWordsPageEntry.computeIfAbsent(
                                entry.getKey(), k -> new ArrayList<>()).add(new PageEntry(
                                file.getName(), pageNumber, entry.getValue()));
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String text) {
        String fileName;
        List<PageEntry> pageEntryList = new ArrayList<>();
        List<PageEntry> totalPageEntryList = new ArrayList<>();

        String[] words = text.split("\\P{IsAlphabetic}+");
        for (String word : words) {
            if (mapWordsPageEntry.containsKey(word)) {
                pageEntryList.addAll(mapWordsPageEntry.get(word));
            }
        }

        Map<String, Map<Integer, Integer>> map = pageEntryList.stream()
                .collect(Collectors.groupingBy(PageEntry::getPdfName,
                        Collectors.groupingBy(PageEntry::getPage,
                                Collectors.summingInt(PageEntry::getCount))));

        for (Map.Entry<String, Map<Integer, Integer>> mapEntry : map.entrySet()) {
            fileName = mapEntry.getKey();
            for (Map.Entry<Integer, Integer> mapInt : mapEntry.getValue().entrySet()) {
                totalPageEntryList.add(new PageEntry(fileName, mapInt.getKey(), mapInt.getValue()));
            }
        }
        Collections.sort(totalPageEntryList);
        return totalPageEntryList;
    }

    public static List<String> readStopWords(File fileName) {
        List<String> listStopWords = null;

        try (Stream<String> stream = Files.lines(Paths.get(String.valueOf(fileName)))) {
            listStopWords = stream.collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listStopWords;
    }
}
