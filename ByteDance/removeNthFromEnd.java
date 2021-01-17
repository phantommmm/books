import datastructure.ListNode;

/**
 * 给定一个链表: 1->2->3->4->5, 和 n = 2.
 *
 * 当删除了倒数第二个节点后，链表变为 1->2->3->5.
 */
public class removeNthFromEnd {

    public static void main(String[] args) {
        int[] arr={1,2,3,4,5};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        head=fun(head,2);
        ListNode.printList(head);
    }

    public static ListNode fun(ListNode head,int n){
        ListNode dummyHead=new ListNode(-1);
        dummyHead.next=head;
        ListNode slow=dummyHead,fast=head;

        while (n-->0){
            fast=fast.next;
        }

        while (fast!=null){
            fast=fast.next;
            slow=slow.next;
        }

        slow.next=slow.next.next;
        return dummyHead.next;
    }
}
