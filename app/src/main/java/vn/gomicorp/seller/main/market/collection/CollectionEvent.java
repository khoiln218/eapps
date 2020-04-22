package vn.gomicorp.seller.main.market.collection;

import vn.gomicorp.seller.event.BaseEvent;

/**
 * Created by KHOI LE on 3/27/2020.
 */
class CollectionEvent<T> extends BaseEvent<T> {
    static final int ON_PICK = 0;
    static final int ON_SHOW = 1;

    CollectionEvent(int code) {
        super(code);
    }
}
