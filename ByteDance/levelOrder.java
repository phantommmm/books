import datastructure.TreeNode;

import java.util.*;

/**
 * 之字形打印二叉树
 */
public class levelOrder {
    public List<List<Integer>> fun(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root==null){
            return res;
        }
        Queue<TreeNode> queue=new LinkedList<>();
        boolean flag=true;
        queue.add(root);

        while (!queue.isEmpty()){
            List<Integer> list=new ArrayList<>();
            int size=queue.size();
            while(size-->0){
                TreeNode node=queue.poll();
                if(node.left!=null){
                    queue.add(node.left);
                }
                if(node.right!=null){
                    queue.add(node.right);
                }
                if(flag){
                    list.add(node.val);
                }else{
                    list.add(0,node.val);
                }
            }
            res.add(list);
            flag=!flag;
        }
        return res;
    }
}
