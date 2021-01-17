/**
 * 二叉树最大路径和 路径被定义为一条从树中任意节点出发
 */

import datastructure.TreeNode;

/**
 * 输入：[1,2,3]
 *        1
 *       / \
 *      2   3
 * 输出：6
 */
public class maxPathSum {
    int maxSum=Integer.MIN_VALUE;

    public static void main(String[] args) {
        TreeNode treeNode1=new TreeNode(1);
        TreeNode treeNode2=new TreeNode(2);
        TreeNode treeNode3=new TreeNode(3);
        treeNode1.left=treeNode2;
        treeNode1.right=treeNode3;
        maxPathSum maxPathSum=new maxPathSum();
        System.out.println(maxPathSum.fun(treeNode1));
    }

    public int fun(TreeNode root){
            maxGain(root);
            return maxSum;
    }

    public int maxGain(TreeNode root){
        if (root==null){
            return 0;
        }
        //递归计算左右节点最大贡献值
        int leftGain=Math.max(maxGain(root.left),0);
        int rightGain=Math.max(maxGain(root.right),0);

        //节点最大路径和=当前节点值以及左右节点最大贡献值
        int price=root.val+leftGain+rightGain;
        //更新结果
        maxSum=Math.max(maxSum,price);
        //返回当前节点最大贡献值
        return root.val+Math.max(leftGain,rightGain);
    }

}
