package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/*  [구현된 UI]
	- 스크롤바의 테마를 구현
*/

public final class Scrollbars {
    private Scrollbars() {}

    /** JScrollPane에 다크 스크롤바 적용 */
    public static void applyDark(JScrollPane sp, int thickness, int unitInc) {
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        JScrollBar vb = sp.getVerticalScrollBar();
        vb.setUI(new DarkScrollBarUI());
        vb.setOpaque(false);
        vb.setPreferredSize(new Dimension(thickness, 0));
        vb.setUnitIncrement(unitInc);

        JScrollBar hb = sp.getHorizontalScrollBar();
        if (hb != null) {
            hb.setUI(new DarkScrollBarUI());
            hb.setOpaque(false);
            hb.setPreferredSize(new Dimension(0, thickness));
        }
    }

    /** 살짝 밝은 다크 스크롤바 */
    public static class DarkScrollBarUI extends BasicScrollBarUI {
        private static final Color TRACK       = new Color(0x000000);
        private static final Color THUMB       = new Color(0x111111);
        private static final Color THUMB_HOVER = new Color(0x191919);
        private static final Color THUMB_DRAG  = new Color(0x222222);

        @Override protected void configureScrollBarColors() {
            trackColor = TRACK; thumbColor = THUMB;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(TRACK); g.fillRect(r.x, r.y, r.width, r.height);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (!c.isEnabled() || (r.width > r.height && r.height < 2)) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = isDragging ? THUMB_DRAG : (isThumbRollover() ? THUMB_HOVER : THUMB);
            int arc = 8;
            g2.setColor(color);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, arc, arc);
            g2.dispose();
        }
        @Override protected JButton createDecreaseButton(int o){ JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        @Override protected JButton createIncreaseButton(int o){ JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
    }
}
