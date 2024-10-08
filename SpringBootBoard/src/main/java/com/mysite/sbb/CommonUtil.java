package com.mysite.sbb;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CommonUtil {
    public String markdown(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
//        return renderer.render(document);
        String html = renderer.render(document);
        
        System.out.println("Converted HTML: " + html);
        
        // Sanitize HTML
 		PolicyFactory policy = new HtmlPolicyBuilder()
 			.allowElements("h1", "h2", "h3", "p", "b", "i", "em", "strong", "img", "a", "ul", "ol", "li", "table", "thead", "tbody", "tr", "th", "td", "del", "blockquote", "code", "pre", "input", "hr")
 			.allowUrlProtocols("https", "http")
 			.allowAttributes("href", "target").onElements("a")
 			.allowAttributes("src", "alt").onElements("img")
 			.allowAttributes("type", "checked", "disabled").onElements("input")
 			.allowAttributes("border", "cellspacing", "cellpadding").onElements("table")
 			.requireRelNofollowOnLinks()
 			.toFactory();
 		
 		String safeHtml = policy.sanitize(html);
		System.out.println("After sanitize: " + safeHtml);
		return safeHtml; // Return the sanitized HTML
    }
}