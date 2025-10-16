package ui;

import dao.MyActivityDao;
import dao.CommentDao;
import static ui.Colors.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*  [구현된 UI]
	- 내가 쓴 글과 댓글을 모아볼 수 있는 화면을 구성
*/

public class ActivityBoard extends JFrame {
	// 내가 쓴 글, 댓글 구분
    public enum Mode { WRITTEN, COMMENTED }
    private final Mode mode;
    private final int userId;

    // state
    private JPanel list;
    private JScrollPane scroll;
    private int page = 0;
    private final int pageSize = 20;
    private boolean loading = false;
    private boolean reachedEnd = false;
    
    
    public ActivityBoard(Mode mode, int userId) {
        super("유레카 - " + (mode == Mode.WRITTEN ? "내가 쓴 글" : "댓글 단 글"));
        this.mode = mode;
        this.userId = userId;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(420, 760);
        setLocationRelativeTo(null);
        setResizable(false);

        var root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildListArea(), BorderLayout.CENTER);

        loadMore();
    }
    
    // 뒤로가기, 타이틀
    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 12, 10, 12));

        JLabel back = new JLabel("\u2190");
        back.setForeground(TEXT_PRIMARY);
        back.setFont(back.getFont().deriveFont(Font.BOLD, 18f));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){ dispose(); }
        });
        p.add(back, BorderLayout.WEST);

        JLabel title = new JLabel(mode == Mode.WRITTEN ? "내가 쓴 글" : "댓글 단 글", SwingConstants.CENTER);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        p.add(title, BorderLayout.CENTER);

        p.add(Box.createHorizontalStrut(24), BorderLayout.EAST);
        return p;
    }
    
    // 스크롤
    private JComponent buildListArea() {
        list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG);
        list.setBorder(new EmptyBorder(0, 0, 12, 0));

        scroll = new JScrollPane(list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Scrollbars.applyDark(scroll, 15, 16);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        scroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (loading || reachedEnd) return;
            JScrollBar b = (JScrollBar) e.getAdjustable();
            int max = b.getMaximum() - b.getVisibleAmount();
            if (max > 0 && max - b.getValue() < 60) loadMore();
        });
        return scroll;
    }
    
    // page load
    private void loadMore() {
        if (loading || reachedEnd) return;
        loading = true;
        try {
        	// Board Type에 맞게 설정
            List<MyActivityDao.Row> rows = (mode == Mode.WRITTEN)
                    ? MyActivityDao.listWrittenByUser(userId, pageSize, page * pageSize) // 내가 쓴 글
                    : MyActivityDao.listCommentedByUser(userId, pageSize, page * pageSize); // 댓글 단 글
            
            // 페이지가 0이면서 받아온 rows가 없을 시에
            if (page == 0 && rows.isEmpty()) {
                JLabel empty = new JLabel(mode == Mode.WRITTEN ? "작성한 게시글이 없습니다." : "댓글을 단 게시글이 없습니다.");
                empty.setForeground(TEXT_SECONDARY);
                empty.setBorder(new EmptyBorder(24, 16, 24, 16));
                list.add(empty);
            } else {
                for (var r : rows) list.add(postRow(r));
                if (rows.size() < pageSize) reachedEnd = true;
                page++;
            }
            list.revalidate(); list.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "로딩 실패: " + e.getMessage());
        } finally {
            loading = false;
        }
    }
    
    // 게시글 미리보기 UI
    private JComponent postRow(MyActivityDao.Row r) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel(r.title);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        row.add(title, BorderLayout.NORTH);

        String preview = r.content.length() > 60 ? r.content.substring(0, 60) + "…" : r.content;
        JLabel sub = new JLabel("<html><div style='width:320px;color:#B3B6BB;'>"
                + escape(preview) + "</div></html>");
        sub.setForeground(TEXT_SECONDARY);
        sub.setFont(sub.getFont().deriveFont(13f));
        row.add(sub, BorderLayout.CENTER);

        String time = DateTimeFormatter.ofPattern("H:mm").format(r.createdAt);
        String meta = String.format("\uD83D\uDCAC %d  |  %s  |  %s",
                r.commentCount, time, r.isAnonymous ? "익명" : (r.nickname == null ? "" : r.nickname));
        JLabel metaL = new JLabel(meta);
        metaL.setForeground(TEXT_SECONDARY);
        metaL.setFont(metaL.getFont().deriveFont(12f));
        row.add(metaL, BorderLayout.SOUTH);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(0x1C2024));
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(BG);
                row.setCursor(Cursor.getDefaultCursor());
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
            	// postDTO 생성
                var pDetail = new PostDetail.Post(
                        r.id, r.authorId, r.title, r.content, r.isAnonymous,
                        r.nickname, r.createdAt
                );
                try {
                    var raw = CommentDao.findByPostId((int) r.id);
                    var cmts = new java.util.ArrayList<PostDetail.Comment>();
                    for (var c : raw) {
                        cmts.add(new PostDetail.Comment(
                                c.getId(), c.getPostId(), c.getUserId(), c.isAnonymous(),
                                c.getContent(), c.getCreatedAt(),
                                c.getParentId() == null ? null : c.getParentId().longValue(), c.getUserNickname(), c.getAliasNo()
                        ));
                    }
                    new PostDetail(r.boardId, r.boardName, auth.Session.currentUserId(),
                            pDetail, cmts, ActivityBoard.this::refreshFromTop).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ActivityBoard.this, "댓글 로드 실패: " + ex.getMessage());
                }
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.add(row, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    private void refreshFromTop() {
        list.removeAll();
        page = 0;
        reachedEnd = false;
        loadMore();
    }

    private static String escape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
