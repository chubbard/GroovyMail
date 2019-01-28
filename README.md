# GroovyMail

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

GroovyMail aims to replace using JavaMail as the API to send email in Groovy by building a 
domain specific language (DSL) on top of JavaMail to make sending email simpler.  GroovyMail 
combines Groovy Templates and MarkupEngine to make sending rich email much easiser that using 
JavaMail alone.

## Installation

For Gradle:

     compile group: 'com.github.chubbard', name: 'groovymail', version: '0.1.0'

For Maven:

      <dependency>
        <groupId>com.github.chubbard</groupId>
        <artifactId>groovymail</artifactId>
        <version>0.1.0</version>
      </dependency>

## Setup

In order for GroovyMail to work you need to setup the email configuration.  This is just
the properies JavaMail needs.  The simplest way to configure the mailer is to add the a
properties file to your classpath named "emailer.properties".  Here is an example:

    mail.host=email.mydomain.com
    mail.port=25
    mail.transport.protocol=smtp
    mail.smtp.auth=true
    mail.smtp.from=noreply@mydomain.com
    mail.smtp.user=someUser
    mail.smtp.password=somePassword

Or with SMTPS

    mail.host=email.mydomain.com
    mail.port=465
    mail.transport.protocol=smtps
    mail.smtps.auth=true
    mail.smtps.from=noreply@mydomain.com
    mail.smtps.user=someUser
    mail.smtps.password=somePassword

## How to send email

GroovyMail is based on the `Emailer` class.  Consider the `Emailer` something like a specific email 
server, and it can be cached global so instantiate an `Emailer` in your program, and use it to send 
email through that server.  For example:

    Emailer emailer = new Emailer()
    ...
    
    emailer.email("person@someDomain.com", Some subject")
        .html("classpath:my_email_template.groovy")
        .bind("name", "Dan")
        .send() 
        
    ...
    
    emailer.stop()

So the code above shows how the instantiation of the `Emailer` occurs at some point before you actually
send the email.  For example, that might be inside your dependency injection framework when your
container starts up, or at the beginning of your program, etc.  The `Emailer` instance is long living,
and the final line is showing how when the emailer instance is being shutdown you need to call stop() 
method to clean it up.

The middle part is how email is actually addressed and sent.  To initiate an email you call the `email` 
method to start an email.  You must specify the email address in the to line, and the subject.  After that
you can use the optional methods to further customize your email.  In this example `html` method is used 
to set body content type to html and specify the template to use to generate that body content.  The next 
method `bind` to used to pass data to the template.  You can invoke that method as many times as you want
to include other data.  Finally `send` method is used to evaluate the template and send the email to
the recipient(s).

## The Template

In the above example we didn't show what the template looks like.  GroovyMail is bsed around groovy's
awesome [MarkupTemplateEngine](http://groovy-lang.org/templating.html) which gives a lot of flexibility for creating content.  Here is an example:

    yieldUnescaped '<!DOCTYPE html>'
    html(lang:'en') {
        head {
            meta( name: "viewport", content: "width=device-width" )
            meta( "http-equiv": "Content-Type", content: "text/html; charset=UTF-8")
        }
        body {
            p("Hi ${name}"
            p {
            yield """
                    We've noticed you are interested in stuff we are doing, and we love that you want to
                    stay in the know with us.  Welcome aboard
                """
            }
            p {
                yield """
                    Sincerely                    
                """
                br()
                yield "Us"
            }
        }
    }
 
### Locating templates

By default most templates are loaded off the classpath.  But you can also load them from directories
and external locations to your application.