package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import dao.CommentDao;
import dto.CommentDto;
import static ui.Colors.*;

/*  [구현된 UI]
	- 게시글과 댓글을 달 수 있는 화면을 구성
*/
public class PostDetail extends JFrame {
    private final long currentUserId;
    private final long boardId;
    private final String boardName;
    private final Post post;
    private final List<Comment> initialComments;

    private Long replyingToCommentId = null;
    private JPanel commentsBox;
    private JTextField commentField;
    private JCheckBox anonymousCmt;

    private JLabel title;
    private JTextArea body;

    private JPanel replyBadgeWrap;
    private JLabel replyBadge;

    private java.util.List<Comment> allComments = new java.util.ArrayList<>();

    private final Runnable onChanged;

    public PostDetail(long boardId, String boardName, long currentUserId,
                      Post post, List<Comment> initialComments, Runnable onChanged) {
        super(boardName + " - 게시글");
        this.boardId = boardId;
        this.boardName = boardName;
        this.currentUserId = currentUserId;
        this.post = post;
        this.initialComments = initialComments == null ? List.of() : initialComments;
        this.onChanged = onChanged;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(420, 760);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildCommentBar(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(10, 8, 8, 8));

        JLabel back = new JLabel("\u2190");
        back.setForeground(TEXT_PRIMARY);
        back.setFont(back.getFont().deriveFont(Font.BOLD, 18f));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new Hover(back));
        back.addMouseListener(new ClickSimple(this::dispose));
        p.add(back, BorderLayout.WEST);

        JLabel title = new JLabel(boardName, SwingConstants.CENTER);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        p.add(title, BorderLayout.CENTER);

