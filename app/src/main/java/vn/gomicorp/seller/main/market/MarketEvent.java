package vn.gomicorp.seller.main.market;

import vn.gomicorp.seller.event.Event;

/**
 * Created by KHOI LE on 3/25/2020.
 */
public class MarketEvent extends Event {
    public static final int SELECT_ERROR = 0;
    public static final int ON_PICK = 1;

    public MarketEvent(int code) {
        this.code = code;
    }

    public MarketEvent(int code, String msg) {
        this.code = code;
        this.message = msg;
    }
}
