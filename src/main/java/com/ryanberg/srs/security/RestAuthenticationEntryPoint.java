package com.ryanberg.srs.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException
    {
        if (isPreflight(request)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        else {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode()
                    .put("error", true)
                    .put("cause", "UNAUTHORIZED")
                    .put("message", "Unauthorized: Authentication token was either missing or invalid.");
            PrintWriter out = response.getWriter();
            out.print(node.toString());
            out.flush();
            out.close();

        }
    }

    private boolean isPreflight(HttpServletRequest request)
    {
        return "OPTIONS".equals(request.getMethod());
    }
}