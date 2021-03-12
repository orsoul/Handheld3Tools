package com.fanfull.handheldtools.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;

import org.orsoul.baselib.util.ClockUtil;

public abstract class InitModuleActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModule();
    }

    protected void initModule() {}

    @Override public void onClick(View v) {
    }

    @Override public void onBackPressed() {
        if (!ClockUtil.isFastDoubleClick()) {
            ToastUtils.showShort(R.string.text_click_again_quit);
            return;
        }
        super.onBackPressed();
    }
}
