package ui;

import java.awt.Color;
/*  [구현된 기능]
	- 공통된 테마를 선언
*/
public class Colors {
    // ---- LG Dark theme ----
    public static final Color BG             = new Color(0x0D0D0F);  // 아주 짙은 차콜
    public static final Color CARD           = new Color(0x151518);  // 카드 배경
    public static final Color TEXT_PRIMARY   = new Color(0xF2F2F5);  // 주요 텍스트(거의 흰색)
    public static final Color TEXT_SECONDARY = new Color(0xB3B6BB);  // 보조 텍스트(쿨 그레이)
    public static final Color DIVIDER        = new Color(0x27282C);  // 디바이더

    // LG 시그니처
    public static final Color ACCENT         = new Color(0xA50034);  // LG Red
    public static final Color DANGER         = new Color(0xA50034);  // 배지/경고도 LG Red로 통일
    public static final Color MUTED_ICON     = new Color(0x9EA2A8);  // 아이콘 그레이

    private Colors() {} // 인스턴스화 방지
}
