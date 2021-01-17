import java.util.HashMap;
import java.util.Map;

public class myLRUCache {
    //双向链表
    private class ListNode {
        ListNode pre;
        ListNode next;
        int key;
        int val;

        public ListNode(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

    private int capacity;
    private ListNode head;
    private ListNode tail;
    private Map<Integer, ListNode> map;

    public myLRUCache(int capacity) {
        this.capacity = capacity;
        head = new ListNode(-1, -1);
        tail = new ListNode(-1, -1);
        map = new HashMap<>();
        head.next = tail;
        tail.pre = head;
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        ListNode node = map.get(key);
        //改变前后节点指向
        node.pre.next = node.next;
        node.next.pre = node.pre;
        //将当前节点移到尾部
        moveToTail(node);

        return node.val;
    }

    public void put(int key,int val){
        //直接调用get，若存在则直接修改值即可
        if (get(key)!=-1){
            map.get(key).val=val;
            return ;
        }
        ListNode node=new ListNode(key,val);
        map.put(key,node);
        moveToTail(node);

        if (map.size()>capacity){
            //淘汰头节点
            map.remove(head.next.key);
            head.next=head.next.next;
            head.next.pre=head;

        }

    }


    public void moveToTail(ListNode node) {
        node.pre=tail.pre;
        tail.pre.next=node;
        node.next=tail;
        tail.pre=node;
    }

}
