package search;

import com.hankcs.lucene.HanLPIndexAnalyzer;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
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

    public List<AbstractMap.SimpleEntry<Document, Float>> search(String field, String keyWord, int n) {
        Query query = new TermQuery(new Term(field, keyWord));
        return search(query, n);
    }

    public List<AbstractMap.SimpleEntry<Document, Float>> search(Query query, int n) {
        try {
            TopDocs topDocs = searcher.search(query, n);

            ScoreDoc[] hits = topDocs.scoreDocs;
            List<AbstractMap.SimpleEntry<Document, Float>> result = new ArrayList<>();
            for (ScoreDoc hit : hits) {
                Document doc = searcher.doc(hit.doc);
                float score = hit.score;
                AbstractMap.SimpleEntry<Document, Float> entry = new AbstractMap.SimpleEntry<>(doc, score);
                result.add(entry);
            }

            return result;
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
