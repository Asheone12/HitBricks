package com.muen.hitbricks.rxbus;

import com.muen.hitbricks.rxbus.event.BusData;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * @author ry
 * @date 2019-05-28
 * link:https://blog.csdn.net/donkor_/article/details/79709366
 */
public class RxBus {

    private final Subject<Object> mBus;
    private RxBus() {
        // toSerialized method made bus thread safe
        mBus = PublishSubject.create().toSerialized();
    }

    public static RxBus get() {
        return Holder.BUS;
    }

    public void post(Object obj) {
        mBus.onNext(obj);
    }

    public <T> Observable<T> toObservable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public Observable<Object> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    public Disposable subscribe(Consumer<BusData> action) {
        return toObservable(BusData.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
    }

    public <T> Disposable toIOSubscribe(Class<T> tClass,Consumer<T> action) {
        return toObservable(tClass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
    }


    private static class Holder {
        private static final RxBus BUS = new RxBus();
    }
}
