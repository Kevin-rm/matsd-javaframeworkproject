package mg.matsd.javaframework.servletwrapper.http;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.utils.Assert;

import java.io.IOException;
import java.io.PrintWriter;

public class Response {
    protected final HttpServletResponse raw;

    public Response(HttpServletResponse raw) {
        Assert.notNull(raw, "L'argument raw ne peut pas être \"null\"");
        this.raw = raw;
    }

    public HttpServletResponse getRaw() {
        return raw;
    }

    public Response setHeader(String name, String value) {
        raw.setHeader(name, value);
        return this;
    }

    public Response setContentType(String contentType) {
        raw.setContentType(contentType);
        return this;
    }

    public Response setCharset(String charset) {
        raw.setCharacterEncoding(charset);
        return this;
    }

    public Response addCookie(Cookie cookie) {
        raw.addCookie(cookie);
        return this;
    }

    public Response addCookie(String name, String value) {
        return addCookie(new Cookie(name, value));
    }

    public Response setStatus(int status) {
        raw.setStatus(status);
        return this;
    }

    public Response ok() {
        return setStatus(HttpServletResponse.SC_OK);
    }

    public Response notFound() {
        return setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    public Response badRequest() {
        return setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    public Response unauthorized() {
        return setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public Response internalServerError() {
        return setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    public Response redirect(String location) throws IOException {
        raw.sendRedirect(location);
        return this;
    }

    public Response write(String content) throws IOException {
        getWriter().write(content);
        return this;
    }

    public Response html(String html) throws IOException {
        return setContentType("text/html").write(html);
    }

    public Response text(String text) throws IOException {
        return setContentType("text/plain").write(text);
    }

    public Response noCache() {
        return setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
            .setHeader("Pragma", "no-cache")
            .setHeader("Expires", "0");
    }

    public Response cacheFor(int seconds) {
        return setHeader("Cache-Control", "public, max-age=" + seconds);
    }

    public Response csp(String policy) {
        return setHeader("Content-Security-Policy", policy);
    }

    public Response hsts() {
        return setHeader("Strict-Transport-Security", "max-age=63072000; includeSubDomains");
    }

    public Response xssProtection() {
        return setHeader("X-XSS-Protection", "1; mode=block");
    }

    public Response nosniff() {
        return setHeader("X-Content-Type-Options", "nosniff");
    }

    public Response frameOptions(String option) {
        return setHeader("X-Frame-Options", option);
    }

    public boolean isCommitted() {
        return raw.isCommitted();
    }

    public void end() throws IOException {
        if (isCommitted()) return;

        PrintWriter printWriter = getWriter();
        printWriter.flush();
        printWriter.close();
    }

    public PrintWriter getWriter() throws IOException {
        return raw.getWriter();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return raw.getOutputStream();
    }
}
