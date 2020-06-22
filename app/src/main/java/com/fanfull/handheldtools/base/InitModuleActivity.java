package com.fanfull.handheldtools.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class InitModuleActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModule();
    }

    protected void initModule() {}

    @Override
    public void onClick(View v) {}
}
