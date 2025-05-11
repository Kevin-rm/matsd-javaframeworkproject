import type { Error, File } from "../types.ts";

const files: File[] = [
	{
		exception: {
			className: "java.lang.NullPointerException",
			message: "Parameter 'model' is null"
		},
		fullPath: "mg/itu/prom16/controllers/TestController.java",
		method: "index",
		highlightedLine: 23,
		code: `public class TestController {
    public ModelView index() {
        Model model = null; // Ligne problématique
        model.setAttributes(request);
        return new ModelView("index.jsp");
    }
}`
	},
	{
		exception: {
			className: "java.lang.IllegalStateException",
			message: "Model attributes cannot be set after response is committed"
		},
		fullPath: "mg/itu/prom16/base/ModelView.java",
		method: "setAttributes",
		highlightedLine: 45,
		code: `public class ModelView {
    public void setAttributes(Request request) {
        if (responseCommitted) {
            throw new IllegalStateException(
                "Model attributes cannot be set after response is committed");
        }
        // ...
    }
}`
	}
];

export const errorData: Error = {
	exception: {
		className: "java.lang.NullPointerException",
		message: "Cannot invoke \"mg.itu.prom16.base.Model.setAttributes(mg.matsd.javaframework.servletwrapper.http.Request)\" because \"model\" is null"
	},
	files: files,
	requestInfo: {
		method: "GET",
		serverName: "localhost",
		uri: "/test",
		port: 8080,
		headers: {
			"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
			"Accept": "text/html,application/xhtml+xml",
			"Accept-Language": "fr-FR,fr;q=0.9",
			"Connection": "keep-alive"
		},
		body: {
			"param1": "value1",
			"param2": 123
		},
		statusCodeReason: "Internal Servor Error"
	},
	appDetails: {
		javaVersion: "19.0.2",
		jakartaEEVersion: "10.0.0",
		matsdjavaframeworkVersion: "1.0-SNAPSHOT"
	}
};
