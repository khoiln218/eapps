package vn.gomisellers.apps.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.gomisellers.apps.EappsApplication;
import vn.gomisellers.apps.R;
import vn.gomisellers.apps.data.source.model.data.Attribute;
import vn.gomisellers.apps.data.source.model.data.Product;
import vn.gomisellers.apps.databinding.HolderProductDescriptionBinding;
import vn.gomisellers.apps.databinding.LayoutNoResultBinding;
import vn.gomisellers.apps.databinding.LayoutProductAttributeBinding;
import vn.gomisellers.apps.databinding.LayoutSummaryBinding;
import vn.gomisellers.apps.event.ProductHandler;
import vn.gomisellers.apps.utils.Strings;
import vn.gomisellers.apps.utils.Utils;

/**
 * Created by KHOI LE on 3/27/2020.
 */
public class ProductDetailAdapter extends RecyclerView.Adapter {
    private Product product;
    private List<Integer> panelList;
    private ProductHandler productHandler;

    public ProductDetailAdapter(Product product, ProductHandler productHandler) {
        this.panelList = new ArrayList<>();
        this.product = product;
        this.productHandler = productHandler;
    }

    public void setProduct(Product product) {
        if (product == null || Strings.isNullOrEmpty(product.getId())) {
            panelList.clear();
            panelList.add(PanelType.NOT_FOUND);
        } else {
            this.product = product;
            panelList.clear();
            panelList.add(PanelType.SUMMARY);

            if (product.getAttributeList() != null && product.getAttributeList().size() > 0)
                panelList.add(PanelType.ATTRIBUTE);

            if (!TextUtils.isEmpty(product.getDescription()))
                panelList.add(PanelType.DESCRIPTION);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return panelList.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case PanelType.SUMMARY:
                return SummaryHolder.getInstance(parent);
            case PanelType.ATTRIBUTE:
                return AttributeHolder.getInstance(parent);
            case PanelType.DESCRIPTION:
                return DescriptionHolder.getInstance(parent);
            default:
                return NoResultHolder.getInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryHolder) {
            ((SummaryHolder) holder).bind(product, productHandler);
        } else if (holder instanceof AttributeHolder) {
            ((AttributeHolder) holder).bind(product.getAttributeList());
        } else if (holder instanceof DescriptionHolder) {
            ((DescriptionHolder) holder).bind(product.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return panelList == null ? 0 : panelList.size();
    }

    static class SummaryHolder extends RecyclerView.ViewHolder {
        LayoutSummaryBinding binding;

        public static SummaryHolder getInstance(ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            LayoutSummaryBinding binding = LayoutSummaryBinding.inflate(inflater, viewGroup, false);
            return new SummaryHolder(binding);
        }

        SummaryHolder(LayoutSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, ProductHandler productHandler) {
            binding.setProduct(product);
            binding.setProductHandler(productHandler);
            binding.executePendingBindings();
        }
    }

    static class DescriptionHolder extends RecyclerView.ViewHolder {
        HolderProductDescriptionBinding binding;

        public static DescriptionHolder getInstance(ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            HolderProductDescriptionBinding binding = HolderProductDescriptionBinding.inflate(inflater, viewGroup, false);
            return new DescriptionHolder(binding);
        }

        private DescriptionHolder(HolderProductDescriptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            initWebView();
        }

        private void initWebView() {
            binding.webView.getSettings().setLoadWithOverviewMode(true);
            binding.webView.setVerticalScrollBarEnabled(false);
            binding.webView.setHorizontalScrollBarEnabled(false);
            binding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
//                    binding.progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
//                    final WebView contentView = view;
//
//                    contentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                        @Override
//                        public boolean onPreDraw() {
//                            int measuredHeight = contentView.getMeasuredHeight();
//                            int height = (int) (Utils.getScreenWidth() * 1.5f);
//
//                            if (measuredHeight < height) {
//                                height = measuredHeight;
//                                binding.btnViewDetail.setVisibility(View.GONE);
//                            }
//
//                            ViewGroup.LayoutParams params = binding.webContainer.getLayoutParams();
//                            params.height = height;
//                            binding.webContainer.setLayoutParams(params);
//                            binding.progressBar.setVisibility(View.GONE);
//                            contentView.getViewTreeObserver().removeOnPreDrawListener(this);
//                            return false;
//                        }
//                    });
                }
            });
        }

        public void bind(String description) {
            final String data = EappsApplication.getInstance().getString(R.string.html).replace("{body_content}", description)
                    .replace("{width}", String.valueOf(Utils.getScreenWidth()));
            binding.webView.loadData(data, "text/html", "UTF-8");
            binding.btnViewDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup.LayoutParams params = binding.webContainer.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    binding.webContainer.setLayoutParams(params);
                    binding.btnViewDetail.setVisibility(View.GONE);
                }
            });
        }
    }

    static class AttributeHolder extends RecyclerView.ViewHolder {
        private LayoutProductAttributeBinding binding;

        public static AttributeHolder getInstance(ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            LayoutProductAttributeBinding binding = LayoutProductAttributeBinding.inflate(inflater, viewGroup, false);
            return new AttributeHolder(binding);
        }

        private AttributeHolder(LayoutProductAttributeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(List<Attribute> attributeList) {
            binding.setAttributes(attributeList);
            binding.executePendingBindings();
        }
    }

    static class NoResultHolder extends RecyclerView.ViewHolder {

        public static NoResultHolder getInstance(ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            LayoutNoResultBinding binding = LayoutNoResultBinding.inflate(inflater, viewGroup, false);
            return new NoResultHolder(binding);
        }

        NoResultHolder(LayoutNoResultBinding binding) {
            super(binding.getRoot());
        }
    }
}