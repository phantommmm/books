import datastructure.ListNode;

/**
 * 判断环形链表
 */
public class hasCycle {
    public static void main(String[] args) {
        int[] arr={2,2,3,4};
        System.out.println(fun(arr));
    }


    public static int fun(int[] arr){
        int res=arr[0];
        for(int i=1;i<arr.length;i++){
            res^=arr[i];
        }
        return res;
    }

    public ListNode fun2(ListNode head){
        if (head==null||head.next==null){
            return null;
        }

        ListNode slow=head.next,fast=head.next.next;

        while(fast!=null&&fast.next!=null){
            if (slow==fast){
                break;
            }else{
                slow=slow.next;
                fast=fast.next.next;
            }
        }

        slow=head;
        //这里可能无环 即fast可能为null 也可能有环
        while (fast!=null&&fast!=slow){
            slow=slow.next;
            fast=fast.next;
        }

        //注意记得返回fast 因为fast可能为null或==slow
        return fast;
    }

    public boolean fun(ListNode head){
        if (head==null||head.next==null){
            return false;
        }

        ListNode slow=head.next,fast=head.next.next;

        while (fast!=null&&fast.next!=null){
            if (slow==fast){
                return true;
            }else{
                slow=slow.next;
                fast=fast.next.next;
            }
        }

        return false;
    }

}
