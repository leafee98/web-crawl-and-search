package pipeline;

import data.CommentSummary;
import data.Phone;
import data.Price;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.List;

class DocStruct {
    Document doc;
    boolean phoneInfo = false;
    boolean priceInfo = false;
    boolean commentInfo = false;

    public DocStruct() {
        this.doc = new Document();
    }

    public DocStruct(Document doc) {
        this.doc = doc;
    }

    public boolean ready() {
        return phoneInfo && priceInfo && commentInfo;
    }

    public void addPhoneInfo(Phone phone) {
        StringBuilder infoStr = new StringBuilder();
        List<String> infoList = phone.getInfo();
        for (String s : infoList)
            infoStr.append(s);
        Field info = new TextField("INFO", infoStr.toString(), Field.Store.YES);
        Field title = new StringField("TITLE", phone.getTitle(), Field.Store.YES);
        Field skuId = new StringField("SKUID", phone.getSkuId(), Field.Store.YES);
        Field url = new StringField("URL", phone.getUrl(), Field.Store.YES);

        this.doc.add(info);
        this.doc.add(title);
        this.doc.add(skuId);
        this.doc.add(url);

        this.phoneInfo = true;
    }

    public void addPriceInfo(Price price) {
        Field priceField = new StringField("PRICE", price.getP(), Field.Store.YES);
        this.doc.add(priceField);

        this.priceInfo = true;
    }

    public void addCommentInfo(CommentSummary comment) {
        Field goodRate = new StringField("GOOD_RATE", comment.getGoodRate(), Field.Store.YES);
        Field commentCount = new StringField("COMMENT_COUNT", comment.getCommentCount(), Field.Store.YES);

        this.doc.add(goodRate);
        this.doc.add(commentCount);

        this.commentInfo = true;
    }
}
