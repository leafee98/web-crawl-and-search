package main;

import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import search.Searcher;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

@Log4j
public class LuceneSearch {
    private static Searcher searcher;

    public static void main(String [] args) {
        if (!initSearcher()) {
            return;
        }

        searchApple();
        searchPrice();
        complexSearch();

        searcher.destory();
    }

    private static boolean initSearcher() {
        try {
            searcher = new Searcher("crawl-lucene-result");
            return true;
        } catch (IOException e) {
            log.error("error occupied while creating searcher");
            return false;
        }
    }

    private static void searchApple() {
        List<AbstractMap.SimpleEntry<Document, Float>> docs = searcher.search("TITLE", "Apple", 10);
        displaySearchResult(docs, "TITLE", "URL", "INFO");
    }

    private static void searchPrice() {
        Query query = LongPoint.newRangeQuery("PRICE_IND", 1000L * 100L, 3500L * 100L);
        List<AbstractMap.SimpleEntry<Document, Float>> docs = searcher.search(query, 10);
        displaySearchResult(docs, "PRICE", "TITLE", "URL");
    }

    private static void complexSearch() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        Query query1 = new TermQuery(new Term("TITLE", "nova"));
        Query query2 = new TermQuery(new Term("INFO", "华为"));
        builder.add(query1, BooleanClause.Occur.MUST);
        builder.add(query2, BooleanClause.Occur.MUST);
        Query query = builder.build();

        List<AbstractMap.SimpleEntry<Document, Float>> docs = searcher.search(query, 10);
        displaySearchResult(docs, "TITLE", "PRICE", "URL", "INFO");
    }

    private static void displaySearchResult(List<AbstractMap.SimpleEntry<Document, Float>> documents, String... fields) {
        log.info(" --------- Search Result follow --------- ");
        for (AbstractMap.SimpleEntry<Document, Float> entry : documents) {
            StringBuilder resStr = new StringBuilder();
            Document doc = entry.getKey();

            resStr.append("score: ").append(entry.getValue());
            for (String f : fields) {
                resStr.append('/');
                resStr.append(doc.getField(f).stringValue());
            }
            log.info(resStr.toString());
        }
    }
}
