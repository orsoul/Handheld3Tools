package com.fanfull.handheldtools.main;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NodeAdapter extends BaseNodeAdapter {

  FuncNodeProvider provider;

  public NodeAdapter() {
    super();
    addFullSpanNodeProvider(new RootNodeProvider());
    provider = new FuncNodeProvider();
    addNodeProvider(provider);
  }

  @Override
  protected int getItemType(@NotNull List<? extends BaseNode> data, int position) {
    BaseNode node = data.get(position);
    if (node instanceof RootNode) {
      return 0;
    } else if (node instanceof FuncNode) {
      return 1;
    }
    return -1;
  }

  public void setOnFuncClickListener(NodeAdapter.OnFuncClickListener listener) {
    provider.setOnFuncClickListener(listener);
  }

  public interface OnFuncClickListener {
    void onFuncClick(FuncNode funcBean);
  }
}
