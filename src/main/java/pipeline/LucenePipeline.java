package pipeline;

import com.hankcs.lucene.HanLPIndexAnalyzer;
import data.CommentSummary;
import data.Phone;
import data.Price;
import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Log4j
public class LucenePipeline implements Pipeline {
    private String indexDir;
    private IndexWriter writer;
    private IndexSearcher searcher;

    public LucenePipeline() throws Exception {
        this("lucene-index-dir");
    }

    public LucenePipeline(String indexDir) throws Exception {
        this.indexDir = indexDir;
        this.initIndexWriter();
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        if (resultItems.get("PHONE_INFO") != null) {
            this.indexFieldPhone(resultItems);
        } else if (resultItems.get("PRICE_INFO")) {
            this.indexFieldPrice(resultItems);
        } else if (resultItems.get("COMMENT_INFO")) {
            this.indexFieldComment(resultItems);
        } else {
            log.warn("unrecognized field");
        }
    }

    private void initIndexWriter() throws Exception {
        HanLPIndexAnalyzer analyzer = new HanLPIndexAnalyzer();
        Directory dir;
        try {
            dir = FSDirectory.open(Paths.get(this.indexDir));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexReader indexReader = DirectoryReader.open(dir);
            this.searcher = new IndexSearcher(indexReader);
            this.writer = new IndexWriter(dir, config);
        } catch (IOException e) {
            writer = null;
            String errMsg = "index directory initialize failed, check if the directory exists";
            log.error(errMsg);
            throw new Exception(e);
        }
    }

    private Document getDocumentFromSkuId(String skuId) {
        if (searcher == null)
            return null;

        Term term = new Term("SKUID", skuId);
        TermQuery termQuery = new TermQuery(term);
        try {
            TopDocs topDocs = searcher.search(termQuery, 1);
            if (topDocs.scoreDocs.length < 1)
                return null;
            return searcher.doc(topDocs.scoreDocs[0].doc);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("fail to search document");
            return null;
        }
    }

    private void indexFieldPhone(ResultItems res) {
        Object tmpObj = res.get("PHONE_INFO");
        Phone phone;
        if (tmpObj instanceof Phone) {
            phone = (Phone)tmpObj;
        } else {
            log.warn("the object is not instance of Phone class");
            return;
        }

        Document document = getDocumentFromSkuId(phone.getSkuId());
        if (document == null)
            document = new Document();

        StringBuilder infoStr = new StringBuilder();
        List<String> infoList = phone.getInfo();
        for (String s : infoList)
            infoStr.append(s);
        Field info = new TextField("INFO", infoStr.toString(), Field.Store.YES);
        Field title = new StringField("TITLE", phone.getTitle(), Field.Store.YES);
        Field skuId = new StringField("SKUID", phone.getSkuId(), Field.Store.YES);
        Field url = new StringField("URL", phone.getUrl(), Field.Store.YES);
        try {
            document.add(info);
            document.add(title);
            document.add(skuId);
            document.add(url);
            writer.updateDocument(new Term("SKUID", phone.getSkuId()), document);
            writer.commit();
        } catch (IOException e) {
            log.error("fail to build index");
            e.printStackTrace();
        }
    }

    private void indexFieldPrice(ResultItems res) {
        Object tmpObj = res.get("PRICE_INFO");
        Price price;
        if (tmpObj instanceof Price) {
            price = (Price)tmpObj;
        } else {
            log.warn("the object is not instance of Price class");
            return;
        }

        Document document = getDocumentFromSkuId(price.getId().substring(2));
        if (document == null)
            document = new Document();

        Field priceField = new TextField("PRICE", price.getP(), Field.Store.YES);

        try {
            document.add(priceField);
            writer.updateDocument(new Term("SKUID", price.getId()), document);
            writer.commit();
        } catch (IOException e) {
            log.error("fail to build index");
            e.printStackTrace();
        }
    }

    private void indexFieldComment(ResultItems res) {
        Object tmpObj = res.get("COMMENT_INFO");
        CommentSummary comment;
        if (tmpObj instanceof CommentSummary) {
            comment = (CommentSummary) tmpObj;
        } else {
            log.warn("the object is not instance of CommentSummary class");
            return;
        }

        Document document = getDocumentFromSkuId(comment.getSkuId());
        if (document == null)
            document = new Document();

        Field goodRate = new StringField("GOOD_RATE", comment.getGoodRate(), Field.Store.YES);
        Field commentCount = new StringField("COMMENT_COUNT", comment.getCommentCount(), Field.Store.YES);

        try {
            document.add(goodRate);
            document.add(commentCount);
            writer.updateDocument(new Term("SKUID", comment.getSkuId()), document);
            writer.commit();
        } catch (IOException e) {
            log.error("fail to build index");
            e.printStackTrace();
        }
    }
}
