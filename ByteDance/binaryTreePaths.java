import datastructure.TreeNode;

import java.util.*;

/**给定一个二叉树，返回所有从根节点到叶子节点的路径。
 * 输入:
 *
 *    1
 *  /   \
 * 2     3
 *  \
 *   5
 *
 * 输出: ["1->2->5", "1->3"]
 * 解释: 所有根节点到叶子节点的路径为: 1->2->5, 1->3
 */
public class binaryTreePaths {

    List<String> res=new ArrayList<>();
    public List<String> fun(TreeNode root){
        dfs(root,"");
        return res;
    }

    private void dfs(TreeNode root,String str){
        if (root==null){
            return ;
        }
        if (root.left==null&&root.right==null){
            str+=root.val;
            res.add(str);
            return ;
        }
        str+=root.val+"->";
        dfs(root.left,str);
        dfs(root.right,str);
    }

    private List<String> fun2(TreeNode root){
        List<String> res=new ArrayList<>();
        if(root==null){
            return res;
        }
        Stack<Object> stack=new Stack<>();
        stack.push(root);
        stack.push(root.val+"");

        while (!stack.isEmpty()){
            String  path=(String)stack.pop();
            TreeNode node=(TreeNode)stack.pop();
            if (node.left==null&&node.right==null){
                res.add(path);
                continue;
            }

            if (node.right!=null){
                stack.push(node.right);
                stack.push(path+"->"+node.right.val);
            }
            if (node.left!=null){
                stack.push(node.left);
                stack.push(path+"->"+node.left.val);
            }
        }

        return res;
    }
}
