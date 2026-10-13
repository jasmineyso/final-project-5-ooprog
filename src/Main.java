package src;
import java.util.Scanner;

public class Main{
    Scanner sc;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Menu ui = new Menu(sc);
        ui.Menus();
    }
}