package main;

import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import search.Searcher;

import java.io.IOException;
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
        // TermQuery query = new TermQuery(new Term("TITLE", " 荣耀9X 麒麟810 4000mAh续航 4800万超清夜拍 6.59英寸升降全面屏 全网通6GB+64GB 魅海蓝 "));
        // List<Document> docs = searcher.search(query, 10);
        // displaySearchResult(docs, "PRICE", "TITLE", "URL");
        List<Document> docs = searcher.search("INFO", "Apple", 10);
        displaySearchResult(docs, "TITLE", "URL", "INFO");
    }

    private static void searchPrice() {
        try {
            Query query = new QueryParser("PRICE", searcher.getAnalyzer()).parse("[1000 TO 3500]");
            List<Document> docs = searcher.search(query, 10);
            displaySearchResult(docs, "PRICE", "TITLE", "URL");
        } catch (ParseException e) {
            log.error("failed to parse query", e);
        }
    }

    private static void complexSearch() {
        try {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Query query1 = new RegexpQuery(new Term("TITLE", ".*苹果.*"));
            Query query2 = new QueryParser("INFO", searcher.getAnalyzer()).parse("128GB");
            builder.add(query1, BooleanClause.Occur.MUST);
            builder.add(query2, BooleanClause.Occur.MUST);
            Query query = builder.build();

            List<Document> docs = searcher.search(query, 10);
            displaySearchResult(docs, "TITLE", "PRICE", "URL", "INFO");
        } catch (ParseException e) {
            log.error("failed to parse query", e);
        }
    }

    private static void displaySearchResult(List<Document> documents, String... fields) {
        log.info(" --------- Search Result follow --------- ");
        for (Document doc : documents) {
            StringBuilder resStr = new StringBuilder();
            for (String f : fields) {
                resStr.append('/');
                resStr.append(doc.getField(f).stringValue());
            }
            log.info(resStr.substring(1));
        }
    }
}
