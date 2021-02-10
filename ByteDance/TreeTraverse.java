import datastructure.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TreeTraverse {
    List<Integer> resInorder = new ArrayList<>();
    List<Integer> resPreorder = new ArrayList<>();
    List<Integer> resPostorder = new ArrayList<>();

    public List<Integer> preorderTraversal(TreeNode root) {
        if (root == null) {
            return resPreorder;
        }

        resPreorder.add(root.val);
        preorderTraversal(root.left);
        preorderTraversal(root.right);

        return resPreorder;
    }

    public List<Integer> preorderTraversal2(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root==null){
            return res;
        }
        Stack<TreeNode> stack=new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()){
            TreeNode node=stack.pop();
            res.add(node.val);
            if (node.right!=null){
                stack.push(node.right);
            }
            if (node.left!=null){
                stack.push(node.left);
            }
        }

        return res;
    }

    public List<Integer> inorderTraversal(TreeNode root) {
        if (root == null) {
            return resInorder;
        }
        inorderTraversal(root.left);
        resInorder.add(root.val);
        inorderTraversal(root.right);

        return resInorder;
    }

    public List<Integer> inorderTraversal2(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();

        while (!stack.isEmpty() || root != null) {
            if (root != null) {
                stack.push(root);
                root = root.left;
            } else {
                TreeNode node = stack.pop();
                res.add(node.val);
                root = node.right;
            }
        }

        return res;
    }

    public List<Integer> postorderTraversal(TreeNode root) {
        if (root == null) {
            return resPostorder;
        }
        preorderTraversal(root.left);
        preorderTraversal(root.right);
        resPostorder.add(root.val);
        return resPostorder;
    }

    public List<Integer> postorderTraversal2(TreeNode root) {
        List<Integer> res=new ArrayList<>();
        Stack<TreeNode> stack=new Stack<>();

        //左右中
        //中右左
        while (!stack.isEmpty()||root!=null){
            if (root!=null){
                res.add(0,root.val);
                stack.push(root);
                root=root.right;
            }else{
                TreeNode node=stack.pop();
                root=node.left;
            }
        }

        return res;
    }
}
