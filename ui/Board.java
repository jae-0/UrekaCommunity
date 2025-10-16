package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import dao.CommentDao;
import static ui.Colors.*;

/*  [구현된 UI]
	- 여러 게시글들이 있는 게시판 화면을 구성
*/
public class Board extends JFrame {

    // ---- State ----
    private JPanel list;
    private JScrollPane scroll;
    private int page = 0;
    private final int pageSize = 20;
    private final int boardId;
    private final String boardName;
    private boolean loading = false;
    private boolean reachedEnd = false; // 더 가져올 게 없는 상태
    private boolean endlessEnabled = false;
    
    // 검색 상태
    private boolean searchMode = false;
    private java.util.List<String> searchTokens = java.util.List.of();
    private JLabel searchBtn; // 헤더 우측 아이콘

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int demoBoardId = 1;
            try {
                String name = dao.BoardDao.findNameById(demoBoardId);
                if (name == null) name = "게시판";
                new Board(demoBoardId, name).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                new Board(demoBoardId, "게시판").setVisible(true);
            }
        });
    }

    public Board(int boardId, String boardName) {
        super("유레카 - " + (boardName == null ? "게시판" : boardName));
        this.boardId = boardId;
        this.boardName = (boardName == null ? "게시판" : boardName);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 760);
        setLocationRelativeTo(null);
        setResizable(false);

        JLayeredPane layers = new JLayeredPane();
        layers.setLayout(new BorderLayout());
        setContentPane(layers);

        layers.add(buildHeader(), BorderLayout.NORTH);
        layers.add(buildListArea(), BorderLayout.CENTER);
        layers.add(buildComposeBar(), BorderLayout.SOUTH);

        loadMore();
    }

    public Board(int boardId) {
        this(boardId, safeName(boardId));
    }

    private static String safeName(int boardId) {
        try {
            String n = dao.BoardDao.findNameById(boardId);
            return n == null ? "게시판" : n;
        } catch (Exception e) {
            return "게시판";
        }
    }

    // ---------- Header ----------
    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 12, 10, 12));

        JLabel back = new JLabel("\u2190");
        back.setForeground(TEXT_PRIMARY);
        back.setFont(back.getFont().deriveFont(Font.BOLD, 18f));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new Hover(back));
        back.addMouseListener(new ClickSimple(() -> {
            dispose();
            SwingUtilities.invokeLater(() -> Boards.main(new String[]{}));
        }));
        p.add(back, BorderLayout.WEST);

        JLabel title = new JLabel(boardName, SwingConstants.CENTER);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        p.add(title, BorderLayout.CENTER);

        searchBtn = new JLabel("\uD83D\uDD0D");
        searchBtn.setForeground(TEXT_PRIMARY);
        searchBtn.setFont(searchBtn.getFont().deriveFont(18f));
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.addMouseListener(new Hover(searchBtn));
        searchBtn.addMouseListener(new ClickSimple(() -> {
            if (searchMode) {
                clearSearch();
            } else {
                String q = JOptionPane.showInputDialog(this, "검색어(두 글자 이상)", "검색", JOptionPane.PLAIN_MESSAGE);
                if (q == null) return;
                startSearch(q);
            }
        }));
        p.add(searchBtn, BorderLayout.EAST);

        return p;
    }

    // ---------- Center: List ----------
    private JComponent buildListArea() {
        list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG);
        list.setBorder(new EmptyBorder(0, 0, 80, 0)); // 하단 버튼 공간

        scroll = new JScrollPane(list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Scrollbars.applyDark(scroll, 15, 16);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().addAdjustmentListener(new Endless());
        return scroll;
    }

    // ---------- Bottom: Compose ----------
    private JComponent buildComposeBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x101214));
        bar.setBorder(new EmptyBorder(10, 12, 16, 12));

        JButton compose = new JButton("\u270E  글쓰기");
        compose.setForeground(Color.WHITE);
        compose.setBackground(ACCENT);
        compose.setFocusPainted(false);
        compose.setBorder(new EmptyBorder(12, 18, 12, 18));
        compose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        compose.setFont(compose.getFont().deriveFont(Font.BOLD, 14f));

        compose.addActionListener(e ->
            new ComposePost(this, boardId, auth.Session.currentUserId()).setVisible(true)
        );

        compose.setContentAreaFilled(true);
        compose.setOpaque(true);
        compose.putClientProperty("JButton.buttonType", "roundRect");

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.add(compose);
        bar.add(center, BorderLayout.CENTER);

        return bar;
    }

    // ---------- Post Row ----------
    private Component postRow(Post p) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel(p.title);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        row.add(title, BorderLayout.NORTH);

        String preview = p.content.length() > 60 ? p.content.substring(0, 60) + "…" : p.content;
        JLabel sub = new JLabel("<html><div style='width:320px;color:#B3B6BB;'>" + escape(preview) + "</div></html>");
        sub.setForeground(TEXT_SECONDARY);
        sub.setFont(sub.getFont().deriveFont(13f));
        row.add(sub, BorderLayout.CENTER);

        String time = DateTimeFormatter.ofPattern("H:mm").format(p.createdAt);
        String meta = String.format("\uD83D\uDCAC %d  |  %s  |  %s",
                p.commentCount, time, p.isAnonymous ? "익명" : p.nickname);
        JLabel metaL = new JLabel(meta);
        metaL.setForeground(TEXT_SECONDARY);
        metaL.setFont(metaL.getFont().deriveFont(12f));
        row.add(metaL, BorderLayout.SOUTH);

        row.addMouseListener(new Hover(row));
        row.addMouseListener(new ClickSimple(() -> {
            PostDetail.Post pDetail = new PostDetail.Post(
                p.id, p.authorId, p.title, p.content, p.isAnonymous, p.nickname, p.createdAt
            );
            try {
                List<dto.CommentDto> raw = CommentDao.findByPostId((int) p.id);
                List<PostDetail.Comment> comments = new java.util.ArrayList<>();
                for (dto.CommentDto c : raw) {
                    comments.add(new PostDetail.Comment(
                        c.getId(), c.getPostId(), c.getUserId(), c.isAnonymous(),
                        c.getContent(), c.getCreatedAt(),
                        c.getParentId() == null ? null : c.getParentId().longValue(), c.getUserNickname(), c.getAliasNo()
                    ));
                }
                new PostDetail(boardId, boardName, auth.Session.currentUserId(), pDetail, comments, this::refreshFromTop).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "댓글 로드 실패: " + ex.getMessage());
            }
        }));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(row, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    public void refreshFromTop() {
        list.removeAll();
        page = 0;
        reachedEnd = false;
        loadMore();
    }

    // ---------- Data loading ----------
    private void loadMore() {
    	if (loading || reachedEnd) return;
        loading = true;
        try {
            var rows = searchMode
                ? dao.PostDao.searchByBoardTokens(boardId, searchTokens, pageSize, page * pageSize)
                : dao.PostDao.listByBoard(boardId, pageSize, page * pageSize);

            if (page == 0 && rows.isEmpty()) {
                // 첫 페이지부터 0건이면 안내 표시
                JLabel empty = new JLabel(searchMode ? "검색 결과가 없습니다." : "게시글이 없습니다.");
                empty.setForeground(TEXT_SECONDARY);
                empty.setBorder(new EmptyBorder(24, 16, 24, 16));
                list.add(empty);
            } else {
                for (var r : rows) {
                    Post p = new Post(
                        r.id, r.authorId, r.title, r.content,
                        r.isAnonymous, r.nickname, r.createdAt, r.commentCount
                    );
                    list.add(postRow(p));
                }
            }
            list.revalidate(); list.repaint();
            if (!rows.isEmpty()) page++;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "로딩 실패: " + ex.getMessage());
        } finally {
            loading = false;
        }
    }

    // ---------- Search helpers ----------
    private void startSearch(String q) {
        var tokens = tokenize(q);
        if (tokens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "두 글자 이상 단어를 입력하세요.");
            return;
        }
        searchMode = true;
        searchTokens = tokens;
        updateSearchIcon();
        list.removeAll();
        page = 0;
        reachedEnd = false;   // ← 추가
        loadMore();
    }

    private void clearSearch() {
        searchMode = false;
        searchTokens = java.util.List.of();
        updateSearchIcon();
        list.removeAll();
        page = 0;
        reachedEnd = false;   // ← 추가
        loadMore();
    }

    private void updateSearchIcon() {
        searchBtn.setText(searchMode ? "\u2715" : "\uD83D\uDD0D");
        searchBtn.setToolTipText(searchMode ? "검색 해제" : "검색");
    }

    private static java.util.List<String> tokenize(String q) {
        var list = new java.util.ArrayList<String>();
        if (q == null) return list;
        for (String w : q.trim().split("\\s+")) {
            if (w.length() >= 2) list.add(w);
        }
        return list;
    }

    // ---------- Misc ----------
    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private class Endless implements AdjustmentListener {
        @Override public void adjustmentValueChanged(AdjustmentEvent e) {
            if (!endlessEnabled) return;
            if (loading || reachedEnd) return;

            JScrollBar b = (JScrollBar) e.getAdjustable();
            int max = b.getMaximum() - b.getVisibleAmount();
            if (max <= 0) return;

            int value = b.getValue();
            if (max - value < 60) loadMore();
        }
    }


    private static class Hover extends java.awt.event.MouseAdapter {
        private final JComponent target;
        private Color original;
        Hover(JComponent t) { target = t; }
        @Override public void mouseEntered(java.awt.event.MouseEvent e) {
            original = target.getBackground();
            target.setBackground(new Color(0x1C2024));
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public void mouseExited(java.awt.event.MouseEvent e) {
            target.setBackground(original);
            target.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static class ClickSimple extends java.awt.event.MouseAdapter {
        private final Runnable r;
        ClickSimple(Runnable r){ this.r = r; }
        @Override public void mouseClicked(java.awt.event.MouseEvent e){ r.run(); }
    }

    static class Post {
        final long id;
        final long authorId;
        final String title;
        final String content;
        final boolean isAnonymous;
        final String nickname;
        final LocalDateTime createdAt;
        final int commentCount;

        Post(long id, long authorId, String title, String content,
             boolean anon, String nickname, LocalDateTime createdAt, int comments) {
            this.id = id;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.isAnonymous = anon;
            this.nickname = nickname == null ? "" : nickname;
            this.createdAt = createdAt;
            this.commentCount = comments;
        }
    }
}
