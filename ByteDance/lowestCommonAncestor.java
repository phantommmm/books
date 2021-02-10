import datastructure.TreeNode;

/**
 * 二叉树最近公共祖先
 */
public class lowestCommonAncestor {

    public TreeNode fun(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) {
            return root;
        }

        TreeNode left = fun(root.left, p, q);
        TreeNode right = fun(root.right, p, q);

        if (left == null && right == null) {
            return null;
        }
        if (left != null && right != null) {
            return root;
        }
        return left == null ? right : left;
    }

}
