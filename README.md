# 유레카 익명 커뮤니티 (U+REKA Anonymous Community)

유플러스 유레카 3기 미니 프로젝트 – **수강생 전용 커뮤니티**  
수업 관련 Q&A, 자유게시판, 익명 댓글/대댓글, 내가 쓴 글/댓글 모아보기 기능을 제공합니다.  
Java **Swing** + **MySQL** + 간단한 DAO 레이어로 구성했습니다.

## 목차
- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)

---

## 핵심 기능

- **회원가입/로그인**
  - 이메일/비밀번호 로그인
  - 초대코드 검증(예: `EUREKA`, `LGU123`)
- **게시판/게시글**
  - 게시판 목록(자유/질문 …)
  - 게시글 목록, 작성/수정/삭제
- **댓글/대댓글**
  - 트리 구조(원댓글/대댓글)
  - **익명** 지원: 같은 글에서 같은 사용자는 **동일 익명번호(익명N)** 유지
  - 본인 댓글만 삭제 가능  
    - **자식 있음** → 내용만 `[삭제된 댓글입니다]`(소프트 삭제)  
    - **자식 없음** → 실제 삭제
- **내 활동 모아보기**
  - **내가 쓴 글** / **댓글 단 글** 페이징 조회
  - 최근 댓글 작성 시각 기준 정렬(서브쿼리 + JOIN)
- **다크 테마 UI**
  - 공통 컬러 토큰(`ui.Colors`)로 일관된 스타일

---
## 기술 스택

- Language: **Java 17+**
- UI: **Swing**
- DB: **MySQL 8.x**
- 빌드: Maven 또는 Gradle
- 기타: JDBC, DAO 패턴

---

## 프로젝트 구조
```
src/
└─ main/
├─ java/
│ ├─ app/
│ │ └─ Launcher.java # 진입점(로그인 → Boards)
│ ├─ auth/
│ │ └─ Session.java # 로그인 세션
│ ├─ dao/ # DB 접근 (BoardDao, PostDao, CommentDao, MyActivityDao, UserDao ...)
│ ├─ dto/ # DTO들 (LoginDto, CommentDto ...)
│ ├─ jdbc/
│ │ └─ DBManager.java # 커넥션 팩토리(환경설정 읽기)
│ └─ ui/
│ ├─ Colors.java
│ ├─ Login.java / Signup.java
│ ├─ Boards.java # 게시판 목록
│ ├─ Board.java # 게시글 목록(페이징)
│ ├─ PostDetail.java # 글 상세 + 댓글 트리 + 작성/삭제
│ └─ ActivityBoard.java # 내가 쓴 글 / 댓글 단 글
└─ resources/
├─ img/logo.png
└─ db.properties
```

