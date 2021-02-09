
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
        document.querySelectorAll("#projects .nav-link").forEach(div => div.classList.remove("active"))
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
                    // if (projTimechecks.some((element) => element.id===timecheck.id)) {
                    ListTimechecks.append($("<li>", {
                        class: 'list-group-item'})
                    .text(timecheck.time)).show()
                })
            }
        )
        


        $("#gestion").show()
    })
}

async function fetchProjects() {
    $("#projects").empty()
    try {
        const res = await fetch("/projects")
        const res_1 = await res.json()
        return res_1.forEach(project => {
            updateNavList(project , "#projects")
        })
    } catch (e) {
        return console.log(e)
    }
}


$(document).ready(() => {
    fetchProjects()
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
                if(res.status===201) {
                    fetchUsers()
                }
            })
            .catch(e => console.log(e))
    })

    $("#form-add-project").submit(e => {
        e.preventDefault();
        fetch("/users", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: $("#add-user-firstname").val(),
            })
        })
            .then(res => {
                if (res.status == 201) {
                        fetchProjects()
                }
            })
            .catch(e => console.log(e))
    })

    // $("#reaffect-modal").on("show.bs.modal", function () {
    //     $(this).find(".modal-body").empty()
    //     fetch("/managers")
    //         .then(res => res.json())
    //         .then(res => res.forEach(manager => {
    //             $(this).find(".modal-body").append($('<li>', {
    //                 class: 'nav-item',
    //                 style: 'cursor:pointer;',
    //                 'data-id': manager.id
    //             })
    //                 .text(manager.fullName)
    //                 .click(function () {
    //                     $(this).siblings().removeClass("active")
    //                     $(this).addClass("active")
    //                 }))
    //         }))
    //         .catch(e => console.log(e))
    // })
})
