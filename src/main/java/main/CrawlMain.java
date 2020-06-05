package main;

import pageProcesser.JDPhone;
import lombok.extern.log4j.Log4j;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

@Log4j
public class CrawlMain {
    public static void main(String[] args) {
        log.info("crawler starting...");
        JDPhone jdPhone = new JDPhone();
        Spider spider = Spider.create(jdPhone);
        spider.addPipeline(new JsonFilePipeline("crawl-result"));
        spider.thread(1);
        spider.addUrl("https://list.jd.com/list.html?cat=9987%2C653%2C655&page=1&s=1&click=1");
        spider.run();
    }
}
