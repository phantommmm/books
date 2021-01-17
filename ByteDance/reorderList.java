import datastructure.ListNode;

/**
 * 重拍链表
 * 输入 1 -> 2 -> 3 -> 4 -> 5 -> 6
 * 输出 1 -> 6 -> 2 -> 5 -> 3 -> 4
 *
 * 第一步，将链表平均分成两半
 * 1 -> 2 -> 3
 * 4 -> 5 -> 6
 *
 * 第二步，将第二个链表逆序
 * 1 -> 2 -> 3
 * 6 -> 5 -> 4
 *
 * 第三步，依次连接两个链表
 * 1 -> 6 -> 2 -> 5 -> 3 -> 4
 *
 */
public class reorderList {
    public static void main(String[] args) {
        reorderList r=new reorderList();
        int[] arr={1,2,3,4,5,6};
        ListNode head=ListNode.initListByArr(arr);
        ListNode.printList(head);
        r.fun(head);
        ListNode.printList(head);
    }


    public void fun(ListNode head){
        if (head==null||head.next==null||head.next.next==null){
            return ;
        }

        //快慢指针找中点
        ListNode slow=head,fast=head;
        while (fast.next!=null&&fast.next.next!=null){
            slow=slow.next;
            fast=fast.next.next;
        }

        //newHead为第二个链表头
        ListNode newHead=slow.next;
        //断开链表
        slow.next=null;

        //链表反转
        newHead=reverse(newHead);

        //两个链表合并
        while (newHead!=null){
            ListNode temp=newHead.next;

            newHead.next=head.next;
            head.next=newHead;

            head=newHead.next;
            newHead=temp;
        }

    }

    private ListNode reverse(ListNode head){
        ListNode pre=null;
        ListNode cur=head;

        while (cur!=null){
            ListNode next=cur.next;
            cur.next=pre;
            pre=cur;
            cur=next;
        }

        return pre;
    }

}
