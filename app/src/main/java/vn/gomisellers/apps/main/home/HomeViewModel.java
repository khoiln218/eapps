package vn.gomisellers.apps.main.home;

import androidx.lifecycle.MutableLiveData;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import vn.gomisellers.apps.BaseViewModel;
import vn.gomisellers.apps.EappsApplication;
import vn.gomisellers.apps.adapter.ProductItemAdapter;
import vn.gomisellers.apps.data.ProductRepository;
import vn.gomisellers.apps.data.ResultListener;
import vn.gomisellers.apps.data.ShopRepository;
import vn.gomisellers.apps.data.source.model.api.MegaCategoryRequest;
import vn.gomisellers.apps.data.source.model.api.ResponseData;
import vn.gomisellers.apps.data.source.model.api.ShopRequest;
import vn.gomisellers.apps.data.source.model.api.ToggleProductRequest;
import vn.gomisellers.apps.data.source.model.data.Category;
import vn.gomisellers.apps.data.source.model.data.Product;
import vn.gomisellers.apps.data.source.model.data.Shop;
import vn.gomisellers.apps.data.source.remote.ResultCode;
import vn.gomisellers.apps.event.OnLoadMoreListener;
import vn.gomisellers.apps.event.ProductHandler;
import vn.gomisellers.apps.utils.ToastUtils;

/**
 * Created by KHOI LE on 3/30/2020.
 */
public class HomeViewModel extends BaseViewModel<HomeEvent> implements ProductHandler, OnLoadMoreListener, TabLayout.OnTabSelectedListener {
    private static final int INIT_PAGE = 1;
    private static final int ALL = 0;

    private ProductRepository mProductRepository;
    private ShopRepository mShopRepository;

    public MutableLiveData<Shop> shop;
    public MutableLiveData<List<Category>> categories;
    public MutableLiveData<ProductItemAdapter> productItemAdapter;

    private ProductItemAdapter adapter;
    private List<Product> products;
    private List<Category> categoryList;

    private int categoryId;
    private Shop mShop;
    private int page;
    private int totalPage;

    public HomeViewModel() {
        mShopRepository = ShopRepository.getInstance();
        mProductRepository = ProductRepository.getInstance();
        shop = new MutableLiveData<>();
        categories = new MutableLiveData<>();
        productItemAdapter = new MutableLiveData<>();
        products = new ArrayList<>();
        adapter = new ProductItemAdapter(products, this, this);
        productItemAdapter.setValue(adapter);
        categoryId = ALL;
    }

    public void onRefresh() {
        page = INIT_PAGE;
        products.clear();
        updateProductList();

        if (mShop == null) {
            requestShopInformation();
        } else if (categoryList == null) {
            requestMegaCategory();
        } else {
            requestProduct();
        }
    }

    @Override
    public void onLoadMore() {
        if (page >= totalPage) return;
        page++;

        products.add(null);
        requestProduct();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() > categoryList.size())
            return;

        selectCategory(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (tab.getPosition() > categoryList.size())
            return;

        selectCategory(tab.getPosition());
    }

    private void selectCategory(int position) {
        Category selectedCategory = categoryList.get(position);
        categoryId = selectedCategory.getId();

        showProgressing();
        onRefresh();
    }

    @Override
    public void onShow(Product product) {
        HomeEvent<Product> event = new HomeEvent<>(HomeEvent.SHOW_DETAIL);
        event.setData(product);
        getCmd().call(event);
    }

    @Override
    public void onPick(Product product) {
        HomeEvent<Product> event = new HomeEvent<>(HomeEvent.REMOVE_PRODUCT);
        event.setData(product);
        getCmd().call(event);
    }

    public void withdrawn() {
        getCmd().call(new HomeEvent(HomeEvent.WITHDRAW));
    }

    public void shareSNS() {
        String content = String.format("%s%s", EappsApplication.getPreferences().getSellerUrl(), mShop.getWebAddress());
        HomeEvent<String> event = new HomeEvent<>(HomeEvent.SHARE_SNS);
        event.setData(content);
        getCmd().call(event);
    }

    void requestShopInformation() {
        showProgressing();
        ShopRequest request = new ShopRequest();
        mShopRepository.findbyid(request, new ResultListener<ResponseData<Shop>>() {
            @Override
            public void onLoaded(ResponseData<Shop> result) {
                if (result.getCode() == ResultCode.CODE_OK) {
                    mShop = result.getResult();
                    updateShopInformation();
                    onRefresh();
                } else {
                    loaded();
                    ToastUtils.showToast(result.getMessage());
                }
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
                checkConnection(error);
            }
        });
    }

    void requestRemoveProduct(Product product) {
        showProgressing();
        ToggleProductRequest request = new ToggleProductRequest();
        request.setIsSelling(product.getIsSelling());
        request.setProductId(product.getId());
        mProductRepository.select(request, new ResultListener<ResponseData<Product>>() {
            @Override
            public void onLoaded(ResponseData<Product> result) {
                hideProgressing();
                if (result.getCode() == ResultCode.CODE_OK)
                    removeProduct(result.getResult());
                else
                    removeFailure(result.getMessage());
            }

            @Override
            public void onDataNotAvailable(String error) {
                hideProgressing();
                removeFailure(error);
            }
        });
    }

    private void updateShopInformation() {
        EappsApplication.getPreferences().setShop(mShop);
        shop.setValue(mShop);
    }


    private void removeFailure(String message) {
        ToastUtils.showToast(message);
    }

    private void removeProduct(Product product) {
        adapter.notifyItemRemoved(product);
    }

    private void requestMegaCategory() {
        showProgressing();
        MegaCategoryRequest request = new MegaCategoryRequest();
        mShopRepository.megacategory(request, new ResultListener<ResponseData<List<Category>>>() {
            @Override
            public void onLoaded(ResponseData<List<Category>> result) {
                if (result.getCode() == ResultCode.CODE_OK) {
                    categoryList = result.getResult();
                    updateMegaCategory();
                    requestProduct();
                } else {
                    loaded();
                    ToastUtils.showToast(result.getMessage());
                }
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
                checkConnection(error);
            }
        });
    }

    private void updateMegaCategory() {
        categories.setValue(categoryList);
    }

    private void requestProduct() {
        adapter.setLoading();
        ShopRequest request = new ShopRequest();
        request.setFindById(categoryId);
        mProductRepository.findbyshop(request, page, new ResultListener<ResponseData<List<Product>>>() {
            @Override
            public void onLoaded(ResponseData<List<Product>> result) {
                loaded();
                if (result.getCode() == ResultCode.CODE_OK) {
                    products.addAll(result.getResult());
                    totalPage = result.getResult().size() > 0 ? result.getResult().get(0).getTotalPage() : 0;
                    products.remove(null);
                    updateProductList();
                    checkProductEmpty(products);
                } else {
                    setErrorMessage(result.getMessage());
                }
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
                checkConnection(error);
            }
        });
    }

    private void updateProductList() {
        adapter.setProductList(products);
    }
}