        JComponent east;
        if (post.authorId == currentUserId) {
            JButton more = new JButton("\u22EE");
            more.setFocusPainted(false);
            more.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
            more.setForeground(TEXT_PRIMARY);
            more.setBackground(new Color(0x16181B));
            more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("수정");
            JMenuItem delete = new JMenuItem("삭제");
            menu.add(edit); menu.add(delete);
            edit.addActionListener(e -> onEdit());
            delete.addActionListener(e -> onDelete());
            more.addActionListener(e -> menu.show(more, 0, more.getHeight()));
            east = more;
        } else {
            JPanel ghost = new JPanel();
            ghost.setOpaque(false);
            ghost.setPreferredSize(new Dimension(28, 28));
            east = ghost;
        }
        p.add(east, BorderLayout.EAST);
        return p;
    }

    private JComponent buildBody() {
        JPanel content = new JPanel(new java.awt.GridBagLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(0, 0, 70, 0));

        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gc.anchor = java.awt.GridBagConstraints.NORTHWEST;

        int y = 0;

        // 작성자 영역
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel who = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        who.setOpaque(false);
        JLabel avatar = new JLabel("\uD83D\uDC64");
        avatar.setForeground(new Color(0xC4C7CC));
        avatar.setFont(avatar.getFont().deriveFont(22f));
        who.add(avatar);

        String display = post.isAnonymous ? "익명" : post.nickname;
        JLabel name = new JLabel(display);
        name.setForeground(TEXT_PRIMARY);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 14f));
        who.add(name);

        JLabel time = new JLabel(DateTimeFormatter.ofPattern("MM/dd HH:mm").format(post.createdAt));
        time.setForeground(TEXT_SECONDARY);
        time.setFont(time.getFont().deriveFont(12f));
        who.add(time);

        header.add(who, BorderLayout.WEST);

        gc.gridy = y++;
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        content.add(header, gc);

        // 제목
        title = new JLabel(post.title);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.setBorder(new EmptyBorder(4, 12, 6, 12));
        titleWrap.add(title, BorderLayout.WEST);

        gc.gridy = y++;
        content.add(titleWrap, gc);

        // 본문 카드 + 아래 여백/구분선
        body = new JTextArea(post.content);
        body.setWrapStyleWord(true);
        body.setLineWrap(true);
        body.setEditable(false);
        body.setOpaque(false);
        body.setForeground(TEXT_PRIMARY);
        body.setFont(body.getFont().deriveFont(15f));

        JPanel bodyCard = new JPanel(new BorderLayout());
        bodyCard.setBackground(new Color(0x121214));
        bodyCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        bodyCard.add(body, BorderLayout.CENTER);

        gc.gridy = y++;
        gc.insets = new java.awt.Insets(10, 12, 8, 12);
        content.add(bodyCard, gc);

        gc.gridy = y++;
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);
        content.add(sep, gc);

        // 댓글 리스트
        commentsBox = new JPanel();
        commentsBox.setLayout(new BoxLayout(commentsBox, BoxLayout.Y_AXIS));
        commentsBox.setOpaque(false);
        commentsBox.setBorder(new EmptyBorder(4, 12, 4, 12));

        gc.gridy = y++;
        content.add(commentsBox, gc);

        renderInitialComments();

        JScrollPane sp = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setBackground(BG);
        Scrollbars.applyDark(sp, 15, 16);

        return sp;
    }

    private JComponent buildCommentBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x121416));
        bar.setBorder(new EmptyBorder(10, 12, 12, 12));

        // 답글 모드 배지
        replyBadgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        replyBadgeWrap.setOpaque(false);
        replyBadge = new JLabel();
        replyBadge.setForeground(new Color(0xFFB3B3));
        JButton cancelReply = new JButton("\u2715");
        cancelReply.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));
        cancelReply.setFocusPainted(false);
        cancelReply.setForeground(TEXT_SECONDARY);
        cancelReply.setBackground(new Color(0x1A1D20));
        cancelReply.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelReply.addActionListener(e -> exitReplyMode());

        replyBadgeWrap.add(replyBadge);
        replyBadgeWrap.add(cancelReply);
        replyBadgeWrap.setVisible(false);
        bar.add(replyBadgeWrap, BorderLayout.NORTH);

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(0x191B1F));
        box.setBorder(new EmptyBorder(8, 10, 8, 10));
        box.setOpaque(true);

        anonymousCmt = new JCheckBox("익명");
        anonymousCmt.setOpaque(false);
        anonymousCmt.setForeground(new Color(0xFF6B6B));
        anonymousCmt.setSelected(true);
        box.add(anonymousCmt, BorderLayout.WEST);

        commentField = new JTextField();
        commentField.setForeground(TEXT_PRIMARY);
        commentField.setBackground(new Color(0x191B1F));
        commentField.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        commentField.putClientProperty("JTextField.placeholderText", "댓글을 입력하세요.");
        box.add(commentField, BorderLayout.CENTER);

        JButton send = new JButton("\uD83D\uDCE7");
        send.setFocusPainted(false);
        send.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        send.setForeground(new Color(0xFF6B6B));
        send.setBackground(new Color(0x191B1F));
        send.addActionListener(e -> onSendComment());
        box.add(send, BorderLayout.EAST);

        bar.add(box, BorderLayout.CENTER);
        return bar;
    }

    // 넘겨받은 댓글을 한 번에 렌더
    private void renderInitialComments() {
        allComments = new java.util.ArrayList<>(initialComments);
        rerenderCommentsAsTree();
    }

    private void rerenderCommentsAsTree() {
        commentsBox.removeAll();

        // parentId -> children list
        java.util.Map<Long, java.util.List<Comment>> children = new java.util.HashMap<>();
        java.util.List<Comment> roots = new java.util.ArrayList<>();

        for (Comment c : allComments) {
            if (c.parentId == null) {
                roots.add(c);
            } else {
                children.computeIfAbsent(c.parentId, k -> new java.util.ArrayList<>()).add(c);
            }
        }

        // 정렬 기준(같은 부모 아래에서는 시간/ID 순)
        java.util.Comparator<Comment> cmp = java.util.Comparator
                .comparing((Comment c) -> c.createdAt)
                .thenComparingLong(c -> c.id);

        roots.sort(cmp);
        for (var list : children.values()) list.sort(cmp);

        // DFS로 부모 다음에 자식들 렌더
        for (Comment r : roots) {
            renderOne(r, 0, children, cmp);
            addSeparator();
        }

        commentsBox.revalidate();
        commentsBox.repaint();
    }

    private void renderOne(Comment c, int depth,
                           java.util.Map<Long, java.util.List<Comment>> children,
                           java.util.Comparator<Comment> cmp) {
        addCommentRowDepth(c, depth);

        java.util.List<Comment> kids = children.get(c.id);
        if (kids == null || kids.isEmpty()) return;
        for (Comment k : kids) {
            renderOne(k, depth + 1, children, cmp);
            addSeparator();
        }
    }

    private void addSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);
        commentsBox.add(sep);
    }

    // ===== Actions =====
    private void onSendComment() {
        String txt = commentField.getText().trim();
        if (txt.isEmpty()) return;

        boolean isAnon = anonymousCmt.isSelected();

        try {
            Integer parentId =
                (replyingToCommentId == null) ? null : Integer.valueOf((int)(long)replyingToCommentId);

            // DB insert
            int newId = CommentDao.insert(
                    (int) post.id,
                    (int) currentUserId,
                    parentId,
                    txt,
                    isAnon
            );

            // 방금 쓴 댓글 다시 읽기
            CommentDto saved = CommentDao.findById(newId);
            if (saved == null) {
                JOptionPane.showMessageDialog(this, "댓글 저장 후 조회에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (saved.getPostId() != (int) post.id) {
                JOptionPane.showMessageDialog(this, "잘못된 댓글 참조입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Comment ui = toUiComment(saved);

            allComments.add(ui);
            rerenderCommentsAsTree();

            commentField.setText("");
            exitReplyMode();
            if (onChanged != null) onChanged.run();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "댓글 등록 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        EditPostDialog dlg = new EditPostDialog(this, post.title, post.content, post.isAnonymous);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;

        String newTitle = dlg.getTitleText();
        String newBody  = dlg.getBodyText();
        boolean newAnon = dlg.isAnonymous();

        try {
            int updated = dao.PostDao.updatePost((int) post.id, currentUserId, newTitle, newBody, newAnon);
            if (updated == 0) {
                JOptionPane.showMessageDialog(this, "수정 권한이 없거나 글이 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            title.setText(newTitle);
            body.setText(newBody);
            body.setCaretPosition(0);

            if (onChanged != null) onChanged.run();
            JOptionPane.showMessageDialog(this, "수정되었습니다.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int ok = JOptionPane.showConfirmDialog(this, "정말 삭제할까요?", "삭제", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            boolean deleted = dao.PostDao.deletePost((int) post.id, currentUserId);
            if (!deleted) {
                JOptionPane.showMessageDialog(this, "삭제 권한이 없거나 글이 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            if (onChanged != null) onChanged.run();
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCommentRowDepth(Comment c, int depth) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel avatar = new JLabel("\uD83D\uDC64");
        avatar.setForeground(new Color(0xC4C7CC));
        avatar.setFont(avatar.getFont().deriveFont(18f));
        row.add(avatar, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        String who  = displayNameFor(c);
        String when = DateTimeFormatter.ofPattern("MM/dd HH:mm").format(c.createdAt);
        JLabel head = new JLabel(who + "    " + when);
        head.setForeground(TEXT_SECONDARY);
        head.setFont(head.getFont().deriveFont(12f));
        center.add(head);

        JTextArea content = new JTextArea(c.content);
        content.setWrapStyleWord(true);
        content.setLineWrap(true);
        content.setEditable(false);
        content.setOpaque(false);
        content.setForeground(TEXT_PRIMARY);
        content.setBorder(new EmptyBorder(2, 0, 2, 0));
        center.add(content);

        if ("[삭제된 댓글입니다]".equals(c.content)) {
            content.setForeground(TEXT_SECONDARY);
            content.setFont(content.getFont().deriveFont(Font.ITALIC));
        }

        row.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(true);
        actions.setBackground(new Color(0x1A1D20));
        actions.setBorder(new EmptyBorder(6, 8, 6, 8));

        JButton replyBtn = pillBtn("\uD83D\uDCAC");
        JButton moreBtn  = pillBtn("\u22EE");

        replyBtn.addActionListener(e -> {
            replyingToCommentId = c.id;
            String target = displayNameFor(c);
            replyBadge.setText("답글 대상: " + target);
            replyBadgeWrap.setVisible(true);
            commentField.requestFocusInWindow();
            commentField.putClientProperty("JTextField.placeholderText", "답글을 입력하세요…");
            flashHighlight(row);
        });

        moreBtn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem report = new JMenuItem("신고");
            report.addActionListener(ev -> JOptionPane.showMessageDialog(this, "신고(데모)"));
            menu.add(report);

            if (c.userId == currentUserId) {
                JMenuItem del = new JMenuItem("삭제");
                del.addActionListener(ev -> onDeleteComment(c));
                menu.add(del);
            }
            menu.show(moreBtn, 0, moreBtn.getHeight());
        });

        actions.add(replyBtn); actions.add(moreBtn);
        row.add(actions, BorderLayout.EAST);

        // depth 만큼 들여쓰기 (한 단계당 18px)
        int leftPad = 18 * depth;
        JPanel indentWrap = new JPanel(new BorderLayout());
        indentWrap.setOpaque(false);
        indentWrap.setBorder(new EmptyBorder(0, leftPad, 0, 0));
        indentWrap.add(row, BorderLayout.CENTER);

        commentsBox.add(indentWrap);
    }

    // === 표시명 ===
    private String displayNameFor(Comment c) {
        if (c.userId == post.authorId) return "작성자";
        if (c.isAnonymous && c.aliasNo != null) return "익명" + c.aliasNo; // DB alias
        return c.userNickname;
    }

    private static Comment toUiComment(CommentDto d) {
        return new Comment(
            d.getId(),
            d.getPostId(),
            d.getUserId(),
            d.isAnonymous(),
            d.getContent(),
            d.getCreatedAt(),
            d.getParentId() == null ? null : d.getParentId().longValue(),
            d.getUserNickname(),
            d.getAliasNo() // ✅ DB에서 내려온 익명번호
        );
    }

    private JButton pillBtn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(2, 6, 2, 6));
        b.setForeground(TEXT_SECONDARY);
        b.setBackground(new Color(0x1A1D20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class Hover extends java.awt.event.MouseAdapter {
        private final JComponent t;
        private Color original;
        Hover(JComponent t){ this.t = t; }
        @Override public void mouseEntered(java.awt.event.MouseEvent e) {
            original = t.getBackground();
            t.setBackground(new Color(0x1C2024));
            t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public void mouseExited(java.awt.event.MouseEvent e) {
            t.setBackground(original);
            t.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void exitReplyMode() {
        replyingToCommentId = null;
        replyBadgeWrap.setVisible(false);
        commentField.putClientProperty("JTextField.placeholderText", "댓글을 입력하세요.");
    }

    private void onDeleteComment(Comment target) {
        if (target.userId != currentUserId) {
            JOptionPane.showMessageDialog(this, "본인 댓글만 삭제할 수 있어요.", "권한 없음", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean hasChild = false;
        for (Comment c : allComments) {
            if (c.parentId != null && c.parentId.equals(target.id)) { hasChild = true; break; }
        }

        int ok = JOptionPane.showConfirmDialog(this,
                hasChild ? "대댓글이 있어 본문만 가립니다. 진행할까요?"
                         : "이 댓글을 완전히 삭제할까요?",
                "삭제", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (hasChild) {
                int n = dao.CommentDao.softDeleteByIdAndUser((int)target.id, (int)currentUserId);
                if (n == 0) {
                    JOptionPane.showMessageDialog(this, "삭제 실패(권한/상태 확인).", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                target.content = "[삭제된 댓글입니다]";
            } else {
                int n = dao.CommentDao.deleteByIdAndUser((int)target.id, (int)currentUserId);
                if (n == 0) {
                    JOptionPane.showMessageDialog(this, "삭제 실패(권한/상태 확인).", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                allComments.removeIf(c -> c.id == target.id);
            }

            rerenderCommentsAsTree();

            if (onChanged != null) onChanged.run();
            JOptionPane.showMessageDialog(this, "삭제되었습니다.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "삭제 중 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void flashHighlight(JComponent targetRow) {
        Color original = targetRow.getBackground();
        Color hi = new Color(0x1F2A30);
        targetRow.setOpaque(true);
        targetRow.setBackground(hi);
        targetRow.repaint();

        new javax.swing.Timer(1000, e -> {
            targetRow.setBackground(original);
            targetRow.setOpaque(false);
            targetRow.repaint();
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private static class ClickSimple extends java.awt.event.MouseAdapter {
        private final Runnable r; ClickSimple(Runnable r){ this.r = r; }
        @Override public void mouseClicked(java.awt.event.MouseEvent e){ r.run(); }
    }

    // DTOs
    public static class Post {
        public final long id;
        public final long authorId;
        public final String title;
        public final String content;
        public final boolean isAnonymous;
        public final String nickname;
        public final LocalDateTime createdAt;
        public Post(long id, long authorId, String title, String content, boolean isAnonymous, String nickname, LocalDateTime createdAt) {
            this.id=id; this.authorId=authorId; this.title=title; this.content=content;
            this.isAnonymous=isAnonymous; this.nickname=nickname; this.createdAt=createdAt;
        }
    }

    public static class Comment {
        public final long id;
        public final long postId;
        public final long userId;
        public final boolean isAnonymous;
        public String content;
        public final LocalDateTime createdAt;
        public final Long parentId;
        public final String userNickname;

        // ✅ 추가
        public final Integer aliasNo;

        public Comment(long id, long postId, long userId, boolean isAnonymous,
                       String content, LocalDateTime createdAt, Long parentId,
                       String userNickname, Integer aliasNo) { // ✅ 파라미터 추가
            this.id=id; this.postId=postId; this.userId=userId; this.isAnonymous=isAnonymous;
            this.content=content; this.createdAt=createdAt; this.parentId=parentId;
            this.userNickname = userNickname;
            this.aliasNo = aliasNo; // ✅ 대입
        }
    }


    private static class EditPostDialog extends JDialog {
        private boolean saved = false;
        private final JTextField titleField = new JTextField();
        private final JTextArea  bodyArea   = new JTextArea();
        private final JCheckBox  anonChk    = new JCheckBox("익명");

        EditPostDialog(JFrame owner, String title, String body, boolean isAnon) {
            super(owner, "글 수정", true);
            setSize(420, 480);
            setLocationRelativeTo(owner);
            setResizable(false);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(BG);
            setContentPane(root);

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BG);
            header.setBorder(new EmptyBorder(12, 12, 8, 12));

            JLabel close = new JLabel("\u2715");
            close.setForeground(TEXT_PRIMARY);
            close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            close.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { dispose(); }
            });
            header.add(close, BorderLayout.WEST);

            JLabel ttl = new JLabel("글 수정", SwingConstants.CENTER);
            ttl.setForeground(TEXT_PRIMARY);
            ttl.setFont(ttl.getFont().deriveFont(Font.BOLD, 18f));
            header.add(ttl, BorderLayout.CENTER);

            JButton save = new JButton("저장");
            save.setForeground(Color.WHITE);
            save.setBackground(ACCENT);
            save.setFocusPainted(false);
            save.setBorder(new EmptyBorder(8,14,8,14));
            save.addActionListener(e -> {
                if (titleField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (bodyArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "내용을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                saved = true;
                dispose();
            });
            header.add(save, BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);

            // Body
            JPanel content = new JPanel();
            content.setBackground(BG);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(8, 12, 12, 12));

            titleField.setText(title);
            titleField.setBackground(new Color(0x121214));
            titleField.setForeground(TEXT_PRIMARY);
            titleField.setBorder(new EmptyBorder(8,10,8,10));
            titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            content.add(titleField);
            content.add(Box.createVerticalStrut(8));

            bodyArea.setText(body);
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            bodyArea.setBackground(new Color(0x121214));
            bodyArea.setForeground(TEXT_PRIMARY);
            bodyArea.setBorder(new EmptyBorder(8,10,8,10));
            JScrollPane sc = new JScrollPane(bodyArea,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sc.setBorder(null);
            content.add(sc);
            content.add(Box.createVerticalStrut(8));

            anonChk.setSelected(isAnon);
            anonChk.setOpaque(false);
            anonChk.setForeground(TEXT_PRIMARY);
            content.add(anonChk);

            root.add(content, BorderLayout.CENTER);
        }

        boolean isSaved() { return saved; }
        String getTitleText() { return titleField.getText().trim(); }
        String getBodyText()  { return bodyArea.getText().trim(); }
        boolean isAnonymous() { return anonChk.isSelected(); }
    }
}
