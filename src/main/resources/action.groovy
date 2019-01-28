table(role: "presentation", border:"0", cellpadding:"0", cellspacing:"0", class:"btn btn-primary") {
    tbody {
        tr {
            td(align: "center") {
                table(role: "presentation", border: "0", cellpadding: "0") {
                    tbody {
                        tr {
                            td {
                                a(href: actionLink, target: "_blank") {
                                    yield actionText
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}