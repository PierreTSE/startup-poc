function fetchProjects(){
    $("#projects").empty()
    $("#add-project-id").empty()

    fetch("/projects")
        .then(res => res.json())
        .then(res => res.forEach(project => {
            $("#add-project-id").append($("<option>",{
                "value": project.id
            }).text(project.name))

            let project_ul = $('<ul>',{class:"pl-0"}).text(project.name)
            project.timeChecks.forEach(t => {
                project_ul.append($('<div>',{class:"nav-item pl-3"}).text(t.time))
            })
            $("#projects").append(project_ul)
        }))
}

$(document).ready(() => {
    fetchProjects()

    $("#form-add-Time").submit(e => {
        e.preventDefault();

        fetch("/timecheck", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                projectId: $("#add-project-id").val(),
                time: $("#add-Time").val(),
            })
        }).then(res => {
            if (res.status === 200) {
                fetchProjects()
            } else {
                console.error("Something went wrong.")
            }
        }).catch(e => console.error(e))
    })
})
