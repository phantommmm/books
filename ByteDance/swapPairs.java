import datastructure.ListNode;

/**交换链表节点
 * 输入：head = [1,2,3,4]
 * 输出：[2,1,4,3]
 */

public class swapPairs {

    public static void main(String[] args) {
        int[] arr={1,2,3,4};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        head=fun(head);
        ListNode.printList(head);
    }

    public static ListNode fun(ListNode head){
        ListNode dummyHead=new ListNode(-1);
        dummyHead.next=head;
        ListNode cur=dummyHead;

        while (cur.next!=null&&cur.next.next!=null){
            ListNode p=cur.next;
            ListNode q=cur.next.next;

            cur.next=q;
            p.next=q.next;
            q.next=p;
            cur=p;
        }

        return dummyHead.next;
    }
    /**
     * 解释一下非递归解法中最后为什么`return pre.next`，
     * 首先`ListNode temp = pre`是让`temp`指向了`pre`的内存地址，
     * 第一轮循环中`temp.next = end`，也就是`pre.next = end`，
     * 所以这个时候`pre.next`已经是新的头结点了。
     * 而后面`temp = start`操作让后续循环不会更改`pre`的`next`指针，
     * 所以最后返回`pre.next`其实就是返回了新的头结点。
     */
}
