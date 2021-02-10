function affectProject(){
    let modal = $("#reaffect-modal")

    let activeItem = modal.find(".modal-body .active")
    if (activeItem.length === 0) return

    fetch(`/users/${$("#fullname").attr('data-id')}`, {
        method: 'PATCH',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({'project': activeItem.attr('data-id')})
    })
        .then(res => {
            if (res.ok) {
                modal.modal('hide')
                fetchUsers()
                fetchProjects()
            }
        })
        .catch(e => console.log(e))
}

function export_to_pdf(){
	fetch("/timecheck/export", {
            method: 'get'})
			.then(res => res.blob())
			.then(blob => window.open(
				URL.createObjectURL(
					new Blob([blob],{
						type:'application/pdf'
					})
			), "_self"))
      // handle request error
      .catch((err) => {console.log(err); throw err});
	}

function updateNavList(element, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.name
    div.setAttribute("data-id", element.id)
    div.setAttribute("data-project-name", element.name)


    li.appendChild(div)
    document.querySelector(domID).append(li)
    div.addEventListener('click', function () {
        document.querySelectorAll("#people .nav-link").forEach(div => div.classList.remove("active"))
        this.classList.add("active")
        let usersGestion = $("#users-gestion")
        usersGestion.children("ul,li,h4,p").remove()
        usersGestion.prepend($("<ul>", {
            id: "list-users",
            class: 'list-group',
            "data-id": this.getAttribute("data-id")
        }))
        let listUsers = $("#list-users")
        element.users.forEach(user => {
                listUsers.append($("<li>", {
                    id: "user-list-item"+user.id,
                    class: 'list-group-item'
                    })
                .text(user.fullName).append($("<ul>", {
                    id: "list-timechecks"+user.id,
                    class: 'list-group',
                }))).show()
                let ListTimechecks=$("#list-timechecks"+user.id)
                user.timeChecks.forEach(timecheck => {
                    ListTimechecks.append($("<li>", {
                        class: 'list-group-item'})
                    .text(timecheck.time)).show()
                })
            }
        )
        $("#gestionUser").hide() 
        $("#gestion").show()
    })
}
function updateNavListUser(element, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.fullName
    div.setAttribute("data-id", element.id)
    if (element.manager)
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
            "data-id": this.getAttribute("data-id")
        }).text(this.innerText)).show()
        $("#gestionUser").show()
        $("#gestion").hide()
    })
    
}

function fetchProjects() {
    $("#projects").empty()
    return fetch("/projects")
        .then(res => res.json())
        .then(res => res.forEach(project => {
            updateNavList(project, "#projects")
        }))
        .catch(e => console.log(e))
}


function fetchUsers() {
    $("#users").empty()
    return fetch("/users")
        .then(res => res.json())
        .then(res => res.forEach(user => {
            updateNavListUser(user, "#users")
        }))
        .catch(e => console.log(e))
}


$(document).ready(() => {
    fetchProjects()
    fetchUsers()

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
                password: $("#add-user-password").val()
            })
        })
            .then(res => {
                if (res.status == 201) {
                    fetchUsers()
                }
            })
            .catch(e => console.log(e))
        
    })


    $("#form-add-project").submit(e => {
        e.preventDefault();
        fetch("/projects", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: $("#add-project-name").val()
            
        })
        .then(res => {
            if (res.status == 200) {
                    fetchProjects()
            }
        })
        .catch(e => console.log(e))
    })


    $("#reaffect-modal").on("show.bs.modal", function () {
        $(this).find(".modal-body").empty()
        fetch("/projects")
            .then(res => res.json())
            .then(res => res.forEach(project => {
                $(this).find(".modal-body").append($('<li>', {
                    class: 'nav-item',
                    style: 'cursor:pointer;',
                    'data-id': project.id
                })
                    .text(project.name)
                    .click(function () {
                        $(this).siblings().removeClass("active")
                        $(this).addClass("active")
                    }))
            }))
            .catch(e => console.log(e))
    })


})
