function updateNavList(element, role, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.fullName
    div.setAttribute("data-role", role)
    div.setAttribute("data-id", element.id)
    div.setAttribute("data-manager-fullname", element.manager.fullName)

    li.appendChild(div)
    document.querySelector(domID).append(li)
    div.addEventListener('click', function () {
        document.querySelectorAll("#people .nav-link").forEach(div => div.classList.remove("active"))
        this.classList.add("active")
        let usersGestion = $("#users-gestion")
        usersGestion.children("h4,p").remove()
        usersGestion.prepend($("<h4>", {
            id: "fullname",
            class: 'mb-3',
            "data-role": this.getAttribute("data-role"),
            "data-id": this.getAttribute("data-id")
        }).text(this.innerText)).show()
            $("#fullname").after($('<p>', {
                class: 'mb-3 ml-5',
            }).text("Managé par : " + this.getAttribute("data-manager-fullname")))
            // $("#btn-reaffect").show()
            // $("#btn-promote-admin").show()
            // $("#btn-promote-manager").show()
        $("#users-gestion").show()
    })
}

async function fetchUsers() {
    $("#users").empty()
    try {
        const res = await fetch("/users")
        const res_1 = await res.json()
        return res_1.forEach(user => {
            updateNavList(user, 'user', "#users")
        })
    } catch (e) {
        return console.log(e)
    }
}


$(document).ready(() => {
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
                firstname: $("#add-user-firstname").val(),
                lastname: $("#add-user-lastname").val(),
            })
        })
            .then(res => {
                if (res.status == 201) {
                        fetchUsers()
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
