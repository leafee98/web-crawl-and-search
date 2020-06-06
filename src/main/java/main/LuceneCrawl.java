package main;

import pageProcesser.JDPhone;
import lombok.extern.log4j.Log4j;
import pipeline.LucenePipeline;
import us.codecraft.webmagic.Spider;

@Log4j
public class LuceneCrawl {
    public static void main(String[] args) {
        log.info("crawler starting...");
        JDPhone jdPhone = new JDPhone();
        Spider spider = Spider.create(jdPhone);

        try {
            spider.addPipeline(new LucenePipeline("crawl-lucene-result"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        spider.thread(1);
        spider.addUrl("https://list.jd.com/list.html?cat=9987%2C653%2C655&page=1&s=1&click=1");
        spider.run();
    }
}
