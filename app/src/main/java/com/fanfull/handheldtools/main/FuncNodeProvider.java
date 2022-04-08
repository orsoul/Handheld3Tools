package com.fanfull.handheldtools.main;

import android.view.View;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fanfull.handheldtools.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuncNodeProvider extends BaseNodeProvider {

  NodeAdapter.OnFuncClickListener listener;

  @Override public int getItemViewType() {
    return 1;
  }

  @Override public int getLayoutId() {
    return R.layout.item_func;
  }

  @Override
  public void convert(@NotNull BaseViewHolder helper, @Nullable BaseNode data) {
    if (data == null) {
      return;
    }

    FuncNode item = (FuncNode) data;
    helper.setText(R.id.tv_item_func, item.getName());
  }

  @Override
  public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data,
      int position) {
    if (data instanceof FuncNode && listener != null) {
      FuncNode bean = (FuncNode) data;
      listener.onFuncClick(bean);
    }
  }

  public void setOnFuncClickListener(NodeAdapter.OnFuncClickListener listener) {
    this.listener = listener;
  }
}
