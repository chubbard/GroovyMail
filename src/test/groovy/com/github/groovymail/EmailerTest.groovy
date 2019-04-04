package com.github.groovymail

import spock.lang.Specification

class EmailerTest extends Specification {
    Map emailConfig = [
            "mail.debug": true,
            "mail.host": "email-smtp.us-east-1.amazonaws.com",
            "mail.port": 465,
            "mail.transport.protocol": "smtps",
            "mail.smtps.auth": true,
            "mail.smtps.from": "contact@fuseanalytics.com",
            "mail.smtps.user": "${System.getProperty("username")}",
            "mail.smtps.password": "${System.getProperty("password")}"
    ]

    def "test rendering html5 template email"() {
        setup:
        Emailer emailer = new Emailer( emailConfig )
        when:
        String content = emailer.email("sales@fuseanalytics.com", "A message for you Rudy" )
                .html("classpath:test_html5.groovy")
                .bind(message: 'Lorem ipsum dolor')
                .render()
        then:
        !content.isEmpty()
        content.contains("Lorem ipsum dolor")

        println(content)

        cleanup:
        emailer.stop()
    }

    def "test rendering simplified template email"() {
        setup:
        Emailer emailer = new Emailer( emailConfig )
        when:
        String content = emailer.email("sales@fuseanalytics.com", "Did you forget your password?" )
                .html("classpath:test_html.groovy")
                .bind(name: 'Dan')
                .render()

        then:
        !content.isEmpty()
        content.contains("<title>Hello</title>")
        content.contains("This is what will be displayed in some email clients.")
        content.contains("Dan")
        content.contains(".container {")
        content.contains(".btn-primary a {")
        content.contains("Reset Password")

        cleanup:
        emailer?.stop()
    }
}
