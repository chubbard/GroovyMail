package com.github.groovymail

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine

class FileTemplate implements TemplateSource {
    File template

    FileTemplate(File template) {
        this.template = template
    }

    @Override
    Template locate(MarkupTemplateEngine engine) {
        return engine.createTemplate( this.template );
    }
}
