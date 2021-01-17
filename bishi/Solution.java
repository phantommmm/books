//import java.util.*;
//public class Main {
//    static char[][] _board = null;
//    static ArrayList<String> _result = new ArrayList<String>();
//
//    public static void main(String[] args) {
//        Scanner in = new Scanner(System.in);
//        int N=0,M=0;
//        N=in.nextInt();
//        String[] strings=new String[N];
//        for(int i=0;i<N;i++){
//            strings[i]=in.next();
//        }
//        M=in.nextInt();
//        String[] words=new String[M];
//        for(int i=0;i<M;i++){
//            words[i]=in.next();
//        }
//        char[][] board=new char[N][];
//        for(int i=0;i<N;i++){
//            board[i]=strings[i].toCharArray();
//        }
//        Main solution=new Main();
//        List<String> list=solution.findWords(board,words);
//        if(list.isEmpty()){
//            System.out.println("");
//        }else{
//            String[] s=stringSort(list.toArray(new String[0]));
//            for (int i = 0; i < s.length; i++) {
//                System.out.println(s[i]);
//            }
//        }
//    }
//
//    public static String[] stringSort(String[] s) {
//        List<String> list = new ArrayList<String>(s.length);
//        for (int i = 0; i < s.length; i++) {
//            list.add(s[i]);
//        }
//        Collections.sort(list);
//        return list.toArray(s);
//    }
//
//
//    public List<String> findWords(char[][] board, String[] words) {
//
//        // Step 1). Construct the Trie
//        TrieNode root = new TrieNode();
//        for (String word : words) {
//            TrieNode node = root;
//
//            for (Character letter : word.toCharArray()) {
//                if (node.children.containsKey(letter)) {
//                    node = node.children.get(letter);
//                } else {
//                    TrieNode newNode = new TrieNode();
//                    node.children.put(letter, newNode);
//                    node = newNode;
//                }
//            }
//            node.word = word;  // store words in Trie
//        }
//
//        this._board = board;
//        // Step 2). Backtracking starting for each cell in the board
//        for (int row = 0; row < board.length; ++row) {
//            for (int col = 0; col < board[row].length; ++col) {
//                if (root.children.containsKey(board[row][col])) {
//                    backtracking(row, col, root);
//                }
//            }
//        }
//
//        return this._result;
//    }
//
//    private void backtracking(int row, int col, TrieNode parent) {
//        Character letter = this._board[row][col];
//        TrieNode currNode = parent.children.get(letter);
//
//        // check if there is any match
//        if (currNode.word != null) {
//            this._result.add(currNode.word);
//            currNode.word = null;
//        }
//
//        // mark the current letter before the EXPLORATION
//        this._board[row][col] = '#';
//
//        // explore neighbor cells in around-clock directions: up, right, down, left
//        int[] rowOffset = {-1, 0, 1, 0};
//        int[] colOffset = {0, 1, 0, -1};
//        for (int i = 0; i < 4; ++i) {
//            int newRow = row + rowOffset[i];
//            int newCol = col + colOffset[i];
//            if (newRow < 0 || newRow >= this._board.length || newCol < 0
//                    || newCol >= this._board[0].length) {
//                continue;
//            }
//            if (currNode.children.containsKey(this._board[newRow][newCol])) {
//                backtracking(newRow, newCol, currNode);
//            }
//        }
//
//        // End of EXPLORATION, restore the original letter in the board.
//        this._board[row][col] = letter;
//
//        // Optimization: incrementally remove the leaf nodes
//        if (currNode.children.isEmpty()) {
//            parent.children.remove(letter);
//        }
//    }
//}
//
//class TrieNode {
//    HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>();
//    String word = null;
//    public TrieNode() {}
//}