package com.github.groovymail

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine

class ReaderTemplate implements TemplateSource {

    String sourceName
    Reader reader

    ReaderTemplate(Reader reader, String sourceName = null) {
        this.reader = reader
        this.sourceName = sourceName
    }

    @Override
    Template locate(MarkupTemplateEngine engine) {
        return engine.createTemplate( reader, sourceName )
    }
}
