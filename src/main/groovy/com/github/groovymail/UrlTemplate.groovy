package com.github.groovymail

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine

class UrlTemplate implements TemplateSource {

    String url

    UrlTemplate(String url) {
        this.url = url
    }

    public Template locate(MarkupTemplateEngine engine) {
        return engine.createTemplate( new URL(url) )
    }
}
