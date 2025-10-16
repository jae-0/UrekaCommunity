package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import static ui.Colors.*;

/*  [구현된 UI]
	- 여러 게시판들이 있는 게시판 목록 화면을 구성
*/
public class Boards {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Boards::createAndShow);
    }

    private static void createAndShow() {
        JFrame f = new JFrame("유레카 익명 커뮤니티 - 게시판");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(420, 650);
        f.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        f.setContentPane(root);

        // Header (시간/상태바 느낌의 간단 타이틀)
        root.add(buildHeader(), BorderLayout.NORTH);
        
        // Center list (scroll)
        root.add(buildScrollableList(), BorderLayout.CENTER);

        // Bottom Tab bar
        //root.add(buildBottomTabs(), BorderLayout.SOUTH);

        f.setVisible(true);
    }

    private static JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.setBackground(BG);

        JLabel title = new JLabel("Ureka 게시판");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        p.add(title, BorderLayout.WEST);

        return p;
    }

    private static JComponent buildScrollableList() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 12, 12, 12));

        // Quick links section
        content.add(rowFavorite("내가 쓴 글", new Color(0x4F9EF8), "📃",
        	    () -> new ActivityBoard(ActivityBoard.Mode.WRITTEN, auth.Session.currentUserId()).setVisible(true)));

        	content.add(rowFavorite("댓글 단 글", new Color(0x22C55E), "💬",
        	    () -> new ActivityBoard(ActivityBoard.Mode.COMMENTED, auth.Session.currentUserId()).setVisible(true)));


        content.add(divider());

     // Boards from DB
        try {
            var boards = dao.BoardDao.listAll();
            for (var b : boards) {
                content.add(boardRow(b.name, false,
                        () -> SwingUtilities.invokeLater(() -> new Board(b.id).setVisible(true))));
            }
        } catch (Exception e) {
            e.printStackTrace();
            content.add(simpleRow("⚠️", "게시판 목록을 불러올 수 없어요.", DANGER));
        }

        content.add(divider());

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(BG);
        sp.setBackground(BG);
        return sp;
    }

    // ----- Row builders -----
    private static Component rowFavorite(String title, Color dot, String emoji, Runnable onClick) {
        JPanel row = baseRow();
        row.add(iconDot(dot, 18), BorderLayout.WEST);

        JLabel text = new JLabel("  " + title);
        text.setForeground(TEXT_PRIMARY);
        text.setFont(text.getFont().deriveFont(Font.PLAIN, 16f));
        row.add(text, BorderLayout.CENTER);

        JLabel em = new JLabel(emoji);
        em.setForeground(TEXT_SECONDARY);
        em.setFont(em.getFont().deriveFont(16f));
        row.add(em, BorderLayout.EAST);

        row.addMouseListener(Effects.hover(row));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (onClick!=null) onClick.run(); }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return row;
    }


    private static Component boardRow(String title, boolean showNewBadge, Runnable onClick) {
        JPanel row = baseRow();
        // pin icon
        row.add(iconPin(), BorderLayout.WEST);

        JLabel text = new JLabel("  " + title);
        text.setForeground(TEXT_PRIMARY);
        text.setFont(text.getFont().deriveFont(Font.PLAIN, 16f));
        row.add(text, BorderLayout.CENTER);

        if (showNewBadge) {
            row.add(badge("N"), BorderLayout.EAST);
        }

        row.addMouseListener(Effects.hover(row)); // hover 효과 유지
        // ✅ 클릭 시 실행될 동작 주입
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onClick != null
                        && javax.swing.SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 1) {
                    onClick.run();
                }
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return row;
    }

    private static Component simpleRow(String leadingEmoji, String title, Color color) {
        JPanel row = baseRow();
        JLabel lead = new JLabel(leadingEmoji);
        lead.setForeground(TEXT_SECONDARY);
        row.add(lead, BorderLayout.WEST);

        JLabel text = new JLabel("  " + title);
        text.setForeground(color);
        text.setFont(text.getFont().deriveFont(Font.PLAIN, 16f));
        row.add(text, BorderLayout.CENTER);

        row.addMouseListener(Effects.hover(row));
        return row;
    }

    private static JPanel baseRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setBorder(new EmptyBorder(10, 8, 10, 8));
        return row;
    }

    private static Component divider() {
        JSeparator s = new JSeparator();
        s.setForeground(DIVIDER);
        s.setBackground(DIVIDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private static Component sectionHeader(String title) {
        JLabel h = new JLabel(title);
        h.setBorder(new EmptyBorder(14, 8, 6, 8));
        h.setForeground(TEXT_SECONDARY);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 14f));
        return h;
    }

    private static Component sectionSpacer() {
        return sectionHeader(" ");
    }

