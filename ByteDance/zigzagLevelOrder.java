import datastructure.TreeNode;

import java.util.*;

/**
 * 之字形打印二叉树
 */
public class zigzagLevelOrder {
    public List<List<Integer>> fun(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();

        if (root == null) return res;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        boolean flag = false;
        while (!queue.isEmpty()) {
            int size = queue.size();
            List<Integer> list = new ArrayList<>();
            while (size-- > 0) {
                TreeNode node = queue.poll();
                list.add(node.val);
                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }

            if (flag) {
                Collections.reverse(list);
            }
            res.add(list);
            flag = !flag;
        }

        return res;
    }

}
