import datastructure.ListNode;

/**
 * 合并两个有序链表
 * 输入：1->2->4, 1->3->4
 * 输出：1->1->2->3->4->4
 */
public class mergeTwoLists {

    public static void main(String[] args) {
        mergeTwoLists mergeTwoLists = new mergeTwoLists();
        int[] arr1 = {1, 2, 4};
        int[] arr2 = {1, 3, 4};
        ListNode l1 = ListNode.initListByArr(arr1);
        ListNode l2 = ListNode.initListByArr(arr2);
        ListNode.printList(l1);
        ListNode.printList(l2);
        ListNode l3 = mergeTwoLists.fun(l1, l2);
        ListNode.printList(l3);
    }

    //递归
    public ListNode fun1(ListNode l1, ListNode l2) {
        if (l1 == null) {
            return l2;
        } else if (l2 == null) {
            return l1;
        } else if (l1.val < l2.val) {
            l1.next = fun1(l1.next, l2);
            return l1;
        } else {
            l2.next = fun1(l1, l2.next);
            return l2;
        }
    }


    //迭代
    public ListNode fun(ListNode l1, ListNode l2) {
        ListNode temp = new ListNode(-1);
        ListNode cur = temp;

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

        if (l1 == null) {
            cur.next = l2;
        } else {
            cur.next = l1;
        }

        return temp.next;
    }
}
