package asz.vizsgaremek.websocket;

import asz.vizsgaremek.auth.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor{
    private final JwtUtil jwtUtil;

    public UserHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer "
            }
            System.out.println("==== HANDSHAKE ====");
            System.out.println("Token param: " + servletRequest.getServletRequest().getParameter("token"));

            try {
                String username = jwtUtil.extractUsername(token);
                Principal principal = () -> username;
                attributes.put("user", principal);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // nothing
    }
}