//    // ----- Bottom tabs -----
//
//    private static JComponent buildBottomTabs() {
//        JPanel bar = new JPanel(new GridLayout(1, 5));
//        bar.setBackground(CARD);
//        bar.setBorder(new EmptyBorder(6, 6, 10, 6));
//
//        bar.add(tab("홈", "\uD83C\uDFE0", false));      // 🏠
//        bar.add(tab("시간표", "\uD83D\uDCC5", false));   // 📅
//        bar.add(tab("게시판", "\uD83D\uDCCB", true));    // 📋 (selected)
//        bar.add(tab("채팅", "\uD83D\uDCAC", false));     // 💬
//        bar.add(tab("혜택", "\u2699\uFE0F", false));     // ⚙️
//
//        return bar;
//    }

    private static JComponent tab(String text, String emoji, boolean selected) {
        JButton b = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBackground(selected ? new Color(0x22262B) : CARD);
        b.setForeground(selected ? TEXT_PRIMARY : TEXT_SECONDARY);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // click demo
        b.addActionListener(e -> JOptionPane.showMessageDialog(
                b, text + " 탭 클릭!", "탭", JOptionPane.PLAIN_MESSAGE));
        return b;
    }

    // ----- Small helpers / visuals -----

    private static Component iconDot(Color c, int size) {
        return new JLabel(new DotIcon(c, size));
    }

    private static Component iconPin() {
        // pin composed by two dots to mimic the screenshot's pin silhouette
        return new JLabel(new PinIcon(MUTED_ICON));
    }

    private static JComponent badge(String t) {
        JLabel l = new JLabel(t);
        l.setOpaque(true);
        l.setBackground(DANGER);
        l.setForeground(Color.WHITE);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setBorder(new EmptyBorder(2, 6, 2, 6));
        l.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // make rounded
        return new RoundedLabel(l, DANGER, 12);
    }

    // ------- Custom components -------

    /** Rounded wrapper for a label (badge-like). */
    static class RoundedLabel extends JPanel {
        final JComponent child;
        final Color bg;
        final int radius;

        RoundedLabel(JComponent child, Color bg, int radius) {
            setOpaque(false);
            setLayout(new GridBagLayout());
            this.child = child;
            this.bg = bg;
            this.radius = radius;
            setBorder(new EmptyBorder(0,0,0,0));
            add(child);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, h/2 - child.getHeight()/2 - 2, w, child.getHeight()+4, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = child.getPreferredSize();
            return new Dimension(d.width + 12, d.height + 6);
        }
    }

    /** Small circular dot icon. */
    static class DotIcon implements Icon {
        final Color color;
        final int size;
        DotIcon(Color c, int size) { this.color = c; this.size = size; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /** Minimal pin icon in muted gray to match list style. */
    static class PinIcon implements Icon {
        final Color color;
        PinIcon(Color c) { this.color = c; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            // pin head
            g2.fillOval(x+2, y+4, 12, 12);
            // pin body
            g2.fillRoundRect(x+6, y+14, 4, 10, 2, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 26; }
    }

    /** Row hover effect */
    static class Effects extends java.awt.event.MouseAdapter {
        private final JComponent target;
        private Color original = BG;
        private Effects(JComponent t) { this.target = t; }
        static java.awt.event.MouseListener hover(JComponent t) { return new Effects(t); }
        @Override public void mouseEntered(java.awt.event.MouseEvent e) {
            original = target.getBackground();
            target.setBackground(new Color(0x1C2024));
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            target.repaint();
        }
        @Override public void mouseExited(java.awt.event.MouseEvent e) {
            target.setBackground(original);
            target.setCursor(Cursor.getDefaultCursor());
            target.repaint();
        }
        @Override public void mouseClicked(java.awt.event.MouseEvent e) {
      
        }
    }
}

