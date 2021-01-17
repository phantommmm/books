import datastructure.ListNode;

/**
 * 题目描述：一个链表，奇数位升序偶数位降序，让链表变成升序的。
 * 比如：1 8 3 6 5 4 7 2 9，最后输出1 2 3 4 5 6 7 8 9。
 */


public class sortList {

    public static void main(String[] args) {
        sortList sortList = new sortList();
        ListNode head = sortList.init();
        sortList.printList(head);
        sortList.printList(sortList.fun(head));
    }

    private ListNode init() {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(8);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(6);
        ListNode node5 = new ListNode(5);
        ListNode node6 = new ListNode(4);
        ListNode node7 = new ListNode(7);
        ListNode node8 = new ListNode(2);
        ListNode node9 = new ListNode(9);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;
        node5.next = node6;
        node6.next = node7;
        node7.next = node8;
        node8.next = node9;
        return node1;
    }

    //打印链表
    private void printList(ListNode head) {
        if (head == null) {
            return;
        }
        ListNode cur = head;
        while (cur.next != null) {
            System.out.print(cur.val + "\t");
            cur = cur.next;
        }
        System.out.println(cur.val);
    }


    public ListNode fun(ListNode head) {
        ListNode[] heads = splitList(head);
        ListNode head2 = reverser(heads[1]);
        return merge(heads[0], head2);
    }

    //1.首先根据奇数位和偶数位拆分成两个链表
    private ListNode[] splitList(ListNode head) {
        ListNode head1 = null;
        ListNode head2 = null;
        ListNode cur1 = null;
        ListNode cur2 = null;
        boolean flag = true;//奇数

        while (head != null) {
            if (flag) {
                if (head1 == null) {
                    head1 = head;
                    cur1 = head1;
                } else {
                    cur1.next = head;
                    cur1 = cur1.next;
                }
                flag = false;
            } else {
                if (head2 == null) {
                    head2 = head;
                    cur2 = head2;
                } else {
                    cur2.next = head;
                    cur2 = cur2.next;
                }
                flag = true;
            }
            head = head.next;
        }
        //最后节点next指向null
        cur1.next = null;
        cur2.next = null;
        ListNode[] heads = {head1, head2};

        return heads;
    }

    //2.反转偶数链表
    private ListNode reverser(ListNode head) {
        ListNode cur = head;
        ListNode pre = null;

        while (cur != null) {
            ListNode next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }

        return pre;
    }

    //3.合并两个有序链表
    public ListNode merge(ListNode head1, ListNode head2) {
        if (head1 == null || head2 == null) {
            return head1 == null ? head2 : head1;
        }
        ListNode pre = new ListNode(-1);
        ListNode cur = pre;

        while (head1 != null && head2 != null) {
            if (head1.val <= head2.val) {
                cur.next = head1;
                head1 = head1.next;
            } else {
                cur.next = head2;
                head2 = head2.next;
            }
            cur = cur.next;
        }
        cur.next = head1 == null ? head2 : head1;

        return pre.next;
    }

}
