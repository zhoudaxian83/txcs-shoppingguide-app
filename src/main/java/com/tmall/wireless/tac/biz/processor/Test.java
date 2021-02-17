package com.tmall.wireless.tac.biz.processor;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yangqing.byq on 2021/2/14.
 */
public class Test {

    public static void main(String[] args) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                System.out.println("emit 1");
                emitter.onNext(1);
                System.out.println("emit 2");
                emitter.onNext(2);
                System.out.println("emit 3");
                emitter.onNext(3);
                System.out.println("emit complete");
                emitter.onComplete();
                System.out.println("emit 4");
                emitter.onNext(4);
            }
        }).subscribeOn(Schedulers.io())
                .map(integer -> integer + 1)
                .filter(integer -> integer % 10 == 0)
                .subscribe(new Observer<Integer>() {
            private Disposable mDisposable;
            private int i;

            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("subscribe");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer value) {
                System.out.println("onNext: " + value);
                i++;
                if (i == 2) {
                    System.out.println("dispose");
                    mDisposable.dispose();
                    System.out.println("isDisposed : " + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error");
            }

            @Override
            public void onComplete() {
                System.out.println("complete");
            }
        });
    }
}
