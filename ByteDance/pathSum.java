import datastructure.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 输入一棵二叉树和一个整数，打印出二叉树中节点值的和为输入整数的所有路径。
 * 从树的根节点开始往下一直到叶节点所经过的节点形成一条路径。
 * 给定如下二叉树，以及目标和 sum = 22，
 * 5
 * / \
 * 4   8
 * /   / \
 * 11  13  4
 * /  \    / \
 * 7    2  5   1
 * 返回:
 * [
 * [5,4,11,2],
 * [5,8,4,5]
 * ]
 */
public class pathSum {
    List<List<Integer>> res = new ArrayList<>();
    List<Integer> list = new ArrayList<>();

    public List<List<Integer>> fun(TreeNode root, int sum) {
        dfs(root, sum);
        return res;
    }

    private void dfs(TreeNode root, int target) {
        if (root == null) {
            return;
        }
        list.add(target);
        target -= root.val;

        if (target == 0 && root.left == null && root.right == null) {
            res.add(new ArrayList<>(list));
        }
        dfs(root.left, target);
        dfs(root.right, target);

        list.remove(list.size() - 1);
    }

    /**
     * 判断是否存在
     * @param root
     * @param sum
     * @return
     */
    public boolean fun2(TreeNode root,int sum){
        if (root==null){
            return false;
        }
        if (root.left==null&&root.right==null){
            return root.val==sum;
        }

        sum-=root.val;
        return fun2(root.left,sum)||fun2(root.right,sum);
    }

    /**
     * root = [10,5,-3,3,2,null,11,3,-2,null,1], sum = 8
     *
     *       10
     *      /  \
     *     5   -3
     *    / \    \
     *   3   2   11
     *  / \   \
     * 3  -2   1
     *
     * 返回 3。和等于 8 的路径有:
     *
     * 1.  5 -> 3
     * 2.  5 -> 2 -> 1
     * 3.  -3 -> 11
     */

    int res3=0;

    public int fun3(TreeNode root,int sum){
        if (root==null){
            return 0;
        }
        help(root,sum);
        fun3(root.left,sum);
        fun3(root.right,sum);
        return res3;
    }

    private void help(TreeNode root,int sum){
        if (root==null){
            return;
        }

        sum-=root.val;
        if (sum==0){
            res3++;
        }
        help(root.left,sum);
        help(root.right,sum);
    }

}
