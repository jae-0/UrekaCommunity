package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.UserDao;
import ui.Scrollbars.DarkScrollBarUI;
import static ui.Colors.*;

/*  [구현된 UI]
	- 회원가입을 할 수 있는 화면을 구성
*/

public class Signup extends JFrame {

    private JTextField emailField, nameField, birthField, phoneField, inviteField;
    private JPasswordField pwField, pwCheckField;
    private JCheckBox showPw;
    private JRadioButton maleBtn, femaleBtn, otherBtn;
    private JLabel errorLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Signup().setVisible(true));
    }

    public Signup() {
        super("유레카 익명 커뮤니티 - 회원가입");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(460, 820);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildBottom(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(28, 0, 8, 0));
        JLabel logo = new JLabel(new Login().loadLogoIcon(150));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        p.add(logo);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private JComponent buildForm() {
    	JPanel card = new Login.RoundedPanel(16, CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setAlignmentX(LEFT_ALIGNMENT);
        
        JScrollPane scroll = new JScrollPane(card,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Scrollbars.applyDark(scroll, /*thickness*/ 15, /*unitInc*/ 16);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0)); // 얇게
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(BG);
        box.setBorder(new EmptyBorder(8, 24, 24, 24));
        box.add(scroll, BorderLayout.CENTER);

       
        KeyAdapter enterToSubmit = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSignup();
            }
        };

        // 이메일
        card.add(label("이메일"));
        emailField = inputField("you@example.com");
        emailField.addKeyListener(enterToSubmit);
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));

        // 비밀번호
        card.add(label("비밀번호 (8자 이상, 영문+숫자)"));
        pwField = passwordField();
        pwField.addKeyListener(enterToSubmit);
        card.add(pwField);
        card.add(Box.createVerticalStrut(10));

        // 비밀번호 확인
        card.add(label("비밀번호 확인"));
        pwCheckField = passwordField();
        pwCheckField.addKeyListener(enterToSubmit);
        card.add(pwCheckField);

        // 비밀번호 표시
        JPanel pwRow = row();
        showPw = new JCheckBox("비밀번호 표시");
        showPw.setOpaque(false);
        showPw.setForeground(TEXT_SECONDARY);
        showPw.addActionListener(e -> {
            char echo = showPw.isSelected() ? 0 : '\u2022';
            pwField.setEchoChar(echo);
            pwCheckField.setEchoChar(echo);
        });
        pwRow.add(showPw, BorderLayout.WEST);
        card.add(Box.createVerticalStrut(6));
        card.add(pwRow);
        card.add(Box.createVerticalStrut(14));

        // 이름
        card.add(label("이름"));
        nameField = inputField("홍길동");
        nameField.addKeyListener(enterToSubmit);
        card.add(nameField);
        card.add(Box.createVerticalStrut(14));

        // 생년월일
        card.add(label("생년월일 (YYYY-MM-DD)"));
        birthField = inputField("1999-12-31");
        birthField.addKeyListener(enterToSubmit);
        card.add(birthField);
        card.add(Box.createVerticalStrut(14));

        // 성별
        card.add(label("성별"));
        JPanel genderRow = row();
        maleBtn = radio("남"); femaleBtn = radio("여");
        ButtonGroup g = new ButtonGroup();
        g.add(maleBtn); g.add(femaleBtn);
        maleBtn.setSelected(true);
        genderRow.add(maleBtn); genderRow.add(Box.createHorizontalStrut(10));
        genderRow.add(femaleBtn); genderRow.add(Box.createHorizontalStrut(10));
        card.add(genderRow);
        card.add(Box.createVerticalStrut(14));

        // 전화번호
        card.add(label("전화번호 (예: 010-1234-5678)"));
        phoneField = inputField("010-1234-5678");
        phoneField.addKeyListener(enterToSubmit);
        card.add(phoneField);
        card.add(Box.createVerticalStrut(14));

        // 초대코드
        card.add(label("초대코드"));
        inviteField = inputField("예: LGU123");
        inviteField.addKeyListener(enterToSubmit);
        card.add(inviteField);
        card.add(Box.createVerticalStrut(12));

        // 에러
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(0xFF6B6B));
        errorLabel.setFont(errorLabel.getFont().deriveFont(12f));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));

        // 가입 버튼
        JButton signupBtn = primaryButton("회원가입");
        signupBtn.addActionListener(e -> onSignup());
        signupBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(signupBtn);
      
        return box;
    }

    private JComponent buildBottom() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 24, 24, 24));

        JButton toLogin = ghostButton("이미 계정이 있나요? 로그인");
        toLogin.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new Login().setVisible(true));
        });
        p.add(toLogin, BorderLayout.WEST);
        return p;
    }

	    // ========== Actions ==========
	    private void onSignup() {
	        String email = emailField.getText().trim();
	        String pw = new String(pwField.getPassword());
	        String pw2 = new String(pwCheckField.getPassword());
	        String name = nameField.getText().trim();
	        String birth = birthField.getText().trim();
	        String phone = normalizePhone(phoneField.getText().trim());
	        String gender = maleBtn.isSelected() ? "M" : (femaleBtn.isSelected() ? "F" : (otherBtn.isSelected() ? "O" : ""));
	        String invite = inviteField.getText().trim();
	
	        // --- Validation ---
	        if (email.isEmpty() || pw.isEmpty() || pw2.isEmpty() || name.isEmpty()
	                || birth.isEmpty() || gender.isEmpty() || phone.isEmpty() || invite.isEmpty()) {
	            showError("모든 항목을 입력해주세요.");
	            return;
	        }
	        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
	            showError("올바른 이메일 형식이 아닙니다.");
	            return;
	        }
	        if (!birth.matches("^\\d{4}-\\d{2}-\\d{2}$") || !isValidDate(birth)) {
	            showError("생년월일 형식이 올바르지 않습니다. (예: 1999-12-31)");
	            return;
	        }
	        if (!phone.matches("^01[016789]-\\d{3,4}-\\d{4}$")) {
	            showError("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
	            return;
	        }
	        if (!invite.matches("^[A-Za-z0-9]{5,10}$")) {
	            showError("초대코드는 5~10자 영문/숫자입니다.");
	            return;
	        }
	        
	        if (!(invite.equalsIgnoreCase("EUREKA") || invite.equalsIgnoreCase("LGU123"))) {
	            showError("유효하지 않은 초대코드입니다.");
	            return;
	        }
	
	        try {
	            // 이메일 중복 선체크
	            if (UserDao.emailExists(email)) {
	                showError("이미 가입된 이메일입니다.");
	                return;
	            }

	            // DB 인서트
	            long newUserId = UserDao.insert(
	                    /* nickname */ name,
	                    /* email    */ email,
	                    /* pw_hash  */ pw,
	                    /* role     */ "USER"
	            );

	            // 성공 처리
	            errorLabel.setText(" ");
	            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다. 이제 로그인 해주세요.");
	            dispose();
	            SwingUtilities.invokeLater(() -> new Login().setVisible(true));

	        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
	            // UNIQUE(email) 충돌 대비
	            showError("이미 가입된 이메일입니다.");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            showError("회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
	        }
	    }

    private boolean isValidDate(String ymd) {
        try {
            LocalDate.parse(ymd, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) { return false; }
    }

    private String normalizePhone(String s) {
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.startsWith("010"))
            return String.format("010-%s-%s", digits.substring(3,7), digits.substring(7));
        if (digits.length() == 10 && digits.startsWith("011"))
            return String.format("011-%s-%s", digits.substring(3,6), digits.substring(6));
        // 그대로 반환 (사용자가 이미 하이픈 포함 입력했을 수 있음)
        return s;
    }

    // ========== UI Helpers ==========
    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(0,0,6,0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel row() {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        r.setOpaque(false);
        r.setAlignmentX(LEFT_ALIGNMENT);
        return r;
    }

    private JRadioButton radio(String text) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setForeground(TEXT_PRIMARY);
        r.setAlignmentX(LEFT_ALIGNMENT);
        return r;
    }

    private JTextField inputField(String placeholder) {
        JTextField tf = new JTextField();
        styleField(tf);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    private JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        styleField(pf);
        pf.setEchoChar('\u2022');
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
        tf.setAlignmentX(LEFT_ALIGNMENT);
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
        b.setAlignmentX(LEFT_ALIGNMENT);
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

    private void showError(String msg) { errorLabel.setText(msg); }
    
    
}
