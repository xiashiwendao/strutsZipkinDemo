package brave.servlet;

import brave.Span;
import brave.SpanCustomizer;
import brave.Tracer;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation.Getter;
import brave.propagation.TraceContext;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MyTracingFilter implements Filter {
	static final Getter<HttpServletRequest, String> GETTER = new Getter<HttpServletRequest, String>() {
		public String get(HttpServletRequest carrier, String key) {
			return carrier.getHeader(key);
		}

		@Override
		public String toString() {
			return "HttpServletRequest::getHeader";
		}
	};
	static final HttpServletAdapter ADAPTER = new HttpServletAdapter();

	public static Filter create(Tracing tracing) {
		return new TracingFilter(HttpTracing.create(tracing));
	}

	public static Filter create(HttpTracing httpTracing) {
		return new MyTracingFilter(httpTracing);
	}

	final ServletRuntime servlet = ServletRuntime.get();
	final Tracer tracer;
	final HttpServerHandler<HttpServletRequest, HttpServletResponse> handler;
	final TraceContext.Extractor<HttpServletRequest> extractor;

	MyTracingFilter(HttpTracing httpTracing) {
		tracer = httpTracing.tracing().tracer();
		handler = HttpServerHandler.create(httpTracing, ADAPTER);
		extractor = httpTracing.tracing().propagation().extractor(GETTER);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = servlet.httpResponse(response);

		String url = httpRequest.getRequestURI();
		String resource = url.substring(url.lastIndexOf("/"));
		// 对于静态资源的请求不记录日志
		if (resource.contains(".") && !resource.contains(".do")) {
			chain.doFilter(httpRequest, httpResponse);
			return;
		}

		// Prevent duplicate spans for the same request
		if (request.getAttribute("TracingFilter") != null) {
			chain.doFilter(request, response);
			return;
		}

		request.setAttribute("TracingFilter", "true");

		Span span = handler.handleReceive(extractor, httpRequest);

		// Add attributes for explicit access to customization or span context
		request.setAttribute(SpanCustomizer.class.getName(), span.customizer());
		request.setAttribute(TraceContext.class.getName(), span.context());

		Throwable error = null;
		Tracer.SpanInScope ws = null;
		try {
			ws = tracer.withSpanInScope(span);
			// any downstream code can see Tracer.currentSpan() or use
			// Tracer.currentSpanCustomizer()
			chain.doFilter(httpRequest, httpResponse);
		} catch (IOException e) {
			error = e;
			throw e;
		} catch (ServletException e) {
			error = e;
			throw e;
		} finally {
			if (ws != null) {
				ws.close();
			}
			if (servlet.isAsync(httpRequest)) { // we don't have the actual
												// response, handle later
				servlet.handleAsync(handler, httpRequest, span);
			} else { // we have a synchronous response, so we can finish the
						// span
				handler.handleSend(ADAPTER.adaptResponse(httpRequest, httpResponse), error, span);
			}
		}
	}

	public void destroy() {
	}

	public void init(FilterConfig filterConfig) {
	}
}
