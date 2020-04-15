package vn.gomicorp.seller.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.gomicorp.seller.BaseFragment;
import vn.gomicorp.seller.R;
import vn.gomicorp.seller.data.source.model.data.Product;
import vn.gomicorp.seller.databinding.FragmentHomeBinding;
import vn.gomicorp.seller.event.OnSelectedListener;
import vn.gomicorp.seller.main.MainActivity;
import vn.gomicorp.seller.main.home.withdrawn.WithdrawnActivity;
import vn.gomicorp.seller.utils.Intents;
import vn.gomicorp.seller.widgets.dialog.SelectProductDialogFragment;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> implements HomeListener {
    private static HomeFragment INSTANCE;

    public static HomeFragment getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HomeFragment();
        return INSTANCE;
    }

    private HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (binding == null)
            binding = FragmentHomeBinding.bind(view);
        viewModel = MainActivity.obtainViewModel(getActivity(), MainActivity.HOME);
        getViewModel().setListener(this);
        getBinding().setViewModel(getViewModel());
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().onRefreshProduct();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getViewModel().setListener(null);
    }

    @Override
    public void show(Product product) {
        Intents.startProductDetailActivity(getActivity(), product.getId());
    }

    @Override
    public void remove(Product product) {
        showDialogPickProduct(product);
    }

    @Override
    public void withdrawn() {
        startActivity(new Intent(getActivity(), WithdrawnActivity.class));
    }

    @Override
    public void shareSNS(String content) {
        if (getActivity() != null)
            Intents.startActionSend(getActivity(), getString(R.string.share), getString(R.string.share_sub), content);
    }

    private void showDialogPickProduct(Product product) {
        if (getFragmentManager() == null) return;
        final SelectProductDialogFragment selectProductDialogFragment = SelectProductDialogFragment.getInstance(product);
        selectProductDialogFragment.setListener(new OnSelectedListener() {
            @Override
            public void onSelected(Product product) {
                getViewModel().requestRemoveProduct(product);
                selectProductDialogFragment.dismiss();
            }
        });
        selectProductDialogFragment.show(getFragmentManager(), getTag());
    }
}
