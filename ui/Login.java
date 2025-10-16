package ui;

import static ui.Colors.ACCENT;
import static ui.Colors.BG;
import static ui.Colors.CARD;
import static ui.Colors.DIVIDER;
import static ui.Colors.TEXT_PRIMARY;
import static ui.Colors.TEXT_SECONDARY;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.UserDao;
import dto.LoginDto;

/*  [구현된 UI]
	- 로그인 화면을 구성
*/
public class Login extends JFrame {
	
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPw;
    private JLabel errorLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }

    public Login() {
        super("유레카 익명 커뮤니티 - 로그인");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 720);
        setResizable(false); 
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildBottomLinks(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        // 상단에 여백만 주고, 로고를 수평 정중앙으로
        JPanel p = new JPanel();
        p.setOpaque(false);                           // 배경(그라데이션/블랙) 비치게
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(36, 0, 12, 0));   // 위쪽 여백은 이미지처럼 넉넉히

        JLabel logo = new JLabel(loadLogoIcon(220));  // 원하는 너비(px)로 조절
        logo.setAlignmentX(CENTER_ALIGNMENT);         // 가로 중앙 정렬
        p.add(logo);

        // 필요하면 아래로 살짝 여백
        p.add(Box.createVerticalStrut(8));
        return p;
    }
    
    public ImageIcon loadLogoIcon(int targetWidth) {
        try {
            URL url = getClass().getResource("/img/logo.png");
            if (url == null) throw new IllegalArgumentException("logo not found");
            Image img = new ImageIcon(url).getImage();
            int w = img.getWidth(null), h = img.getHeight(null);
            int targetHeight = (int) Math.round(h * (targetWidth / (double) w));
            Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            // 로고가 없으면 텍스트로 대체(깨짐 방지)
            JLabel fallback = new JLabel("EUREKA");
            fallback.setForeground(TEXT_PRIMARY);
            fallback.setFont(fallback.getFont().deriveFont(28f));
            BufferedImage bi = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB); // dummy
            return new ImageIcon(bi); // 상징적으로 반환 (이미지 준비되면 자동 교체)
        }
    }

    private JComponent buildForm() {
        JPanel box = new JPanel();
        box.setBackground(BG);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(8, 24, 24, 24));

        // 카드
        JPanel card = new RoundedPanel(16, CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        alignLeft(card);

        // 이메일
        card.add(label("이메일"));
        emailField = inputField("you@example.com");
        // 가로폭 꽉 채우기 + 높이 고정(선택)
        emailField.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        alignLeft(emailField);
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));

        // 비밀번호
        card.add(label("비밀번호"));
        passwordField = passwordField();
        passwordField.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        alignLeft(passwordField);
        KeyAdapter enterToLogin = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin(null);
            }
        };
        emailField.addKeyListener(enterToLogin);
        passwordField.addKeyListener(enterToLogin);
        card.add(passwordField);

        // 옵션/표시 (체크박스 줄도 왼쪽 정렬)
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        alignLeft(row);
        showPw = new JCheckBox("비밀번호 표시");
        showPw.setOpaque(false);
        showPw.setForeground(TEXT_SECONDARY);
        showPw.addActionListener(e -> passwordField.setEchoChar(showPw.isSelected() ? (char)0 : '\u2022'));
        row.add(showPw, BorderLayout.WEST);
        card.add(Box.createVerticalStrut(8));
        card.add(row);
        card.add(Box.createVerticalStrut(8));

        // 오류 메시지
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(0xFF6B6B));
        errorLabel.setFont(errorLabel.getFont().deriveFont(12f));
        alignLeft(errorLabel);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));

        // 로그인 버튼 (가로 가운데가 좋다면 생략 가능)
        JButton loginBtn = primaryButton("로그인");
        loginBtn.addActionListener(this::onLogin);
        loginBtn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44)); // 선택
        alignLeft(loginBtn);
        card.add(loginBtn);

        // 구분선
        card.add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);
        alignLeft(sep);
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        // 하단 액션
        JPanel actions = new JPanel(new GridLayout(1,1,10,0));
        actions.setOpaque(false);
        alignLeft(actions);
        JButton signupBtn = ghostButton("회원가입");
        signupBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new Signup().setVisible(true));
        });
        actions.add(signupBtn);
        card.add(actions);

        box.add(card);
        return box;
    }
    
    private void alignLeft(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    private JComponent buildBottomLinks() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 24, 24, 24));

        JLabel hint = new JLabel("초대코드로 회원가입 후 로그인할 수 있습니다.");
        hint.setForeground(TEXT_SECONDARY);
        p.add(hint, BorderLayout.WEST);
        return p;
    }

    // ---- Widgets ----
    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(0,0,6,0));
        l.setAlignmentX(LEFT_ALIGNMENT);  // ← 추가
        return l;
    }

    private JTextField inputField(String placeholder) {
        JTextField tf = new JTextField();
        styleField(tf);
        tf.putClientProperty("JTextField.placeholderText", placeholder); // 일부 LAF에서 동작
        return tf;
    }

    private JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        styleField(pf);
        pf.setEchoChar('\u2022');
        return pf;
    }

    private void styleField(JTextField tf) {
        tf.setBackground(new Color(0x121214));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(new CompoundBorder(new LineBorder(DIVIDER, 1, true),
                new EmptyBorder(10,12,10,12)));
        tf.setSelectedTextColor(TEXT_PRIMARY);
        tf.setSelectionColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 90));
    }

    private JButton primaryButton(String t) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, ACCENT, getWidth(), getHeight(), ACCENT.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 12,12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12,0,12,0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostButton(String t) {
        JButton b = new JButton(t);
        b.setForeground(TEXT_SECONDARY);
        b.setBackground(new Color(0x141414));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(DIVIDER, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        return b;
    }

    // ---- Actions ----
    private void onLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String pw = new String(passwordField.getPassword());

        // 기본 검증 (도메인 제한 제거)
        if (email.isEmpty() || pw.isEmpty()) {
            showError("이메일과 비밀번호를 입력하세요.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("올바른 이메일 형식이 아닙니다.");
            return;
        }

        try {
            LoginDto dto = UserDao.findByEmail(email);
            if (dto == null) {
                showError("이메일 또는 비밀번호가 올바르지 않습니다.");
                return;
            }

            if (!pw.equals(dto.getPw())) {  // 대/소문자 구분하여 그대로 비교
                showError("이메일 또는 비밀번호가 올바르지 않습니다.");
                return;
            }
            
            auth.Session.setUser(dto);

            errorLabel.setText(" ");
            JOptionPane.showMessageDialog(this, "로그인 성공! " + dto.getNickname() + "님 안녕하세요.");
            dispose();
            SwingUtilities.invokeLater(() -> Boards.main(new String[]{}));

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    // ---- Rounded panel ----
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
