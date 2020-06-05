package crawl.data;

// https://club.jd.com/comment/productCommentSummaries.action?referenceIds=100008348542

import lombok.Data;

@Data
public class CommentSummary {
    private String SkuId;
    private String CommentCount;    // sum of comments
    private String CommentCountStr;
    private String GoodRate;        // rate of good comments
    private String GoodRateShow;
}
