package pipeline;

import data.CommentSummary;
import data.Phone;
import data.Price;
import lombok.extern.log4j.Log4j;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;

import java.awt.*;
import java.util.List;

@Log4j
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
        Field title = new TextField("TITLE", phone.getTitle(), Field.Store.YES);
        Field skuId = new StringField("SKUID", phone.getSkuId(), Field.Store.YES);
        Field url = new StringField("URL", phone.getUrl(), Field.Store.YES);

        this.doc.add(info);
        this.doc.add(title);
        this.doc.add(skuId);
        this.doc.add(url);

        this.phoneInfo = true;
    }

    public void addPriceInfo(Price price) {
        try {
            long priceVal = (long)(100 * Double.parseDouble(price.getP()));
            Field priceIndField = new LongPoint("PRICE_IND", priceVal);
            Field priceField = new StoredField("PRICE", priceVal);
            this.doc.add(priceIndField);
            this.doc.add(priceField);

            this.priceInfo = true;
        } catch (NumberFormatException e) {
            log.error("fail to parse string to double while add price info", e);
        }
    }

    public void addCommentInfo(CommentSummary comment) {
        Field goodRate = new StringField("GOOD_RATE", comment.getGoodRate(), Field.Store.YES);
        Field commentCount = new StringField("COMMENT_COUNT", comment.getCommentCount(), Field.Store.YES);

        this.doc.add(goodRate);
        this.doc.add(commentCount);

        this.commentInfo = true;
    }
}
