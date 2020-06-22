package com.fanfull.handheldtools.base;

import android.os.Bundle;
import android.view.View;

import com.fanfull.handheldtools.util.DialogUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    protected DialogUtil dialogUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        dialogUtil = new DialogUtil(this);
    }

    protected void initView() {}

    protected void showLoadingView(String msg) {
        dialogUtil.showLoadingView(msg);
    }

    protected void showLoadingView() {
        dialogUtil.showLoadingView();
    }

    protected void dismissLoadingView() {
        dialogUtil.dismissLoadingView();
    }

    @Override
    public void onClick(View v) {}

    @Override
    protected void onDestroy() {
        dialogUtil.destroy();
        super.onDestroy();
    }
}
