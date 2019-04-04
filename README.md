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
the properies [JavaMail](https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html) needs.  The simplest way to configure the mailer is to add the a
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

You can change the filename used if you want to keep different versions for each environment.
You just need to pass the name to the constructor you want to use, and it will look for that
file on the classpath.

## How to send email

GroovyMail is based on the `Emailer` class.  Consider the `Emailer` something like a specific email 
server, and it can be cached global so instantiate an `Emailer` in your program, and use it to send 
email through that server.  For example:

    Emailer emailer = new Emailer()
    ...
    
    emailer.email("person@someDomain.com", Some subject")
        .html("classpath:onboard_email.groovy")
        .bind("name", "Dan")
        .send() 
        
    ...
    
    emailer.stop()

So the code above shows how the instantiation of the `Emailer` occurs at some point before you actually
send the email.  For example, that might be inside your dependency injection framework when your
container starts up, or at the beginning of your program, etc.  The `Emailer` instance is long living,
and the final line is showing how when the emailer instance is being shutdown you need to call stop() 
method to clean it up.

The middle part is how email is actually addressed and sent.  Here is the breakdown:

* **`email`** initiates an email you want to build.  It requires an email address for the to line, and
the subject of the Email.
* **`html`** This method sets the content body of the message to be text/html and specifices the template
to use to generate that content.  In this case we are loading the template off the classpath.
* **`bind`** This method is used to pass data to the template during evaulation.  You may call this method
as many times as you like to add additional data.  It takes a name and the underlying Object.  Templates
may refer to this object using the name given.
* **`send`** This method evaluates all templates, constructs the email, and sends it.  

### Template Example

In the above example we didn't show what the template looks like.  GroovyMail is based around groovy's
awesome [MarkupTemplateEngine](http://groovy-lang.org/templating.html) which gives a lot of flexibility 
for creating content.  Here is an example:

    yieldUnescaped '<!DOCTYPE html>'
    html(lang:'en') {
        head {
            meta( name: "viewport", content: "width=device-width" )
            meta( "http-equiv": "Content-Type", content: "text/html; charset=UTF-8")
        }
        body {
            p("Hi ${name}"
            p("""
                We've noticed you are interested in stuff we are doing, and we 
                love that you want to stay in the know with us.  Welcome aboard!
            """)
            p("Sincerely")
            p("Us")
        }
    }

## Other Examples

Here are a few other examples of the API for common operations:

### Asynchronous Emails

You can offload the sending of the email to a background thread using `sendAsync`:

    emailer.emal("person@someDomain", "Welcome" )
        .html("classpath:welcome.groovy")
        .bind("name", person.name )
        .sendAsync()

### Attach files:

    emailer.email("person@someDomain.com", "Report")
        .html("classpath:report.groovy")
        .attach( pdfFile )
        .bind( "data", data )
        .send()
        
### bcc, cc, from:

    emailer.email("person@someDomain.com", "Support Email")
        .bcc("support@myDomain.com", "manager@myDomain.com")
        .cc("otherPerson@someDomain.com")        
        .html("classpath:support.groovy")
        .bind("ticketId", ticket.id )
        .send("noreply@myDomain.com")
        
### Multiple content:

You can add multiple formats to your emails like sending plain text and HTML using the following:

    emailer.email("person@someDomain.com", "Welcome Aboard")
        .html("classpath:onboard_email_html.groovy")
        .text("classpath:onboard_email_text.groovy")
        .bind("user", user)
        .send()

## Templates

GroovyMail renders content using [MarkupTemplateEngine](http://groovy-lang.org/templating.html) 
for an explanation of how content is generated and the features you can use it's suggested you 
visit those pages.
                
### Locating templates

By default most templates are loaded off the classpath.  But you can also load them from directories
and external locations to your application.  You can load them from any of these:
 
* [File](https://docs.oracle.com/javase/8/docs/api/java/io/File.html)
* [URL](https://docs.oracle.com/javase/8/docs/api/java/net/URL.html)
* [Reader](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html)

The URL is simply a string representing a URL (http:, file:, classpath:, etc).

### Templates, Layouts, Default Look and Feel

A very important part of Groovy's MarkupTemplateEngine is being able to define reusable 
[layouts](http://groovy-lang.org/templating.html#_layouts).  GroovyMail ships with some
default layouts to make it easier to create responsive, pleasant looking emails.

#### html5.groovy

This is just a bare HTML5 layout.  Here is an example of how to use it:

    layout "html5.groovy", 
        title: "My Email", 
        css: "some_file.css", 
        bodyContent: contents {
            p("Looks like Snow torrow.")
            table {
                thead {
                    tr {
                        th("Day of Week")
                        th("Low")
                        th("High")
                        th("Conditions")
                    }
                }
                tbody {
                    tr {
                        td("Monday")
                        td("28F")
                        td("32F")
                        td("Snow")
                    }
                    tr {
                        td("Tuesday")
                        td("22F")
                        td("31F")
                        td("Snow")
                    }
                    tr {
                        td("Wednesday")
                        td("25F")
                        td("30F")
                        td("Cloudy")
                    }
                    tr {
                        td("Thursday")
                        td("24F")
                        td("33F")
                        td("Sunny")
                    }
                    tr {
                        td("Friday")
                        td("23F")
                        td("31F")
                        td("Sunny")
                    }
                }
            }
        } 

#### Simplified

Simplified is a series of templates and styles that helps you create modern style 
emails.

##### simplified-responsive-layout.groovy

This gives you a single action responsive layout:

    layout "simplified-responsive-layout.groovy",
        title: 'Hello',
        previewText: 'This is what will be displayed in some email clients as preview text.',
        beforeActionContent: contents {
            p("Hey there looks like you forgot something.  That's ok use the button below to reset your password.")
        },
        actionText: "Reset Password",
        actionLink: "http://www.google.com",
        afterActionContent: contents {
            p("If you didn't initiate this please alert your administrator or contact us.")
        },
        footer: contents {
            div {
                span(class: "apple-link") {
                    yield "Fruit Company Inc, 100 Infinite Loop, Cupertino CA 94102"
                }
                br()
                span("Don't like these emails?")
                a(href: "http://i.imgur.com/CScmqnj.gif") {
                    yield "Unsubscribe"
                }
            }
        }
 
 The above shows all the parameters you can pass to the layout.  Here is a breakdown of what those do:
 
 * **`title`**  The title of the email some clients display this
 * **`previewText`** This is the preview text that might be displayed in the list of emails on some clients.
 * **`beforeActionContent`** This is a template to insert before the action button
 * **`actionText`** The text displayed to the user on the action button
 * **`actionLink`** The URL the user will visit if they click on the action button
 * **`afterActionContent`** A template to insert after the action button
 * **`footer`** A template to insert at the bottom of the email after the main area.