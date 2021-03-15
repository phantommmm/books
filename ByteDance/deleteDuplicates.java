import datastructure.ListNode;

/**
 * 给定一个排序链表，删除所有含有重复数字的节点，只保留原始链表中 没有重复出现 的数字。
 * 输入: 1->2->3->3->4->4->5
 * 输出: 1->2->5
 */
public class deleteDuplicates {

    public static void main(String[] args) {
        int[] arr={1,2,3,3,4,4,5};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        deleteDuplicates d=new deleteDuplicates();
        ListNode.printList(d.fun(head));
        head=ListNode.initListByArr(arr);
        ListNode.printList(d.fun2(head));
    }


    public ListNode fun(ListNode head){
        ListNode dummyHead=new ListNode(-1);
        dummyHead.next=head;
        ListNode p=dummyHead,q=head;

        while (q!=null&&q.next!=null){
            if (p.next.val!=q.next.val){
                p=p.next;
                q=q.next;
            }else{
                while (q.next!=null&&p.next.val==q.next.val){
                    q=q.next;
                }
                p.next=q.next;
                q=q.next;
            }
        }

        return dummyHead.next;
    }

    public ListNode fun2(ListNode head){
        ListNode cur=head;
        while (cur!=null&&cur.next!=null){
            if (cur.val==cur.next.val){
                cur.next=cur.next.next;
            }else{
                cur=cur.next;
            }
        }

        return head;
    }

}
