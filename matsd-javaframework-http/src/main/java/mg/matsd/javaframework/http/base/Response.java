package mg.matsd.javaframework.http.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.CollectionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

public class Response {
    protected final HttpServletResponse raw;
    protected final Request request;

    public Response(HttpServletResponse raw, Request request) {
        Assert.notNull(raw, "L'argument raw ne peut pas être \"null\"");
        Assert.notNull(request, "La requête ne peut pas être \"null\"");

        this.raw     = raw;
        this.request = request;
    }

    public HttpServletResponse getRaw() {
        return raw;
    }

    public String getHeader(String name) {
        Assert.notBlank(name, false, "Le nom de l'en-tête ne peut pas être vide ou \"null\"");
        return raw.getHeader(name);
    }

    public Collection<String> getHeaders(String name) {
        Assert.notBlank(name, false, "Le nom de l'en-tête ne peut pas être vide ou \"null\"");
        return raw.getHeaders(name);
    }

    public boolean hasHeader(String name) {
        Assert.notBlank(name, false, "Le nom de l'en-tête ne peut pas être vide ou \"null\"");
        return raw.containsHeader(name);
    }

    public Response setHeader(String name, String value) {
        Assert.notBlank(name, false, "Le nom de l'en-tête ne peut pas être vide ou \"null\"");

        raw.setHeader(name, value);
        return this;
    }

    public Response addHeader(String name, String value) {
        Assert.notBlank(name, false, "Le nom de l'en-tête ne peut pas être vide ou \"null\"");

        raw.addHeader(name, value);
        return this;
    }

    public String getContentType() {
        return raw.getContentType();
    }

    public Response setContentType(String contentType) {
        Assert.notBlank(contentType, false, "Le type de contenu ne peut pas être vide ou \"null\"");

        raw.setContentType(contentType);
        return this;
    }

    public Response asHtml() {
        return setContentType("text/html");
    }

    public Response asText() {
        return setContentType("text/plain");
    }

    public Response asJson() {
        return setContentType("application/json");
    }

    public String getCharset() {
        return raw.getCharacterEncoding();
    }

    public Response setCharset(String charset) {
        Assert.notBlank(charset, false, "L'argument charset ne peut pas être vide ou \"null\"");

        raw.setCharacterEncoding(charset);
        return this;
    }

    public Response addCookie(Cookie cookie) {
        Assert.notNull(cookie, "Le cookie ne peut pas être \"null\"");

        raw.addCookie(cookie);
        return this;
    }

    public Response addCookie(String name, @Nullable String value) {
        Assert.notBlank(name, false, "Le nom du cookie ne peut pas être vide ou \"null\"");
        return addCookie(new Cookie(name, value));
    }

    public int getStatus() {
        return raw.getStatus();
    }

    public Response setStatus(HttpStatusCode status) {
        Assert.notNull(status, "Le code de statut HTTP ne peut pas être \"null\"");
        return setStatus(status.getValue());
    }

    public Response setStatus(int status) {
        raw.setStatus(status);
        return this;
    }

    public Response ok() {
        return setStatus(HttpServletResponse.SC_OK);
    }

    public Response badRequest() {
        return setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    public Response unauthorized() {
        return setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public Response notFound() {
        return setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    public Response methodNotAllowed() {
        return setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public Response internalServerError() {
        return setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    public Response redirect(String location) throws IOException {
        Assert.notBlank(location, false, "L'argument location ne peut pas être vide ou \"null\"");

        raw.sendRedirect(location);
        return this;
    }

    public Response forwardTo(String path, @Nullable Map<String, Object> attributes) throws ServletException, IOException {
        Assert.notBlank(path, false, "Le chemin ne peut pas être vide ou \"null\"");

        HttpServletRequest httpServletRequest = request.getRaw();

        if (!CollectionUtils.isEmpty(attributes)) attributes.forEach(httpServletRequest::setAttribute);
        httpServletRequest.getRequestDispatcher(path).forward(httpServletRequest, raw);
        return this;
    }

    public Response forwardTo(String path) throws ServletException, IOException {
        return forwardTo(path, null);
    }

    public Response error(int status, String message) throws IOException {
        raw.sendError(status, message);
        return this;
    }

    public Response error(int status) throws IOException {
        raw.sendError(status);
        return this;
    }

    public Response error(HttpStatusCode status, String message) throws IOException {
        Assert.notNull(status, "Le code de statut HTTP ne peut pas être \"null\"");

        return error(status.getValue(), message);
    }

    public Response error(HttpStatusCode status) throws IOException {
        Assert.notNull(status, "Le code de statut HTTP ne peut pas être \"null\"");

        return error(status.getValue());
    }

    public Response write(String content) throws IOException {
        Assert.notNull(content, "Le contenu ne peut pas être \"null\"");

        getWriter().write(content);
        return this;
    }

    public Response print(@Nullable String content) throws IOException {
        getWriter().print(content);
        return this;
    }

    public Response println(@Nullable String content) throws IOException {
        getWriter().println(content);
        return this;
    }

    public Response println() throws IOException {
        getWriter().println();
        return this;
    }

    public Response printf(String format, Object... args) throws IOException {
        getWriter().printf(format, args);
        return this;
    }

    public Response flush() throws IOException {
        getWriter().flush();
        return this;
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
