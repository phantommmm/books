import datastructure.ListNode;

/**
 * 输入: 1->2->3->4->5->NULL, k = 2
 * 输出: 4->5->1->2->3->NULL
 * 解释:
 * 向右旋转 1 步: 5->1->2->3->4->NULL
 * 向右旋转 2 步: 4->5->1->2->3->NULL
 */
public class rotateRight {
    public static void main(String[] args) {
        int[] arr={1,2,3,4,5};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        head=fun(head, 2);
        ListNode.printList(head);
    }


    public static ListNode fun(ListNode head, int k){
        ListNode tail=head;
        int n=1;
        while (tail.next!=null){
            tail=tail.next;
            n++;
        }
        ListNode tempHead=head;
        tail.next=tempHead;//构造环形链表 首为相连
        int count=n-k%n;
        while (count-->0){
            tail=tail.next;
            tempHead=tempHead.next;
        }
        tail.next=null;
        return tempHead;
    }
}
