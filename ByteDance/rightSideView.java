import datastructure.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 二叉树的右视图
 */
public class rightSideView {
    List<Integer> res=new ArrayList<>();

    public static void main(String[] args) {
        TreeNode root1=new TreeNode(1);
        TreeNode root2=new TreeNode(2);
        TreeNode root3=new TreeNode(3);
        TreeNode root4=new TreeNode(4);
        TreeNode root5=new TreeNode(5);
        root1.left=root2;
        root1.right=root3;
        root2.right=root5;
        root3.right=root4;
        rightSideView rightSideView=new rightSideView();
        System.out.println(rightSideView.fun(root1).toString());
    }

    public List<Integer> fun(TreeNode root) {
        dfs(root, 0);
        return res;
    }

    private void dfs(TreeNode root,int depth){
        if (root==null){
            return;
        }
        //先访问当前节点 右节点 左节点
        if (depth==res.size()){
            //如果当前节点深度不在列表里，说明在当前深度当前节点为首个访问节点，加入列表
            res.add(root.val);
        }
        depth++;
        dfs(root.right,depth);
        dfs(root.left,depth );
    }
}
