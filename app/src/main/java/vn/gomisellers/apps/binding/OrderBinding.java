package vn.gomisellers.apps.binding;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.gomisellers.apps.main.mypage.order.OrderAdapter;
import vn.gomisellers.apps.main.mypage.order.detail.OrderDetailAdapter;
import vn.gomisellers.apps.main.mypage.order.detail.OrderProductAdapter;

/**
 * Created by KHOI LE on 4/29/2020.
 */
public class OrderBinding {

    @BindingAdapter({"address", "ward", "district", "province"})
    public static void setAddress(TextView textView, String address, String ward, String district, String province) {
        String addressLine = address
                + ", " + ward
                + ", " + district
                + ", " + province;
        textView.setText(addressLine);
    }

    @BindingAdapter("setOrderProductAdapter")
    public static void setOrderProductAdapter(RecyclerView recyclerView, OrderProductAdapter adapter) {
        if (recyclerView.getAdapter() == null && adapter != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(adapter);
        }
    }

    @BindingAdapter("setOrderDetailAdapter")
    public static void setOrderDetailAdapter(RecyclerView recyclerView, OrderDetailAdapter adapter) {
        if (recyclerView.getAdapter() == null && adapter != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
        }
    }

    @BindingAdapter("setOrderAdapter")
    public static void setOrderAdapter(RecyclerView recyclerView, OrderAdapter adapter) {
        if (recyclerView.getAdapter() == null && adapter != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            adapter.addOnScrollListener(recyclerView);
            recyclerView.setAdapter(adapter);
        }
    }
}
