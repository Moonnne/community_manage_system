package edu.tut.cms.common.context;

public class UserContext {

    private static final ThreadLocal<CurrentUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(CurrentUser user) {
        USER_HOLDER.set(user);
    }

    public static CurrentUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
