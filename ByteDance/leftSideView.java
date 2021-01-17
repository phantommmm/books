import datastructure.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class leftSideView {
    List<Integer> res=new ArrayList<>();

    public static void main(String[] args) {
        String str="1";

        TreeNode root1=new TreeNode(1);
        TreeNode root2=new TreeNode(2);
        TreeNode root3=new TreeNode(3);
        TreeNode root4=new TreeNode(4);
        TreeNode root5=new TreeNode(5);
        root1.left=root2;
        root1.right=root3;
        root2.right=root5;
        root3.right=root4;
        leftSideView leftSideView=new leftSideView();
        System.out.println(leftSideView.fun(root1).toString());
    }

    public List<Integer> fun(TreeNode root){
        dfs(root, 0);
        return res;
    }

    private void dfs(TreeNode root,int depth){
        if (root==null){
            return;
        }

        if (depth==res.size()){
            res.add(root.val);
        }
        depth++;
        dfs(root.left, depth);
        dfs(root.right,depth);
    }
}
