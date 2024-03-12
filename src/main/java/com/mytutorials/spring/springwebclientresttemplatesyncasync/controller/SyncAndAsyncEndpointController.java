package com.mytutorials.spring.springwebclientresttemplatesyncasync.controller;

import com.mytutorials.spring.springwebclientresttemplatesyncasync.domain.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@RestController
public class SyncAndAsyncEndpointController {

    public static final String uriBase = "http://127.0.0.1:8080";
    Logger logger = LoggerFactory.getLogger(SyncAndAsyncEndpointController.class);

    @GetMapping("/slow-service-tweets")
    public List<Tweet> getAllTweets() throws InterruptedException {
        Thread.sleep(2000L); // delay

        return Arrays.asList(
                new Tweet("RestTemplate rules", "@user1"),
                new Tweet("WebClient  rules", "@user2"),
                new Tweet("Which is is better?", "@user3"),
                new Tweet("OK, both are useful", "@user1"));
    }

    @GetMapping("/tweets-blocking")
    public List<Tweet> getTweetsBlocking() {
        logger.info("Starting BLOCKING controller!");

        RestClient restClient = RestClient.create();

        List<Tweet> tweets = restClient.get()
                .uri(uriBase + "/slow-service-tweets")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (tweets != null) {
            tweets.forEach(tweet -> logger.info(tweet.toString()));
        }
        logger.info("Exiting BLOCKING controller!");
        return tweets;
    }

    @GetMapping(value = "/tweets-non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> getNonBlockingTweets() {
        logger.info("Starting NON-BLOCKING Controller!");

        Flux<Tweet> tweetFlux = WebClient.create()
                .get()
                .uri(uriBase + "/slow-service-tweets")
                .retrieve()
                .bodyToFlux(Tweet.class);


        tweetFlux.subscribeOn(Schedulers.boundedElastic())
                .subscribe();

//        tweetFlux.doOnNext(tweet -> logger.info(tweet.toString())); // blocking call
        logger.info("Exiting NON-BLOCKING Controller!");
        return tweetFlux;
    }
}
