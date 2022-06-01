package dev.declyn.playernotes.mongodb.subscribers;

import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class RequestSubscriber implements Subscriber<Document> {

    private final long requests;

    public RequestSubscriber() {
        this(1);
    }

    public RequestSubscriber(long requests) {
        this.requests = requests;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(requests);
    }

    @Override
    public void onNext(Document document) {
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
    }

}