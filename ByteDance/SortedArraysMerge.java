import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 合并N个长度为L的有序数组为一个有序数组
 *
 * 直接Arrays.sort O(NLlogNL)
 *
 * 全部放入最小堆 插入O(logn) O(NLlogNL)
 *
 * 维护长度为N的最小堆 O(NLlogN) 遍历所有元素为NL 插入为logN 所以为NLlogN
 */

public class SortedArraysMerge {

    public static void main(String[] args) {
        int[][] arr={{-999,1,3,5},{-12,2,3,9},{0,7,10,99}};
        System.out.println(Arrays.toString(mergeArrays(arr)));
    }

    /**
     * 思路：构建大小为N的最小堆，依次将N个数组首个元素放入堆中
     *      取出最小元素放入结果数组中，并且索引后移然后放入堆中，直到完成
     */
    public static int[] mergeArrays(int[][] arr){
        //首先校验数组规范
        int N=arr.length;
        if (N==0){
            return new int[0];
        }
        int L=arr[0].length;

        for (int i=0;i<N;i++){
            if (arr[i].length!=L){
                return new int[0];
            }
        }
        int[] res=new int[N*L];

        PriorityQueue<Node> queue=new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                //o1-o2就是升序(小顶堆) o2-o1就是逆序（大顶堆）
                return o1.val-o2.val;
            }
        });

        for (int i=0;i<N;i++){
            Node node=new Node(arr[i][0],i,0);
            queue.offer(node);
        }
        int idx=0;

        while (idx<N*L){
            Node node=queue.poll();
            res[idx++]=node.val;
            if (node.lIndex+1<L){
                Node temp=new Node(arr[node.nIndex][node.lIndex+1], node.nIndex,node.lIndex+1);
                queue.offer(temp);
            }
        }

        return res;
    }


    /**
     * 因为放入堆中的元素，需要知道值和索引，所以构建节点
     */
    static class Node{
        int val;
        int nIndex;
        int lIndex;

        public Node(int val,int nIndex,int lIndex){
            this.nIndex=nIndex;
            this.val=val;
            this.lIndex=lIndex;
        }
    }

}
