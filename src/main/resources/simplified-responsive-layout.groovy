yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
    head {
        meta( name: "viewport", content: "width=device-width" )
        meta( "http-equiv": "Content-Type", content: "text/html; charset=UTF-8")
        title( title )
        style {
            include unescaped: "simplified.css"
        }
    }
    body {
        table(role:"presentation", border:"0", cellpadding:"0", cellspacing:"0", class:"body") {
            tr {
                td("&nbsp;")
                td(class: "container") {
                    div(class: "content") {
                        span(class: "preheader") {
                            yield previewText
                        }

                        table(role: "pressentation", class: "main") {
                            tr {
                                td(class: "wrapper") {
                                    table( role: "presentation", border: "0", cellpadding: "0", cellspacing: "0") {
                                        tr {
                                            td{
                                                beforeActionContent()

                                                layout 'action.groovy', actionText: actionText, actionLink: actionLink

                                                afterActionContent()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        div( class: "footer" ) {
                            table( role: "presentation", border: "0", cellpadding: "0", cellspacing: "0" ) {
                                tr {
                                    td(class: "content-block") {
                                        footer()
                                    }
                                }
                            }
                        }
                    }
                }
                td("&nbsp;")
            }
        }
    }
}
