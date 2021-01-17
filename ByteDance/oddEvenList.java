import datastructure.ListNode;

/**
 * 奇偶链表
 * 输入: 1->2->3->4->5->NULL
 * 输出: 1->3->5->2->4->NULL
 */
public class oddEvenList {

    public static void main(String[] args) {
        oddEvenList oddEvenList=new oddEvenList();
        int[] arr={1,2,3,4,5};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        ListNode.printList(oddEvenList.fun(head));
    }


    public ListNode fun(ListNode head) {
        if (head == null) {
            return null;
        }
        ListNode curA=head,curB=head.next,headB=head.next;
        //原地分成奇偶链表
        while (curB!=null&&curB.next!=null){
            curA.next=curB.next;
            curA=curA.next;
            curB.next=curA.next;
            curB=curB.next;
        }
        //奇偶链表连接
        curA.next=headB;
        return head;
    }
}
