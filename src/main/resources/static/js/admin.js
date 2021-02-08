fetch("/managers")
    .then(res => res.json())
    .then(res => res.forEach(manager => {
        let li = document.createElement('li')
        li.classList.add("nav-item")
        let div = document.createElement('div')
        div.classList.add('nav-link')
        div.innerText = manager.fullName
        div.setAttribute("data-id", manager.id)

        li.appendChild(div)
        document.querySelector("#managers").appendChild(li)
        div.addEventListener('click', function () {
            document.querySelectorAll("#people .nav-link").forEach(div => div.classList.remove("active"))
            this.classList.add("active")
            console.log(this.innerText + " " + this.getAttribute("data-id"))
        })
    }))
    .catch(e => console.log(e))

fetch("/users")
    .then(res => res.json())
    .then(res => res.forEach(manager => {
        let li = document.createElement('li')
        li.classList.add("nav-item")
        let div = document.createElement('div')
        div.classList.add('nav-link')
        div.innerText = manager.fullName
        div.setAttribute("data-id", manager.id)

        li.appendChild(div)
        document.querySelector("#users").appendChild(li)
        div.addEventListener('click', function () {
            document.querySelectorAll("#people .nav-link").forEach(div => div.classList.remove("active"))
            this.classList.add("active")
            console.log(this.innerText + " " + this.getAttribute("data-id"))
        })
    }))
    .catch(e => console.log(e))
