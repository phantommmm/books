import datastructure.ListNode;

/**
 * 输入: head = 1->4->3->2->5->2, x = 3
 * 输出: 1->2->2->4->3->5
 */
public class partition {

    public static void main(String[] args) {
        int[] arr={1,4,3,2,5,2};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        head=fun(head,3);
        ListNode.printList(head);
    }


    public static ListNode fun(ListNode head, int x){
        ListNode pHead=new ListNode(-1);
        ListNode qHead=new ListNode(-1);
        ListNode p=pHead,q=qHead;
        ListNode cur=head;

        while (cur!=null){
            if (cur.val<x){
                p.next=cur;
                p=p.next;
            }else{
                q.next=cur;
                q=q.next;
            }
            cur=cur.next;
        }
        //两条链表相连
        p.next=qHead.next;
        //尾部null 否则有死循环
        q.next=null;

        return pHead.next;
    }

}
