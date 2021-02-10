function export_to_pdf(){
    fetch("/timecheck/export", {
        method: 'get'})
        .then(res => res.blob())
        .then(blob => window.open(
            URL.createObjectURL(
                new Blob([blob],{
                    type:'application/pdf'
                })
            ), "_blank"))
        // handle request error
        .catch((err) => {console.log(err); throw err});
}
