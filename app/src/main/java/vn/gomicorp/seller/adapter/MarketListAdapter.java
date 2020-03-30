package vn.gomicorp.seller.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.gomicorp.seller.adapter.holder.BannerSliderHolder;
import vn.gomicorp.seller.adapter.holder.CategoryHolder;
import vn.gomicorp.seller.adapter.holder.LoadingHolder;
import vn.gomicorp.seller.adapter.holder.ProductHolder;
import vn.gomicorp.seller.data.source.model.data.Collection;
import vn.gomicorp.seller.data.source.model.data.Product;
import vn.gomicorp.seller.event.CategoryHandler;
import vn.gomicorp.seller.event.CollectionHandler;
import vn.gomicorp.seller.event.ProductHandler;

public class MarketListAdapter extends RecyclerView.Adapter {
    private List<Collection> collections;
    private ProductHandler productHandler;
    private CategoryHandler categoryHandler;
    private CollectionHandler collectionHandler;
    private Product productChange;

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
        notifyDataSetChanged();
    }

    public void setProductChange(Product productChange) {
        this.productChange = productChange;
    }

    public MarketListAdapter(List<Collection> collections, ProductHandler productHandler, CategoryHandler categoryHandler, CollectionHandler collectionHandler) {
        this.collections = collections;
        this.productHandler = productHandler;
        this.categoryHandler = categoryHandler;
        this.collectionHandler = collectionHandler;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case CollectionType.BANNER:
                return BannerSliderHolder.getInstance(parent);
            case CollectionType.CATAGORY:
                return CategoryHolder.getInstance(parent);
            case CollectionType.NEW_PRODUCT:
            case CollectionType.RECOMEND_PRODUCT:
            case CollectionType.SEEN_PRODUCT:
                return ProductHolder.getInstance(parent);
            default:
                return LoadingHolder.getInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BannerSliderHolder)
            ((BannerSliderHolder) holder).setBannerSlider(collections.get(position));

        else if (holder instanceof CategoryHolder)
            ((CategoryHolder) holder).setCategoryList(collections.get(position), categoryHandler);

        else if (holder instanceof ProductHolder) {
            ((ProductHolder) holder).bind(collections.get(position), productHandler, collectionHandler, productChange);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Collection collection = collections.get(position);
        if (collection == null) {
            return CollectionType.VIEW_LOADING;
        }
        return collection.getType();
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    public interface CollectionType {
        int SEEN_PRODUCT = -3;
        int CATAGORY = -2;
        int BANNER = -1;
        int VIEW_LOADING = 0;
        int NEW_PRODUCT = 1;
        int RECOMEND_PRODUCT = 2;
    }
}
