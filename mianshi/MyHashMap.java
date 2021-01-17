import java.util.Objects;

public class MyHashMap<K, V> {

    private Node<K, V>[] table;
    private static int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static int size;//当前容量
    private static int theshold;//扩容阀值

    public void put(K key, V value) {
        int hash = Objects.hashCode(key);

        int length = DEFAULT_INITIAL_CAPACITY;

        //1.判断容器初始化
        if (table == null) {
            table = new Node[DEFAULT_INITIAL_CAPACITY];
            theshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        }

        //索引位置
        int i = hash & length - 1;

        Node<K, V> node = table[i];

        //3.判断哈希冲突
        if (node == null) {
            table[i] = new Node<>(key, value, hash, null);
        } else {
            for (int count = 0; ; count++) {
                if (node.next == null) {
                    node.next = new Node<>(key, value, hash, null);
                    break;
                }
                if ((node.hash == hash) && (key == node.key || (key != null && key.equals(node.key)))) {
                    node.setValue(value);
                    break;
                }
                node = node.next;
            }
        }
        //判断扩容
        if (++size >= theshold) {
            resize();
        }

    }


    public V get(K key) {
        int hash = Objects.hashCode(key);
        if (table == null || table.length <= 0) {
            return null;
        }

        int i = hash & (table.length - 1);
        Node<K, V> node = table[i];

        if (node == null) {
            return null;
        }
        //通过equals判断是否为节点
        if (hash == node.hash && (key == node.key || (key != null && key.equals(node.key)))) {
            return node.value;
        } else {
            for (int count = 0; ; count++) {
                if (node.next != null) {
                    if (node.next.hash == hash && (node.next.key == key || (key != null && node.next.key.equals(key)))) {
                        return node.next.value;
                    }
                }
                node = node.next;
            }
        }
    }

    public Node<K,V> remove(K key){
        int hash=Objects.hashCode(key);
        int i=hash%(table.length-1);
        Node<K,V> pre=table[i];//前一个节点
        Node<K,V> cur=pre;//当前节点

        while (cur!=null){
            Node<K,V> next=cur.next;
            //找到相应节点
            if (cur.hash==hash&&(cur.key==key||(key!=null&&cur.key.equals(key)))){
                //首个节点
                if (pre==cur){
                    table[i]=next;
                }else{
                    pre.next=next;
                }
                size--;
                return cur;
            }
            //遍历节点
            pre=cur;
            cur=next;
        }
        return null;
    }

    public void resize() {
        int newCapacity = table.length << 1;
        Node<K, V>[] newTable = new Node[newCapacity];
        theshold = (int) (newCapacity * DEFAULT_LOAD_FACTOR);
        //转移数据
        for (Node<K, V> oldNode : table) {
            if (oldNode == null) {
                continue;
            }
            for (int count = 0; ; count++) {
                if (oldNode == null) {
                    break;
                }
                Node<K, V> next = oldNode.next;
                int i = oldNode.hash & (newCapacity - 1);
                //头插法
                oldNode.next = newTable[i];
                newTable[i] = oldNode;
                oldNode = next;
            }
        }
        table = newTable;
    }


    static class Node<K, V> {
        private K key;
        private V value;
        private int hash;
        private Node<K, V> next;

        public Node(K key, V value, int hash, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public int getHash() {
            return hash;
        }

        public void setHash(int hash) {
            this.hash = hash;
        }

        public Node<K, V> getNext() {
            return next;
        }

        public void setNext(Node<K, V> next) {
            this.next = next;
        }
    }

    public static void main(String[] args) {
        MyHashMap map=new MyHashMap();
        map.put(1, 1);
        map.put(2, 2);
        System.out.println(map.get(1));
        System.out.println(map.get(2));
    }
}
