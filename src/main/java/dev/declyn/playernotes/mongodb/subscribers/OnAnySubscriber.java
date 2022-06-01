package dev.declyn.playernotes.mongodb.subscribers;

import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class OnAnySubscriber implements Subscriber<Document> {

    private final long requests;

    private boolean found = false;

    public OnAnySubscriber() {
        this(1);
    }

    public OnAnySubscriber(long requests) {
        this.requests = requests;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(requests);
    }

    @Override
    public final void onNext(Document document) {
        onAny(document);
        found = true;
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public final void onComplete() {
        if (found) {
            return;
        }

        onAny(null);
    }

    public abstract void onAny(Document document);

}