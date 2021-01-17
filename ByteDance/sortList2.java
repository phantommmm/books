import datastructure.ListNode;

/**
 * 输入：head = [4,2,1,3]
 * 输出：[1,2,3,4]
 */
public class sortList2 {

    public static void main(String[] args) {
        int[] arr = {-1, 5, 3, 4, 0};
        sortList2 s = new sortList2();
        ListNode head = ListNode.initListByArr(arr);
        ListNode.printList(head);
        ListNode.printList(s.quickSort(head));
    }

    public ListNode quickSort(ListNode head) {
        //如果head为null直接返回，如果只有head一个节点，直接返回head 无需比较了
        if (head==null||head.next==null) return head;

        int pivot = head.val;

        ListNode leftDummyHead = new ListNode(-1), rightDummyHead = new ListNode(-1);
        ListNode left = leftDummyHead, right = rightDummyHead;
        ListNode cur = head;

        while (cur != null) {
            if (cur.val < pivot) {
                left.next = cur;
                left = left.next;
            } else {
                right.next = cur;
                right = right.next;
            }
            cur = cur.next;
        }
        //连接两个链表
        left.next = rightDummyHead.next;
        //结尾置Null
        right.next = null;
        //前面是以head分开 所以现在head.next为右头
        ListNode r = quickSort(head.next);
        //head一定是左边 所以head.next=null即断开两链表
        head.next = null;
        //左头
        ListNode l=quickSort(leftDummyHead.next);

        //连接两链表
        cur=l;
        while (cur.next!=null){
            cur=cur.next;
        }
        cur.next=r;

        return l;
    }


    public ListNode mergeSort(ListNode head) {
        ListNode dummy = new ListNode(-1);
        int len = getLength(head);
        //只有一个元素的情况
        dummy.next = head;

        for (int step = 1; step < len; step *= 2) {
            ListNode pre = dummy;
            ListNode cur = dummy.next;

            while (cur != null) {
                ListNode h1 = cur;
                ListNode h2 = split(h1, step);
                //先更新cur指针到下一个位置即将第二段也断开
                cur = split(h2, step);

                ListNode temp = merge(h1, h2);
                pre.next = temp;
                //pre指针移到排序好链表的末尾
                while (pre.next != null) {
                    pre = pre.next;
                }

            }
        }

        return dummy.next;
    }

    private int getLength(ListNode head) {
        ListNode cur = head;
        int len = 0;
        while (cur != null) {
            cur = cur.next;
            len++;
        }
        return len;
    }

    /**
     * 1->2->3 spilit(head,1)==> 1->null 2->3->null return 2;
     */
    private ListNode split(ListNode head, int step) {
        if (head == null) {
            return null;
        }
        ListNode cur = head;
        //注意null情况，不一定刚刚好分配
        //注意因为最后返回的是cur.next 所以要cur.next!=null
        while (step-- > 1 && cur.next != null) {
            cur = cur.next;
        }
        ListNode temp = cur.next;
        cur.next = null;

        return temp;
    }

    private ListNode merge(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(-1);
        ListNode cur = dummy;

        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                cur.next = l1;
                l1 = l1.next;
            } else {
                cur.next = l2;
                l2 = l2.next;
            }
            cur = cur.next;
        }

        cur.next = l1 == null ? l2 : l1;
        return dummy.next;
    }
}
