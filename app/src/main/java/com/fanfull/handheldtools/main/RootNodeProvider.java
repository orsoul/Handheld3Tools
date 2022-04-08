package com.fanfull.handheldtools.main;

import android.view.View;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fanfull.handheldtools.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RootNodeProvider extends BaseNodeProvider {

  @Override public int getItemViewType() {
    return 0;
  }

  @Override public int getLayoutId() {
    return R.layout.item_func_head;
  }

  @Override public void convert(@NotNull BaseViewHolder helper, @Nullable BaseNode data) {
    RootNode entity = (RootNode) data;

    List<BaseNode> childNode = entity.getChildNode();

    //if (childNode == null || childNode.isEmpty()) {
    //  helper.itemView.setVisibility(View.GONE);
    //  return;
    //}

    helper.setVisible(R.id.iv_arrow, !childNode.isEmpty());

    if (entity.isExpanded()) {
      helper.setImageResource(R.id.iv_arrow, R.drawable.ic_arrow_down_24);
    } else {
      helper.setImageResource(R.id.iv_arrow, R.drawable.ic_arrow_right_24);
    }

    helper.setText(R.id.header, entity.getTitle());
  }

  @Override public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data,
      int position) {
    getAdapter().expandOrCollapse(position);
    //getAdapter().getRecyclerView().scrollToPosition(position);
  }
}
