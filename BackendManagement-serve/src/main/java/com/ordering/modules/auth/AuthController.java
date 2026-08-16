package com.ordering.modules.auth;

import com.ordering.common.result.R;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 顾客端微信登录：POST /api/client/auth/login  { code } */
    @PostMapping("/client/auth/login")
    public R<Map<String, String>> clientLogin(@RequestBody(required = false) Map<String, String> body) {
        String code = body == null ? null : body.get("code");
        String openid = authService.clientLogin(code);
        Map<String, String> m = new HashMap<>();
        m.put("openid", openid);
        return R.ok(m);
    }

    /** 后台登录：POST /api/admin/auth/login  { username, password } → 返回 token（需带 X-Shop-Id） */
    @PostMapping("/admin/auth/login")
    public R<Map<String, String>> adminLogin(@RequestBody(required = false) Map<String, String> body) {
        String username = body == null ? null : body.get("username");
        String password = body == null ? null : body.get("password");
        String token = authService.adminLogin(username, password);
        Map<String, String> m = new HashMap<>();
        m.put("token", token);
        return R.ok(m);
    }
}
