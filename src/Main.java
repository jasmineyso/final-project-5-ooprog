package src;

import src.menu.Menu;
import src.util.Input;

public class Main {
    public static void main(String[] args) {
        try {
            Menu ui = new Menu();
            ui.Menus();
        } finally {
            Input.getScanner().close();
        }
    }
}
