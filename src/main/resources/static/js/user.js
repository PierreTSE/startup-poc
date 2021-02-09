function updateNavList(element, role, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.time  + " " + element.projectId
    div.setAttribute("data-role", role)
    div.setAttribute("data-id", element.id)
    div.setAttribute("data-time", element.time)

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


function export_to_pdf(){
	fetch("/TimeCheck/export", {
            method: 'get'
}) }

function fetchTimes() {
    $("#temps").empty()
    return fetch("/TimeCheck")
        .then(res => res.json())
        .then(res => res.forEach(timeCheck => {
            updateNavList(timeCheck, 'timeCheck', "#temps")
        }))
        .catch(e => console.log(e))
}

$(document).ready(() => {
    fetchTimes()
    $("#form-add-Time").submit(e => {
        e.preventDefault();
        fetch("/TimeCheck", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                ProjectId: $("#add-Project-name").val(),
                time: $("#add-Time").val(),
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
