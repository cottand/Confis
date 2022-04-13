document.addEventListener("DOMContentLoaded", function() {
    let nav = document.getElementsByClassName("md-header__inner md-grid")[0];
    let button = document.createElement("div")
    button.className = "md-header__api-ref"
    let href = document.createElement("a")
    href.target= "_blank"
    let currentVersion = window.location.pathname.split("/")[2]
    href.href = "/kkafka/" + currentVersion + "/generated/index.html"
    href.title = "Go to KDocs"
    href.className = "md-api-ref"
    href.appendChild(document.createTextNode("API Reference"))
    button.appendChild(href)
    let last = nav.children[nav.children.length - 1]
    nav.insertBefore(button, last)
});
