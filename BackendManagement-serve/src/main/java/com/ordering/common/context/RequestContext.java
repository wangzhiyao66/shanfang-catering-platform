package com.ordering.common.context;

/**
 * 请求级上下文（ThreadLocal）：存放租户 shopId、顾客 openid/memberId、后台 adminId。
 * 由拦截器写入，请求结束（ShopInterceptor.afterCompletion）清理，MyBatis-Plus 多租户插件读取 shopId。
 */
public class RequestContext {

    private static final ThreadLocal<Long> SHOP_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();
    private static final ThreadLocal<Long> MEMBER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();

    public static void setShopId(Long id) { SHOP_ID.set(id); }
    public static Long getShopId() { return SHOP_ID.get(); }

    public static void setOpenid(String o) { OPENID.set(o); }
    public static String getOpenid() { return OPENID.get(); }

    public static void setMemberId(Long id) { MEMBER_ID.set(id); }
    public static Long getMemberId() { return MEMBER_ID.get(); }

    public static void setAdminId(Long id) { ADMIN_ID.set(id); }
    public static Long getAdminId() { return ADMIN_ID.get(); }

    public static void clear() {
        SHOP_ID.remove();
        OPENID.remove();
        MEMBER_ID.remove();
        ADMIN_ID.remove();
    }
}
