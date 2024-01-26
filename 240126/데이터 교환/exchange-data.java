public class Main {
    public static void main(String[] args) {
        int a = 5;
        int b = 6;
        int c = 7;
        
        int tmp_a = a;
        int tmp_b = b;
        int tmp_c = c;
        a = tmp_c;
        b = tmp_a;
        c = tmp_b;
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }
}