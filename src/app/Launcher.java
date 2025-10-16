package app;

import javax.swing.SwingUtilities;
import ui.Login;

/*  [구현 설명]
	- 프로그램 시작
*/

public class Launcher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}