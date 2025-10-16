// auth/Session.java
package auth;

import dto.LoginDto;

public final class Session {
    private static LoginDto user;

    private Session() {}

    public static void setUser(LoginDto u) { user = u; }

    public static LoginDto getUser() { return user; }

    public static boolean isLoggedIn() { return user != null; }

    public static int currentUserId() {
        if (user == null) throw new IllegalStateException("로그인 필요");
        return user.getId();
    }

    public static String currentNickname() {
        if (user == null) throw new IllegalStateException("로그인 필요");
        return user.getNickname();
    }

    public static void logout() { user = null; }
}
