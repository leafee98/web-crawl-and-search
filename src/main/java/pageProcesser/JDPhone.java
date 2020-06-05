package pageProcesser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import data.CommentSummary;
import data.Phone;
import data.Price;
import lombok.extern.log4j.Log4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class JDPhone implements PageProcessor {

    private Site site = Site.me().setRetrySleepTime(5000).setRetryTimes(3).setSleepTime(15000)
            .setUserAgent("Mozilla/5.0 (X11; Linux x86_64; rv:76.0) Gecko/20100101 Firefox/76.0");

    private void handleContents(Page page) {
        String url = page.getRequest().getUrl();
        log.info("operating on CONTENTS page, url=" + url);

        // url of phones item page
        List<String> urls = page.getHtml().xpath("//div[@class='gl-i-wrap']/div[@class='p-img']/a/@href").all();
        for (String i : urls)
            page.addTargetRequest(i);

        log.info(String.format("get %d items page from contents", urls.size()));

        // url of next page
        Pattern reg = Pattern.compile("page=(\\d+).+s=(\\d+)");
        Matcher matcher = reg.matcher(url);
        if (matcher.find()) {
            int pageNumber = Integer.parseInt(matcher.group(1));
            int offsetNumber = Integer.parseInt(matcher.group(2));
            pageNumber += 1;
            offsetNumber += 30;

            String nextPageUrl = "https://list.jd.com/list.html?cat=9987%2C653%2C655&page="
                    + pageNumber + "&s=" + offsetNumber + "&click=1";
            page.addTargetRequest(nextPageUrl);
            log.info("next page's url generated: " + nextPageUrl);
        } else {
            log.error("failed to generate next page's url while lookup for current page number in url");
        }

        page.setSkip(true);
    }

    private void handleItem(Page page) {
        String url = page.getRequest().getUrl();
        log.info("operating on ITEM page, url=" + url);

        // get skuId from url
        Pattern reg = Pattern.compile("/(\\d+).html");
        Matcher matcher = reg.matcher(url);
        String skuId = null;
        if (matcher.find()) {
            skuId = matcher.group(1);
        } else {
            log.error("failed to get current skuId from url, url=" + url);
        }

        // get phone info
        String title = page.getHtml().xpath("//div[@class='sku-name']/text()").get();

        List<String> info = page.getHtml().xpath("//div[@class='p-parameter']//ul//li//text()").all();
        for (int i = 0; i < info.size(); ++i)
            info.set(i, info.get(i).trim());
        info.removeAll(Arrays.asList("", null));

        Phone phone = new Phone();
        phone.setTitle(title);
        phone.setSkuId(skuId);
        phone.setUrl(url);
        phone.setInfo(info);
        page.putField("PHONE_INFO", phone);
        log.info(String.format("phone found, skuId=%s, title=%s", skuId, title));

        // generate price request
        String pricePageUrl = String.format("https://p.3.cn/prices/mgets?skuIds=J_%s", skuId);
        page.addTargetRequest(pricePageUrl);
        log.info("price request url generated: " + pricePageUrl);

        // generate comments request
        String commentsUrl =
                String.format("https://club.jd.com/comment/productCommentSummaries.action?referenceIds=%s", skuId);
        page.addTargetRequest(commentsUrl);
        log.info("comments request url generated: " + commentsUrl);
    }

    private void handlePrice(Page page) {
        String url = page.getRequest().getUrl();
        log.info("operating on PRICE page, url=" + url);

        String json = page.getRawText();
        List<Price> prices = JSONArray.parseArray(json, Price.class);
        if (prices == null) {
            log.error("failed to get price, url=" + url);
        } else {
            page.putField("PRICE_INFO", prices.get(0));
            log.info(String.format("price found: skuId=%s, price=%s",
                    prices.get(0).getId(), prices.get(0).getP()));
            log.info("price found: " + prices.get(0).getP());
        }
    }

    private void handleComments(Page page) {
        String url = page.getRequest().getUrl();
        log.info("operating on COMMENTS page, url=" + url);

        String tmpStr = page.getRawText();
        String json = tmpStr.substring(tmpStr.indexOf('['), tmpStr.lastIndexOf(']') + 1);

        List<CommentSummary> commentSummaryList = JSONObject.parseArray(json, CommentSummary.class);
        page.putField("COMMENT_INFO", commentSummaryList);
        log.info(String.format("comment info get: skuId=%s, commentCount=%s",
                commentSummaryList.get(0).getSkuId(), commentSummaryList.get(0).getCommentCount()));
    }

    public void process(Page page) {
        String url = page.getRequest().getUrl();
        if (url.startsWith("https://list.jd.com/")) {
            this.handleContents(page);
        } else if (url.startsWith("https://item.jd.com")) {
            this.handleItem(page);
        } else if (url.startsWith("https://p.3.cn/prices")) {
            this.handlePrice(page);
        } else if (url.startsWith("https://club.jd.com/comment/")) {
            this.handleComments(page);
        }
    }

    public Site getSite() {
        return this.site;
    }
}
