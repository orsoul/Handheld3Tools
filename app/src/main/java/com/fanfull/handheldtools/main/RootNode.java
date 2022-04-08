package com.fanfull.handheldtools.main;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.entity.node.NodeFooterImp;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RootNode extends BaseExpandNode implements NodeFooterImp {
  private String title;
  private List<BaseNode> childNode;

  public RootNode(@NonNull List<BaseNode> childNode, String title) {
    this.childNode = childNode;
    this.title = title;
    setExpanded(false);
  }

  public String getTitle() {
    return title;
  }

  /**
   * {@link BaseNode}
   * 重写此方法，获取子节点。如果没有子节点，返回 null 或者 空数组
   *
   * @return child nodes
   */
  @Nullable
  @Override
  public List<BaseNode> getChildNode() {
    return childNode;
  }

  public boolean containsChildNode(FuncNode funcBean) {
    if (!haveChildNode()) {
      return false;
    }
    return getChildNode().contains(funcBean);
  }

  public boolean haveChildNode() {
    return !childNode.isEmpty();
  }

  /**
   * {@link NodeFooterImp}
   * （可选实现）
   * 重写此方法，获取脚部节点
   */
  @Nullable
  @Override
  public BaseNode getFooterNode() {
    //return new RootFooterNode("显示更多...");
    return null;
  }
}
