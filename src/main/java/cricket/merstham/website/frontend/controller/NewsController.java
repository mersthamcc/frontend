package cricket.merstham.website.frontend.controller;

import cricket.merstham.website.frontend.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Map;

import static cricket.merstham.website.frontend.helpers.RoutesHelper.NEWS_HOME_ROUTE;

@Controller("NewsController")
public class NewsController {
    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping(value = NEWS_HOME_ROUTE, name = "news")
    public ModelAndView home(@RequestParam(name = "page", defaultValue = "1") int page) throws IOException {
        var news = newsService.feed(page);
        return new ModelAndView(
                "news/home",
                Map.of(
                        "news", news.getData(),
                        "totalPages", Math.floorDiv(news.getRecordsTotal(), 10) + 1,
                        "page", page));
    }
}