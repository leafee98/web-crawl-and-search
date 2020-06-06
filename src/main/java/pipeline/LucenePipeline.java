package pipeline;

import com.hankcs.lucene.HanLPIndexAnalyzer;
import data.CommentSummary;
import data.Phone;
import data.Price;
import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Log4j
public class LucenePipeline implements Pipeline {
    private String indexDir;
    private IndexWriter writer;
    private Map<String, DocStruct> cacheDocs = new HashMap<>();

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
            this.handlePhone(resultItems);
        } else if (resultItems.get("PRICE_INFO") != null) {
            this.handlePrice(resultItems);
        } else if (resultItems.get("COMMENT_INFO") != null) {
            this.handleComment(resultItems);
        } else {
            log.warn("unrecognized field");
        }
        log.info(String.format("%d document cached in memory", cacheDocs.size()));
    }

    private void initIndexWriter() throws Exception {
        HanLPIndexAnalyzer analyzer = new HanLPIndexAnalyzer();
        Directory dir;
        try {
            dir = FSDirectory.open(Paths.get(this.indexDir));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            this.writer = new IndexWriter(dir, config);
        } catch (IOException e) {
            writer = null;
            String errMsg = "index directory initialize failed, check if the directory exists";
            log.error(errMsg);
            throw new Exception(e);
        }
    }

    private void storeDoc(Document document) {
        try {
            String skuId = document.getField("SKUID").stringValue();
            Term term = new Term("SKUID", skuId);
            writer.updateDocument(term, document);
            writer.commit();

            log.info(String.format("document with skuId '%s' stored", skuId));
        } catch (IOException e) {
            log.error("fail to store document");
            e.printStackTrace();
        }
    }

    private void handlePhone(ResultItems res) {
        Phone phone = res.get("PHONE_INFO");
        String skuId = phone.getSkuId();
        DocStruct doc = cacheDocs.get(skuId);
        if (doc == null) {
            doc = new DocStruct();
            cacheDocs.put(skuId, doc);
        }

        doc.addPhoneInfo(phone);

        if (doc.ready()) {
            storeDoc(doc.doc);
            cacheDocs.remove(skuId);
        }
    }

    private void handlePrice(ResultItems res) {
        Price price = res.get("PRICE_INFO");
        String skuId = price.getId().substring(2);
        DocStruct doc = cacheDocs.get(skuId);
        if (doc == null) {
            doc = new DocStruct();
            cacheDocs.put(skuId, doc);
        }

        doc.addPriceInfo(price);

        if (doc.ready()) {
            storeDoc(doc.doc);
            cacheDocs.remove(skuId);
        }
    }

    private void handleComment(ResultItems res) {
        CommentSummary comment = res.get("COMMENT_INFO");
        String skuId = comment.getSkuId();
        DocStruct doc = cacheDocs.get(skuId);
        if (doc == null) {
            doc = new DocStruct();
            cacheDocs.put(skuId, doc);
        }

        doc.addCommentInfo(comment);

        if (doc.ready()) {
            storeDoc(doc.doc);
            cacheDocs.remove(skuId);
        }
    }
}
