package com.my.myapplication;

import android.app.Activity;

import java.net.ConnectException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class Schedulers {
    Activity activity;

    public Schedulers(Activity mActivity){
        this.activity = mActivity;

    }

    private final Observable.Transformer<Object, Object> mSchedulersTransformer = observable ->
            observable.subscribeOn(rx.schedulers.Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    public <T> Observable.Transformer<T, T> applySchedulers() {
        //type casting is necessary to reuse our transformer instance
        //see 'Reusing Transformers' in reference given for more info
        return (Observable.Transformer<T, T>) mSchedulersTransformer;
    }

    private <T> Observable<T> call(Observable<T> apiObservable) {
        return apiObservable
                //startWith will emit observable inside it before another emissions from source observable
                //defer will create fresh observable only when observer subscribes it
                .startWith(Observable.defer(() -> {
                    //before calling each api, network connection is checked.
                    networkConnection netchk = new networkConnection(activity);
                    if (!netchk.isConnected()) {
                        //if network is not available, it will return error observable with ConnectException.
                        return Observable.error(new ConnectException("Device is not connected to network"));
                    } else {
                        //if it is available, it will return empty observable. Empty observable just emits onCompleted() immediately
                        return Observable.empty();
                    }
                }))
                .doOnNext(response -> {
                    //logging response on success
                    //you can change to to something else
                    //for example, if all your apis returns error codes in success, then you can throw custom exception here

                })
                .doOnError(throwable -> {
                    //printing stack trace on error
                });
    }
}
