function confirmarEliminacion(event) {
    if (!confirm('Está seguro que desea eliminar este empleado ?')) {
        event.preventDefault();
        return false;
    }
}

function volver(event) {
    event.preventDefault();
    window.history.back();
}