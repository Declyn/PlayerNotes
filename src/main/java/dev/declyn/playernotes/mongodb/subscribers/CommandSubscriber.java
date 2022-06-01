package dev.declyn.playernotes.mongodb.subscribers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class CommandSubscriber implements Subscriber<Object> {

    private final long requests;

    public CommandSubscriber() {
        this.requests = 1;
    }

    public CommandSubscriber(long requests) {
        this.requests = requests;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(requests);
    }

    @Override
    public final void onNext(Object object) {
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {}

}