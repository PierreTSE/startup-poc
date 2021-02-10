function updateNavList(element, role, domID) {
    let li = document.createElement('li')
    li.classList.add("nav-item")
    let div = document.createElement('div')
    div.classList.add('nav-link')
    div.innerText = element.time 
    div.setAttribute("data-role", role)
    div.setAttribute("data-id", element.id)
    div.setAttribute("data-time", element.time)

    li.appendChild(div)
    document.querySelector(domID).append(li)
}

function fetchTimes() {
    $("#temps").empty()
    return fetch("/timecheck")
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
        
        //get project from name :
        let idproj;
        fetch("/projects")
            .then(res => res.json())
            .then(res => res.forEach(proj => {
                const name = JSON.stringify(proj.name);
                console.log(name);
                if (name === ($("#add-Project-name").val())) {
                    idproj = proj.id;
                    console.log(idproj)
                }
            }))
            .then(() => {
                fetch("/timecheck", {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        projectId: idproj,
                        time: $("#add-Time").val(),
                    })
                })
                    .then(res => {
                        if (res.status === 200) {
                            fetchTimes()
                        }
                    })
                    .catch(e => console.log(e))
            })
            .catch(e => console.log(e))
    })
})
