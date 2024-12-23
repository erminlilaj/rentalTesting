package it.linksmt.rental.security;
import it.linksmt.rental.security.SecurityBean;


public class SecurityContext {
    private static final ThreadLocal<SecurityBean> threadLocal= new ThreadLocal();

    public static SecurityBean get() {
        return threadLocal.get();
    }

        public static void set(SecurityBean securityBean) {
            threadLocal.set(securityBean);
        }
    }

