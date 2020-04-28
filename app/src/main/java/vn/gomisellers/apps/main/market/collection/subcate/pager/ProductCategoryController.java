package vn.gomisellers.apps.main.market.collection.subcate.pager;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import vn.gomisellers.apps.EappsApplication;
import vn.gomisellers.apps.R;
import vn.gomisellers.apps.adapter.ProductItemAdapter;
import vn.gomisellers.apps.data.ProductRepository;
import vn.gomisellers.apps.data.ResultListener;
import vn.gomisellers.apps.data.source.model.api.CategoryByIdRequest;
import vn.gomisellers.apps.data.source.model.api.ResponseData;
import vn.gomisellers.apps.data.source.model.api.ToggleProductRequest;
import vn.gomisellers.apps.data.source.model.data.Product;
import vn.gomisellers.apps.data.source.remote.ResultCode;
import vn.gomisellers.apps.event.MultableLiveEvent;
import vn.gomisellers.apps.event.OnLoadMoreListener;
import vn.gomisellers.apps.event.ProductHandler;
import vn.gomisellers.apps.utils.ConnectionHelper;
import vn.gomisellers.apps.utils.ToastUtils;

/**
 * Created by KHOI LE on 4/6/2020.
 */
public class ProductCategoryController implements ProductHandler, OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int INIT_PAGE = 1;

    private ProductRepository mProductRepository;

    public MutableLiveData<Boolean> isProgressing;
    public MutableLiveData<String> errorMessage;
    public MutableLiveData<Boolean> refreshing;

    public MutableLiveData<ProductItemAdapter> productItemAdapter;
    private MultableLiveEvent<ProductCategoryEvent> cmd;

    private ProductItemAdapter adapter;
    private List<Product> products;
    private int categoryType;
    private int categoryId;
    private int page;
    private int totalPage;

    ProductCategoryController() {
        mProductRepository = ProductRepository.getInstance();
        productItemAdapter = new MutableLiveData<>();
        isProgressing = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        refreshing = new MutableLiveData<>();
        cmd = new MultableLiveEvent<>();
        products = new ArrayList<>();
        adapter = new ProductItemAdapter(products, this, this);
        productItemAdapter.setValue(adapter);
    }

    @Override
    public void onShow(Product product) {
        showDetail(product);
    }

    private void showDetail(Product product) {
        ProductCategoryEvent event = new ProductCategoryEvent(ProductCategoryEvent.ON_SHOW);
        event.setData(product);
        cmd.call(event);
    }

    @Override
    public void onPick(Product product) {
        pick(product);
    }

    private void pick(Product product) {
        ProductCategoryEvent event = new ProductCategoryEvent(ProductCategoryEvent.ON_PICK);
        event.setData(product);
        cmd.call(event);
    }

    void requestPickProduct(Product product) {
        showLoading();
        ToggleProductRequest request = new ToggleProductRequest();
        request.setIsSelling(product.getIsSelling());
        request.setProductId(product.getId());
        mProductRepository.select(request, new ResultListener<ResponseData<Product>>() {
            @Override
            public void onLoaded(ResponseData<Product> result) {
                loaded();
                if (result.getCode() == ResultCode.CODE_OK) {
                    notifyChange(result.getResult());
                } else {
                    updateFail(result.getMessage());
                }
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
                updateFail(error);
            }
        });
    }

    private void notifyChange(Product product) {
        ProductCategoryEvent<Product> event = new ProductCategoryEvent<>(ProductCategoryEvent.ON_PICK);
        event.setData(product);
        EventBus.getDefault().post(event);
    }

    private void updateFail(String message) {
        ToastUtils.showToast(message);
    }

    void updateProduct(Product product) {
        for (Product item : products) {
            if (TextUtils.equals(product.getId(), item.getId())) {
                item.setIsSelling(product.getIsSelling());
                adapter.notifyItemChanged(product);
                break;
            }
        }
    }

    @Override
    public void onRefresh() {
        page = INIT_PAGE;
        products.clear();
        updateProductList();
        requestProductByCategory();
    }

    @Override
    public void onLoadMore() {
        if (page >= totalPage) return;
        page++;
        products.add(null);
        updateProductList();
        requestProductByCategory();
    }

    private void requestProductByCategory() {
        adapter.setLoading();
        CategoryByIdRequest request = new CategoryByIdRequest();
        request.setCategoryType(categoryType);
        request.setFindById(categoryId);
        mProductRepository.findbycategory(request, page, new ResultListener<ResponseData<List<Product>>>() {
            @Override
            public void onLoaded(ResponseData<List<Product>> result) {
                loaded();
                if (result.getCode() == ResultCode.CODE_OK) {
                    products.addAll(result.getResult());
                    totalPage = result.getResult().size() > 0 ? result.getResult().get(0).getTotalPage() : 0;
                    products.remove(null);
                    updateProductList();
                    checkEmpty(products);
                } else {
                    setErrorMessage(result.getMessage());
                }
            }

            @Override
            public void onDataNotAvailable(final String error) {
                loaded();
                checkConnection(error);
            }
        });
    }

    private void updateProductList() {
        adapter.setProductList(products);
    }

    MultableLiveEvent<ProductCategoryEvent> getCmd() {
        return cmd;
    }

    void setCategoryType(int categoryType) {
        this.categoryType = categoryType;
    }

    void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    void showLoading() {
        showProgressing();
    }

    protected void showProgressing() {
        isProgressing.setValue(true);
    }

    private void hideProgressing() {
        isProgressing.setValue(false);
    }

    private void setErrorMessage(String error) {
        errorMessage.setValue(error);
    }

    private void checkEmpty(List<Product> products) {
        setErrorMessage(products.size() > 0 ? null : EappsApplication.getInstance().getString(R.string.empty));
    }

    private void refreshed() {
        refreshing.setValue(false);
    }

    protected void loaded() {
        hideProgressing();
        refreshed();
    }

    private void checkConnection(final String error) {
        ConnectionHelper.getInstance().checkNetwork(new ConnectionHelper.OnCheckNetworkListener() {
            @Override
            public void onCheck(boolean isOnline) {
                if (!isOnline)
                    errorMessage.setValue(EappsApplication.getInstance().getString(R.string.network_error));
                else errorMessage.setValue(error);
            }
        });
    }
}
