package com.github.groovymail

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine

interface TemplateSource {
    Template locate(MarkupTemplateEngine engine)
}