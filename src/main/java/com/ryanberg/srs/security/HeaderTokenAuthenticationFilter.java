package com.ryanberg.srs.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter used to validate the authorization token in the header
 */
public class HeaderTokenAuthenticationFilter extends GenericFilterBean
{

    private UserDetailsService userDetailsService;
    private TokenUtils tokenUtils;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        UserDetails userDetails = loadUserDetails((HttpServletRequest) servletRequest);
        SecurityContext securityContext = createSecurityContext(userDetails);

        try {

            SecurityContextHolder.setContext(securityContext);

            if (securityContext.getAuthentication() != null && securityContext.getAuthentication().isAuthenticated()) {
                String userName = (String) securityContext.getAuthentication().getPrincipal();
                tokenUtils.addHeader((HttpServletResponse) servletResponse, userName);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally {
            // Clear the context and free the thread local
            SecurityContextHolder.clearContext();
        }
    }

    private SecurityContext createSecurityContext(UserDetails userDetails)
    {
        if (userDetails != null) {

            SecurityContextImpl securityContext = new SecurityContextImpl();
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                    null, userDetails.getAuthorities());
            securityContext.setAuthentication(authentication);
            return securityContext;

        }
        return SecurityContextHolder.createEmptyContext();
    }

    private UserDetails loadUserDetails(HttpServletRequest request)
    {
        String userName = tokenUtils.getUserName(request);
        return userName != null ? userDetailsService.loadUserByUsername(userName) : null;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

    public void setTokenUtils(TokenUtils tokenUtils)
    {
        this.tokenUtils = tokenUtils;
    }
}
