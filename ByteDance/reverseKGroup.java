import datastructure.ListNode;

/**
 * k个一组反转链表,如果节点总数不是 k 的整数倍，那么请将最后剩余的节点保持原有顺序。
 * 输入：head = [1,2,3,4,5], k = 3
 * 输出：[3,2,1,4,5]
 */
public class reverseKGroup {


    public static void main(String[] args) {
        int[] arr={1,2,3,4,5};
        ListNode head=ListNode.initListByArr(arr);
        reverseKGroup reverseKGroup=new reverseKGroup();
        ListNode.printList(reverseKGroup.fun(head,3));
    }


    public ListNode fun(ListNode head,int k){
        ListNode dummyHead=new ListNode(-1);
        dummyHead.next=head;
        ListNode pre=dummyHead;

        while (head!=null){
            ListNode tail=pre;

            for (int i=0;i<k;i++){
                tail=tail.next;
                if (tail==null){
                    return dummyHead.next;
                }
            }

            ListNode next=tail.next;

            ListNode[] reverses=reverse(head,tail);
            ListNode tempHead=reverses[0];
            ListNode tempTail=reverses[1];

            pre.next=tempHead;
            tempTail.next=next;
            pre=tempTail;
            head=tempTail.next;
        }

        return dummyHead.next;
    }


    private ListNode[] reverse(ListNode head,ListNode tail){
        //下面cur.next=pre 所以pre=tail.next
        ListNode cur=head,pre=tail.next;

        while (pre!=tail){
            ListNode next=cur.next;
            cur.next=pre;
            pre=cur;
            cur=next;
        }

        return new ListNode[]{tail,head};
    }
}
