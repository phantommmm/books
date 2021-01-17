import datastructure.TreeNode;

/**
 * 二叉树最长路径
 */
public class maxLength {
    public int res=0;

    public static void main(String[] args) {
        TreeNode treeNode1=new TreeNode(1);
        TreeNode treeNode2=new TreeNode(2);
        TreeNode treeNode3=new TreeNode(3);
        TreeNode treeNode4=new TreeNode(4);
        TreeNode treeNode5=new TreeNode(5);
        TreeNode treeNode6=new TreeNode(6);
        TreeNode treeNode7=new TreeNode(7);
        treeNode1.left=treeNode2;
        treeNode1.right=treeNode3;
        treeNode2.left=treeNode4;
        treeNode2.right=treeNode5;
        treeNode3.left=treeNode6;
        treeNode3.right=treeNode7;
        maxLength maxLength=new maxLength();
        System.out.println(maxLength.fun(treeNode1));
    }

    public int fun(TreeNode root){
        treeMaxLength(root);
        return res;
    }

    /**
     *遍历所有节点
     * @param node
     */
    public void treeMaxLength(TreeNode node){
        if (node!=null){
            res=Math.max(res, treeLength(node));
            treeMaxLength(node.left);
            treeMaxLength(node.right);
        }
    }

    /**
     * 树长度
     * @param node
     * @return
     */
    public int treeLength(TreeNode node){
        if (node==null){
            return 0;
        }
        int leftH=treeHeight(node.left);
        int rightH=treeHeight(node.right);
        return leftH+rightH;
    }

    /**
     * 树高度
     * @param node
     * @return
     */

    public int treeHeight(TreeNode node){
        if (node==null){
            return 0;
        }
        return Math.max(treeHeight(node.left),treeHeight(node.right))+1;
    }
}
