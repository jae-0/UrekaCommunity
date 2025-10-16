package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import static ui.Colors.*;

/*  [구현된 UI]
	- 글을 올릴 수 있는 화면을 구성
*/
public class ComposePost extends JDialog {
    private final long boardId;
    private final long userId; // 로그인 사용자 ID
    private JTextField titleField;
    private JTextArea contentArea;
    private JCheckBox anonymousChk;
    private JLabel photoLabel;
    private File selectedImage;

    public ComposePost(JFrame owner, long boardId, long userId) {
        super(owner, "글쓰기", true);
        this.boardId = boardId;
        this.userId = userId;

        setSize(420, 620);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 12, 8, 12));

        JLabel back = new JLabel("\u2715"); // X
        back.setForeground(TEXT_PRIMARY);
        back.setFont(back.getFont().deriveFont(Font.BOLD, 18f));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){ dispose(); }
        });
        p.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("글쓰기", SwingConstants.CENTER);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        p.add(title, BorderLayout.CENTER);

        JButton done = new JButton("완료");
        done.setForeground(Color.WHITE);
        done.setBackground(ACCENT);
        done.setFocusPainted(false);
        done.setBorder(new EmptyBorder(8, 14, 8, 14));
        done.addActionListener(e -> onSubmit());
        p.add(done, BorderLayout.EAST);

        return p;
    }

    private JComponent buildBody() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(6, 12, 2, 12));   // 여백 축소

        // 제목
        titleField = new JTextField();
        styleField(titleField);
        applyPlaceholder(titleField, "제목");
        titleField.putClientProperty("JTextField.placeholderText", "제목을 입력해주세요.");
        titleField.setFont(titleField.getFont().deriveFont(Font.BOLD, 15f));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),   // 하단 1px
                new EmptyBorder(4, 10, 4, 10)                           // 얇게
        ));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); // 높이도 낮춤
        content.add(titleField);
        content.add(Box.createVerticalStrut(8));

        // 구분선 (간격 축소)
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        sep.setAlignmentX(LEFT_ALIGNMENT);
        //content.add(sep);
        content.add(Box.createVerticalStrut(2));

        // 본문 (기본 높이 낮춤)
        contentArea = new JTextArea();
        applyPlaceholder(contentArea, "내용");
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(new Color(0x121214));
        contentArea.setForeground(TEXT_PRIMARY);
        contentArea.setCaretColor(TEXT_PRIMARY);
        contentArea.setBorder(new EmptyBorder(6,8,4,8));
        contentArea.setRows(7);                            // ⬅️ 줄 수로 높이 축소

        JScrollPane areaScroll = new JScrollPane(
                contentArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        areaScroll.setBorder(BorderFactory.createEmptyBorder());
        areaScroll.setViewportBorder(null);
        areaScroll.setBackground(new Color(0x121214));
        content.add(areaScroll);
        content.add(Box.createVerticalStrut(8));

        // 하단 행 (사진 첨부 + 파일명 + 익명) — 슬림 레이아웃
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JButton attach = new JButton("\uD83D\uDCCE 사진");
        attach.setFocusPainted(false);
        attach.setBackground(new Color(0x1C2024));
        attach.setForeground(TEXT_PRIMARY);
        attach.setBorder(new EmptyBorder(6,10,6,10));      // ⬅️ 얇은 패딩
        attach.addActionListener(ev -> JOptionPane.showMessageDialog(this, "준비 중입니다."));
        row.add(attach);

        photoLabel = new JLabel("첨부 없음");
        photoLabel.setForeground(TEXT_SECONDARY);
        row.add(photoLabel);

        row.add(Box.createHorizontalStrut(8));
        anonymousChk = new JCheckBox("익명");
        anonymousChk.setOpaque(false);
        anonymousChk.setForeground(TEXT_PRIMARY);
        anonymousChk.setSelected(true);
        row.add(anonymousChk);

        content.add(row);

        JScrollPane sp = new JScrollPane(
                content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG);
        sp.setBackground(BG);
        sp.setViewportBorder(null);


        return sp;
    }

    private JComponent buildBottomBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(2, 12, 8, 12));

        JLabel hint = new JLabel("\u26A0\uFE0F 사진과 익명만 지원 (가벼운 MVP)");
        hint.setForeground(TEXT_SECONDARY);
        p.add(hint, BorderLayout.WEST);

        return p;
    }

    private void styleField(JTextField tf) {
        tf.setBackground(new Color(0x121214));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(new EmptyBorder(10,12,10,12));
    }

    private void onPickImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("이미지 선택");
        fc.setFileFilter(new FileNameExtensionFilter("이미지 파일", "png","jpg","jpeg","gif","webp"));
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            selectedImage = fc.getSelectedFile();
            photoLabel.setText(selectedImage.getName());
            photoLabel.setForeground(TEXT_PRIMARY);
        }
    }

    private void onSubmit() {
        String title = readText(titleField).trim();   // ← 앞서 만든 readText 사용 중이면 그대로
        String body  = readText(contentArea).trim();
        boolean isAnon = anonymousChk.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            long postId = dao.PostDao.insertPost((int) boardId, userId, title, body, isAnon);

            JOptionPane.showMessageDialog(this, "등록되었습니다!");
            dispose();

            // 목록 새로고침 (FreeBoard에 public 메서드가 있을 때)
            if (getOwner() instanceof Board fb) {
                fb.refreshFromTop();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "등록 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
 // 플레이스홀더 부착
    private void applyPlaceholder(JTextComponent comp, String hint) {
        comp.putClientProperty("placeholder", hint);
        comp.setForeground(TEXT_SECONDARY);
        comp.setText(hint);

        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                String h = (String) comp.getClientProperty("placeholder");
                if (h != null && h.equals(comp.getText())) {
                    comp.setText("");
                    comp.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                String h = (String) comp.getClientProperty("placeholder");
                if (h != null && comp.getText().trim().isEmpty()) {
                    comp.setForeground(TEXT_SECONDARY);
                    comp.setText(h);
                }
            }
        });
    }

    // 플레이스홀더인지 구분해서 실제 값 읽기
    private String readText(JTextComponent comp) {
        String h = (String) comp.getClientProperty("placeholder");
        String t = comp.getText();
        return (h != null && h.equals(t)) ? "" : t;
    }
}
