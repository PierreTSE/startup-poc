function reaffect() {
    let modal = $("#reaffect-modal")

    let activeItem = modal.find(".modal-body .active")
    if (activeItem.length === 0) return

    fetch(`/users/${$("#fullname").attr('data-id')}`, {
        method: 'PATCH',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({'manager': activeItem.attr('data-id')})
    })
        .then(res => {
            if (res.ok) {
                modal.modal('hide')
                fetchUsers().then(() => {
                        $("#users").find(`[data-id=${$("#fullname").attr("data-id")}]`).click()
                    }
                )
            }
        })
        .catch(e => console.error(e))
}

function promote(role, endpoint) {
    fetch(`/${endpoint}/${$("#fullname").attr('data-id')}`, {
        method: 'PATCH',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({'status': role})
    })
        .then(res => {
            if (res.ok) {
                fetchUsers()
                fetchManagers().then($("#gestion").hide())
            } else if (res.status === 500 && $("#fullname").attr('data-role') === 'user') {
                alert("Le rôle de cet utilisateur ne peut pas être modifié car des Projets ou des Pointages lui sont associés.")
            }
        })
        .catch(e => console.error(e))
}

function promoteToManager() {
    promote('Manager', 'users')
}

function updateNavList(element, role, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.fullName
    div.setAttribute("data-role", role)
    div.setAttribute("data-id", element.id)
    if (role === 'user' && element.manager)
        div.setAttribute("data-manager-fullname", element.manager.fullName)

    li.appendChild(div)
    document.querySelector(domID).append(li)
    div.addEventListener('click', function () {
        document.querySelectorAll("#people .nav-link").forEach(div => div.classList.remove("active"))
        this.classList.add("active")
        let buttons = $("#buttons")
        buttons.children("h4,p").remove()
        buttons.prepend($("<h4>", {
            id: "fullname",
            class: 'mb-3',
            "data-role": this.getAttribute("data-role"),
            "data-id": this.getAttribute("data-id")
        }).text(this.innerText)).show()
        switch (this.getAttribute("data-role")) {
            case 'manager':
                $("#btn-reaffect").hide()
                $("#btn-promote-admin").show()
                $("#btn-promote-manager").hide()
                break;
            case 'user':
                if (this.getAttribute("data-manager-fullname") !== null)
                    $("#fullname").after($('<p>', {
                        class: 'mb-3 ml-5',
                    }).text("Managé par : " + this.getAttribute("data-manager-fullname")))
                $("#btn-reaffect").show()
                $("#btn-promote-admin").show()
                $("#btn-promote-manager").show()
                break;
        }
        $("#gestion").show()
    })
}

function fetchManagers() {
    $("#managers").empty()
    return fetch("/managers")
        .then(res => res.json())
        .then(res => res.forEach(manager => {
            updateNavList(manager, 'manager', "#managers")
        }))
        .catch(e => console.error(e))
}

function fetchUsers() {
    $("#users").empty()
    return fetch("/users")
        .then(res => res.json())
        .then(res => res.forEach(user => {
            updateNavList(user, 'user', "#users")
        }))
        .catch(e => console.error(e))
}

$(document).ready(() => {
    fetchManagers()
    fetchUsers()

    $("#form-add-manager").submit(e => {
        e.preventDefault();
        fetch("/managers", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                manager: {
                    firstname: $("#add-manager-firstname").val(),
                    lastname: $("#add-manager-lastname").val()
                }, password: $("#add-manager-password").val()
            })
        })
            .then(res => {
                if (res.status === 201)
                    fetchManagers()
            })
            .catch(e => console.error(e))
    })

    $("#form-add-user").submit(e => {
        e.preventDefault();
        fetch("/users", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                user: {
                    firstname: $("#add-user-firstname").val(),
                    lastname: $("#add-user-lastname").val()
                },
                managerID: $("#add-user-manager-id").val(),
                password: $("#add-user-password").val()
            })
        })
            .then(res => {
                switch (res.status) {
                    case 201:
                        fetchUsers()
                        break
                    case 404:
                        alert('Le manager indiqué n\'existe pas')
                        break
                }
            })
            .catch(e => console.error(e))
    })

    $("#reaffect-modal").on("show.bs.modal", function () {
        $(this).find(".modal-body").empty()
        fetch("/managers")
            .then(res => res.json())
            .then(res => res.forEach(manager => {
                $(this).find(".modal-body").append($('<li>', {
                    class: 'nav-item',
                    style: 'cursor:pointer;',
                    'data-id': manager.id
                })
                    .text(manager.fullName)
                    .click(function () {
                        $(this).siblings().removeClass("active")
                        $(this).addClass("active")
                    }))
            }))
            .catch(e => console.error(e))
    })

    $("#btn-promote-admin").click(e => {
        e.preventDefault()
        promote("Admin", $("#fullname").attr("data-role") + 's')
    })
})
