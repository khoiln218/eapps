package vn.gomisellers.apps.binding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import vn.gomisellers.apps.main.mypage.order.detail.OrderDetailAdapter;
import vn.gomisellers.apps.main.mypage.order.detail.OrderProductAdapter;

/**
 * Created by KHOI LE on 4/29/2020.
 */
public class OrderBinding {

    @BindingAdapter("setOrderProductAdapter")
    public static void setOrderProductAdapter(RecyclerView recyclerView, OrderProductAdapter adapter) {
        if (recyclerView.getAdapter() == null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    @BindingAdapter("setOrderDetailAdapter")
    public static void setOrderDetailAdapter(RecyclerView recyclerView, OrderDetailAdapter adapter) {
        if (recyclerView.getAdapter() == null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }
}
