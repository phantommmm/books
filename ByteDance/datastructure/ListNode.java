package datastructure;

public class ListNode {
    public int val;
    public ListNode next;

    public ListNode(int val){
        this.val=val;
    }

    //顺序初始化链表
    public static ListNode initList(int n){
        ListNode temp=new ListNode(-1);
        ListNode cur=temp;
        for (int i=0;i<n;i++){
            ListNode node=new ListNode(i+1);
            cur.next=node;
            cur=cur.next;
        }
        return temp.next;
    }

    //根据数组值初始化链表
    public  static ListNode initListByArr(int[] arr){
        ListNode temp=new ListNode(-1);
        ListNode cur=temp;
        for (int i=0;i<arr.length;i++){
            ListNode node=new ListNode(arr[i]);
            cur.next=node;
            cur=cur.next;
        }
        return temp.next;
    }


    //打印链表
    public static void printList(ListNode head){
        if (head==null){
            return;
        }
        while (head.next!=null){
            System.out.print(head.val+"->");
            head=head.next;
        }
        System.out.println(head.val);
    }

}
