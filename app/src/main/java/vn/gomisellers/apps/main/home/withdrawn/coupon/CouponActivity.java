package vn.gomisellers.apps.main.home.withdrawn.coupon;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.databinding.DataBindingUtil;

import vn.gomisellers.apps.BaseActivity;
import vn.gomisellers.apps.R;

public class CouponActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
        initToolbar(getString(R.string.withdrawn_coupon));
    }

    @Override
    protected void initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_coupon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
