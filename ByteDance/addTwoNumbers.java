import datastructure.ListNode;

import java.util.*;


public class addTwoNumbers {

    public static void main(String[] args) {
        int[] arr1={7,2,4,3};
        int[] arr2={5,6,4};
        ListNode l1=ListNode.initListByArr(arr1);
        ListNode l2=ListNode.initListByArr(arr2);
        ListNode.printList(fun2(l1,l2));
    }
    /**头部为首个数字
     * 输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
     * 输出：7 -> 0 -> 8
     * 原因：342 + 465 = 807
     */
    public static ListNode fun(ListNode l1, ListNode l2){
        ListNode head=new ListNode(-1);
        ListNode cur=head;
        int carry=0;

        while(l1!=null||l2!=null||carry!=0){
            int v1=l1==null?0:l1.val;
            int v2=l2==null?0:l2.val;
            int sum=carry+v1+v2;

            cur.next=new ListNode(sum%10);
            carry=sum/10;

            if (l1!=null){
                l1=l1.next;
            }
            if (l2!=null){
                l2=l2.next;
            }
            cur=cur.next;
        }

        return head.next;
    }

    /**
     * 尾部为首个数字
     * 输入：(7 -> 2 -> 4 -> 3) + (5 -> 6 -> 4)
     * 输出：7 -> 8 -> 0 -> 7
     */
    public static ListNode fun2(ListNode l1,ListNode l2){
        Stack<Integer> stack1=new Stack<>();
        Stack<Integer> stack2=new Stack<>();

        while(l1!=null){
            stack1.push(l1.val);
            l1=l1.next;
        }
        while(l2!=null){
            stack2.push(l2.val);
            l2=l2.next;
        }

        ListNode dummyHead=null;
        int carry=0;

        while(!stack1.isEmpty()||!stack2.isEmpty()||carry!=0){
            int v1=stack1.isEmpty()?0:stack1.pop();
            int v2=stack2.isEmpty()?0:stack2.pop();
            int sum=v1+v2+carry;
            carry=sum/10;

            ListNode cur=new ListNode(sum%10);
            //dummyHead始终为头节点
            cur.next=dummyHead;
            dummyHead=cur;
        }

        return dummyHead;
    }

}
