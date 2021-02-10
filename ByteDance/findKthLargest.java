import java.util.Random;

/**
 * 寻找第k大
 */
public class findKthLargest {
    Random random=new Random();

    public static void main(String[] args) {

        int[] nums={1,3,5,2,2};
        int k=3;
        findKthLargest f=new findKthLargest();
        System.out.println(f.fun(nums,k));
    }

    public int fun(int[] nums,int k){
        int length=nums.length;
        //第K大即倒数第N-K
        int index=length-k;
        int left=0;
        int right=length-1;
        return quickSelect(nums,left,right,index);
    }

    private int quickSelect(int[] nums,int left,int right,int index){
            int idx=randomPartition(nums,left,right);
            if (idx==index){
                return nums[idx];
            }else{
                return idx<index?quickSelect(nums,left+1,right,index):quickSelect(nums, left, right-1, index);
            }
    }

    //为了防止极端情况，加入随机化
    private int randomPartition(int[] nums,int left,int right){
        //[0,right-left+1)-->[left,right+1)-->[left,right]
        //从[left,right]中选出索引
        int index=random.nextInt(right-left+1)+left;
        //将 index和right位置调换
        swap(nums,index,right);
        return partition(nums,left,right);
    }

    private int partition(int[] nums,int left,int right){
        //x即为刚确定的随机数
        int x=nums[right],i=left;
        //x与[left,right]比较
        for (int j=left;j<right;j++){
            //小于x的数去到左边区间i开始
            if (nums[j]<=x){
                //注意这里是i++ 即最后的i不是小于x的
                swap(nums,i++,j);
            }
        }
        //循环结束后，小于x的数去到[left,i-1]区间
        //分割成两块 [left,i-1] 小于x [i,right] 大于x
        //将x和i调换
        swap(nums,i,right);
        //i即为中间为 左边都小于它 右边都大于它
        return i;
    }

    private void swap(int[] nums,int i,int j){
        int temp=nums[i];
        nums[i]=nums[j];
        nums[j]=temp;
    }
}
