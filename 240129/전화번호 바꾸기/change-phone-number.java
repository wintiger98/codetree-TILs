import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String phone = sc.nextLine();
        String[] nums = phone.split("-");
        String tmp = nums[2];
        nums[2] = nums[1];
        nums[1] = tmp;
        System.out.println(nums[0] + "-" + nums[1] + "-" + nums[2]);
    }
}