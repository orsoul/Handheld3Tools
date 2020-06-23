package com.fanfull.handheldtools.base;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;

import org.orsoul.baselib.util.ClickUtil;

import androidx.annotation.Nullable;

public abstract class InitModuleActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModule();
    }

    protected void initModule() {}

    protected void runOnUi(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override
    public void onClick(View v) {}

    @Override
    public void onBackPressed() {
        if (!ClickUtil.isFastDoubleClick()) {
            ToastUtils.showShort(R.string.text_click_again_quit);
            return;
        }
        super.onBackPressed();
    }
}
