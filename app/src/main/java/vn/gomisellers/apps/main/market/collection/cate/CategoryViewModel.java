package vn.gomisellers.apps.main.market.collection.cate;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import vn.gomisellers.apps.BaseViewModel;
import vn.gomisellers.apps.adapter.ProductItemAdapter;
import vn.gomisellers.apps.data.ProductRepository;
import vn.gomisellers.apps.data.ResultListener;
import vn.gomisellers.apps.data.ShopRepository;
import vn.gomisellers.apps.data.source.model.api.CategoryByIdRequest;
import vn.gomisellers.apps.data.source.model.api.ResponseData;
import vn.gomisellers.apps.data.source.model.api.ToggleProductRequest;
import vn.gomisellers.apps.data.source.model.data.Category;
import vn.gomisellers.apps.data.source.model.data.CategoryType;
import vn.gomisellers.apps.data.source.model.data.Product;
import vn.gomisellers.apps.data.source.remote.ResultCode;
import vn.gomisellers.apps.event.CategoryHandler;
import vn.gomisellers.apps.event.OnLoadMoreListener;
import vn.gomisellers.apps.event.ProductHandler;
import vn.gomisellers.apps.main.market.collection.subcate.CategoryItem;

/**
 * Created by KHOI LE on 4/7/2020.
 */
public class CategoryViewModel extends BaseViewModel<CategoryEvent> implements CategoryHandler, ProductHandler, SwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {
    private final int INIT_PAGE = 1;

    private int page;
    private int totalPage;
    private int categoryId;
    private int selectCategoryType;
    private int selectCategoryId;
    private String selectCategoryName;

    private ProductRepository mProductRepository;
    private ShopRepository mShopRepository;

    public MutableLiveData<Boolean> categoryEmpty;
    public MutableLiveData<CategoryAdapter> categoryAdapterLiveData;
    public MutableLiveData<ProductItemAdapter> productItemAdapter;

    private ProductItemAdapter adapter;
    private List<Product> products;

    private CategoryAdapter categoryAdapter;
    private List<Category> categories;

    public CategoryViewModel() {
        mProductRepository = ProductRepository.getInstance();
        mShopRepository = ShopRepository.getInstance();
        categoryEmpty = new MutableLiveData<>();
        categoryAdapterLiveData = new MutableLiveData<>();
        productItemAdapter = new MutableLiveData<>();
        page = INIT_PAGE;
        totalPage = 0;
        selectCategoryType = CategoryType.MEGA_CATEGORY;
        products = new ArrayList<>();
        adapter = new ProductItemAdapter(products, this, this);
        productItemAdapter.setValue(adapter);
        categories = new ArrayList<>();
        categoryEmpty.setValue(true);
        categoryAdapter = new CategoryAdapter(categories, this);
        categoryAdapterLiveData.setValue(categoryAdapter);
    }

    @Override
    public void onClick(Category category) {
        categoryAdapter.selectItem(category.getId());
        int ALL = 0;
        if (category.getId() == ALL) {
            selectCategoryType = CategoryType.MEGA_CATEGORY;
            selectCategoryId = categoryId;
            showLoading();
            onRefresh();
        } else {
            selectCategoryType = CategoryType.CATEGORY;
            selectCategoryId = category.getId();
            selectCategoryName = category.getName();
            openSubCategory();
        }
    }

    private void openSubCategory() {
        CategoryItem categoryItem = new CategoryItem(selectCategoryType, selectCategoryId, selectCategoryName);
        CategoryEvent<CategoryItem> event = new CategoryEvent<>(CategoryEvent.OPEN_SUB_CATEGORY);
        event.setData(categoryItem);
        getCmd().call(event);
    }

    @Override
    public void onRefresh() {
        page = INIT_PAGE;
        products.clear();
        updateProductList();
        initCategory();
    }

    private void initCategory() {
        requestProductListByCategoryId();
    }

    @Override
    public void onLoadMore() {
        if (page >= totalPage) return;
        page++;
        products.add(null);
        updateProductList();
        loadMoreCategory();
    }

    private void loadMoreCategory() {
        requestProductListByCategoryId();
    }

    void requestCategory() {
        CategoryByIdRequest request = new CategoryByIdRequest();
        request.setCategoryType(CategoryType.MEGA_CATEGORY);
        request.setFindById(selectCategoryId);
        mShopRepository.findcatebytype(request, new ResultListener<ResponseData<List<Category>>>() {
            @Override
            public void onLoaded(ResponseData<List<Category>> result) {
                loaded();
                if (result.getCode() == ResultCode.CODE_OK) {
                    categories = result.getResult();
                    updateCategory();
                }
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
            }
        });
    }

    private void requestProductListByCategoryId() {
        adapter.setLoading();
        CategoryByIdRequest request = new CategoryByIdRequest();
        request.setCategoryType(selectCategoryType);
        request.setFindById(selectCategoryId);
        mProductRepository.findbycategory(request, page, new ResultListener<ResponseData<List<Product>>>() {
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
            public void onDataNotAvailable(final String error) {
                loaded();
                checkConnection(error);
            }
        });
    }

    private void updateCategory() {
        categoryAdapter.setCategoryList(categories);
        categoryEmpty.setValue(categories.size() == 0);
        if (categories.size() > 0)
            categoryAdapter.selectItem(categories.get(0).getId());
    }

    private void updateProductList() {
        adapter.setProductList(products);
    }

    void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        this.selectCategoryId = categoryId;
    }

    void showLoading() {
        showProgressing();
    }

    @Override
    public void onShow(Product product) {
        showDetail(product);
    }

    private void showDetail(Product product) {
        CategoryEvent<Product> event = new CategoryEvent<>(CategoryEvent.SHOW_DETAIL);
        event.setData(product);
        getCmd().call(event);
    }

    @Override
    public void onPick(Product product) {
        pick(product);
    }


    private void pick(Product product) {
        CategoryEvent<Product> event = new CategoryEvent<>(CategoryEvent.PICK_PRODUCT);
        event.setData(product);
        getCmd().call(event);
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
                if (result.getCode() == ResultCode.CODE_OK)
                    updateProduct(result.getResult());
                else
                    showToast(result.getMessage());
            }

            @Override
            public void onDataNotAvailable(String error) {
                loaded();
                showToast(error);
            }
        });
    }

    private void updateProduct(Product product) {
        for (Product item : products) {
            if (TextUtils.equals(product.getId(), item.getId())) {
                item.setIsSelling(product.getIsSelling());
                adapter.notifyItemChanged(product);
                break;
            }
        }
    }
}
