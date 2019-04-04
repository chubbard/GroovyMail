layout "simplified-responsive-layout.groovy",
    title: 'Hello',
    previewText: 'This is what will be displayed in some email clients.',
    beforeActionContent: contents {
        p("Hey ${name} looks like you forgot something.  That's ok use the button below to reset your password.")
    },
    actionText: "Reset Password",
    actionLink: "http://www.google.com",
    afterActionContent: contents {
        p("If you didn't initiate this please alert your administrator or contact us.")
    },
    footer: contents {
        div {
            span(class: "apple-link") {
                yield "Company Inc, 3 Abbey Road, San Francisco CA 94102"
            }
            br()
            span("Don't like these emails?")
            a(href: "http://i.imgur.com/CScmqnj.gif") {
                yield "Unsubscribe"
            }
        }
    }

