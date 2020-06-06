package search;

import com.hankcs.hanlp.HanLP;
import com.hankcs.lucene.HanLPIndexAnalyzer;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.surround.query.SrndQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Log4j
public class Searcher {
    @Getter
    private HanLPIndexAnalyzer analyzer;
    private String searchDir;
    private IndexSearcher searcher;
    private IndexReader reader;

    public Searcher() throws IOException {
        this("lucene-index-dir");
    }
    public Searcher(String searchDir) throws IOException {
        this.searchDir = searchDir;
        this.init();
    }

    private void init() throws IOException {
        this.analyzer = new HanLPIndexAnalyzer();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(searchDir)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            log.error("failed to initialize searcher");
            throw e;
        }
    }

    public List<Document> search(String field, String keyWord, int n) {
        Query query = new RegexpQuery(new Term(field, ".*" + keyWord + ".*"));
        return search(query, n);
    }

    public List<Document> search(Query query, int n) {
        try {
            TopDocs topDocs = searcher.search(query, n);

            ScoreDoc[] hits = topDocs.scoreDocs;
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc hit : hits)
                documents.add(searcher.doc(hit.doc));

            return documents;
        } catch (IOException e) {
            log.error("error occupied while searching", e);
        }
        return null;
    }

    public void destory() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                log.error("error occupied while destroying searcher", e);
            }
        }
    }

}
