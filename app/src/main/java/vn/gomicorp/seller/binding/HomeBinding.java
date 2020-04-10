package vn.gomicorp.seller.binding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vn.gomicorp.seller.EappsApplication;
import vn.gomicorp.seller.R;
import vn.gomicorp.seller.data.source.model.data.Category;
import vn.gomicorp.seller.databinding.TabCategoryItemBinding;
import vn.gomicorp.seller.utils.DateTimes;
import vn.gomicorp.seller.utils.Numbers;
import vn.gomicorp.seller.utils.Utils;

/**
 * Created by KHOI LE on 3/31/2020.
 */
public class HomeBinding {

    @BindingAdapter("setTextBalance")
    public static void setTextBalance(TextView textView, double pointBalance) {
        textView.setText(Numbers.doubleFormat(pointBalance));
    }

    @BindingAdapter("setTextShopUrl")
    public static void setTextShopUrl(TextView textView, String webAddress) {
        textView.setText(String.format("%s%s", EappsApplication.getPreferences().getSellerUrl(), webAddress));
    }

    @BindingAdapter("setDateRange")
    public static void setDateRange(TextView textView, Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        textView.setText(String.format("%s - %s", DateTimes.toString(calendar.getTime()), DateTimes.toString(new Date())));
    }

    @BindingAdapter("initLayoutHome")
    public static void initLayoutHome(View view, Void _v) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int) (Utils.getScreenWidth() * (3 / 4f));
        view.setLayoutParams(params);
    }

    @BindingAdapter("setCover")
    public static void setHomeCover(ImageView imageView, String coverUrl) {
        Glide.with(imageView)
                .load(coverUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.img_home_banner)
                        .override(Utils.getScreenWidth())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(imageView);
    }

    @BindingAdapter("setTabIcon")
    public static void setTabIcon(ImageView imageView, String iconUrl) {
        Glide.with(imageView)
                .load(iconUrl)
                .apply(new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(imageView);
    }

    @BindingAdapter({"setHomeCategories", "onTabSelectedListener"})
    public static void initHomeTab(TabLayout tabLayout, List<Category> categories, TabLayout.OnTabSelectedListener onTabSelectedListener) {
        if (categories == null)
            return;
        for (Category category : categories) {
            LayoutInflater inflater = LayoutInflater.from(tabLayout.getContext());
            TabCategoryItemBinding binding = TabCategoryItemBinding.inflate(inflater, null, false);
            binding.setCategory(category);

            tabLayout.addTab(tabLayout.newTab().setCustomView(binding.getRoot()));
            if (onTabSelectedListener != null)
                tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        }
    }
}