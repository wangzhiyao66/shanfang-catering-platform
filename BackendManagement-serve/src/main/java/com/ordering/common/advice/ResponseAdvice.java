package com.ordering.common.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordering.common.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一成功响应：把 Controller 返回值包成 { code:0, data, msg:"ok" }。
 * 已是 R 的（如异常处理器返回的）直接透传；String 类型需手动序列化成 JSON。
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof R) {
            return body;
        }
        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(R.ok(body));
            } catch (Exception e) {
                return R.ok(body);
            }
        }
        return R.ok(body);
    }
}
