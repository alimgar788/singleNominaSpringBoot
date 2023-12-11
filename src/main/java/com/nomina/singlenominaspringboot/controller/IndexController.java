package com.nomina.singlenominaspringboot.controller;

import com.nomina.singlenominaspringboot.model.Administrador;
import com.nomina.singlenominaspringboot.model.Empleado;
import com.nomina.singlenominaspringboot.service.AdministradorService;
import com.nomina.singlenominaspringboot.service.EmpleadoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class IndexController {

    @Autowired
    EmpleadoService empleadoService;

    @Autowired
    AdministradorService administradorService;

    /**
     * Maneja las peticiones GET en la ruta "/index". Determina las acciones a realizar según la opción proporcionada,
     * establece atributos comunes en el modelo y devuelve la vista correspondiente.
     *
     * @param model El modelo utilizado para almacenar atributos y datos a ser utilizados por la vista.
     * @return La vista correspondiente a la opción seleccionada o una vista predeterminada ("index" por defecto).
     * En caso de necesitar inicio de sesión, redirige a la página de inicio de sesión.
     * Si la opción "actualiza" redirige a una vista diferente, devuelve la vista redirigida.
     * Las opciones disponibles son: "registro", "listado", "consulta", "actualiza", "login", "cierraSesion" y "bienvenida".
     */
    @GetMapping("/index")
    public String doGet(Model model) {
        if (this.necesitaLogin()) {
            return this.loginRedirect();
        }
        String view = "index";
        this.estableceAtributosComunes(model);
        switch (this.getOpcion()) {
            case "registro":
                this.doGetRegistro(model);
                break;
            case "listado":
                this.doGetListado(model);
                break;
            case "consulta":
                this.doGetConsulta(model);
                break;
            case "actualiza":
                String redirect = this.doGetActualiza(model);
                if (redirect != null) {
                    view = redirect;
                }
                break;
            case "login":
                this.doGetLogin(model);
                break;
            case "cierraSesion":
                view = this.doGetCierraSesion();
                break;
            case "bienvenida":
            default:
                this.doGetBienvenida(model);
        }

        return view;
    }

    /**
     * Maneja las peticiones POST en la ruta "/index". Evalúa la opción proporcionada y realiza acciones correspondientes
     * en función de la misma, estableciendo atributos comunes en el modelo y devolviendo la vista resultante.
     *
     * @param model El modelo utilizado para almacenar atributos y datos a ser utilizados por la vista.
     * @return La vista correspondiente a la opción seleccionada o una vista predeterminada ("index" por defecto).
     * En caso de necesitar inicio de sesión, redirige a la página de inicio de sesión.
     * Si la opción "registro", "actualiza" o "login" redirige a una vista diferente, devuelve la vista redirigida.
     * Si la opción es diferente a las mencionadas, realiza una acción predeterminada.
     * Las opciones disponibles son: "registro", "actualiza" y "login".
     */
    @PostMapping("/index")
    public String doPost(Model model) {
        if (this.necesitaLogin()) {
            return this.loginRedirect();
        }
        String view = "index";
        String redirect = null;

        this.estableceAtributosComunes(model);
        switch (this.getOpcion()) {
            case "registro":
                redirect = this.doPostRegistro(model);
                if (redirect != null) {
                    view = redirect;
                }
                break;
            case "actualiza":
                redirect = this.doPostActualiza(model);
                if (redirect != null) {
                    view = redirect;
                }
                break;
            case "login":
                redirect = this.doPostLogin(model);
                if (redirect != null) {
                    view = redirect;
                }
                break;
            default:
                this.doPostDefault();
        }
        return view;
    }

    /**
     * Comprueba si se necesita un inicio de sesión para la opción actual.
     *
     * @return true si no está logueado y la opción actual no es "login", indicando que se requiere un inicio de sesión;
     * false si ya está logueado o si la opción actual es "login", lo que significa que no se requiere un inicio de sesión.
     */
    private Boolean necesitaLogin() {
        String opcion = this.getOpcion();
        Boolean logueado = this.estaLogueado();
        return !logueado && (opcion == null || !opcion.equals("login"));
    }

    /**
     * Prepara el modelo para la vista de registro, estableciendo el contenido a mostrar como "content/registro.html".
     *
     * @param model El modelo al que se agrega el atributo "contenido" con el valor "content/registro.html".
     */
    private void doGetRegistro(Model model) {
        model.addAttribute("contenido", "content/registro.html");
    }

    /**
     * Prepara el modelo para la vista de listado de empleados, obteniendo la lista de empleados
     * desde el servicio correspondiente y estableciendo atributos en el modelo para su visualización.
     *
     * @param model El modelo al que se agregan los atributos necesarios para la vista de listado de empleados:
     *              - "usuarioCreado": indica si se confirma la creación de un usuario (true/false).
     *              - "listaEmpleados": la lista de objetos Empleado obtenida del servicio.
     *              - "contenido": la ruta del archivo HTML que contiene el listado de empleados ("content/listado-empleados.html").
     */
    private void doGetListado(Model model) {
        List<Empleado> listaEmpleados = this.empleadoService.obtenerListaEmpleados();
        Boolean creacion = this.getRequestParameter("confirmar-creacion") != null;
        model.addAttribute("usuarioCreado", creacion);
        model.addAttribute("listaEmpleados", listaEmpleados);
        model.addAttribute("contenido", "content/listado-empleados.html");
    }

    /**
     * Prepara el modelo para la vista de consulta de salario de un empleado, basándose en el número de DNI proporcionado.
     * Si no se proporciona un DNI o se deja en blanco, muestra el buscador de DNI en la vista.
     * Si se proporciona un DNI válido, obtiene el salario correspondiente y muestra la información en la vista.
     *
     * @param model El modelo al que se agregan los atributos necesarios para la vista de consulta de salario:
     *              - Si no se proporciona un DNI o se deja en blanco:
     *              - "contenido": la ruta del archivo HTML del buscador de DNI ("buscador/buscador.html").
     *              - Si se proporciona un DNI válido:
     *              - "dni": el número de DNI proporcionado.
     *              - "salario": el salario obtenido del servicio correspondiente.
     *              - "contenido": la ruta del archivo HTML que contiene la información del salario ("content/consulta-salario.html").
     *              En caso de error al obtener el salario, se maneja la excepción y se agrega el mensaje de error al modelo.
     */
    private void doGetConsulta(Model model) {
        String dni = this.getRequestParameter("dni");
        if (dni == null || dni == "") {
            model.addAttribute("contenido", "buscador/buscador.html");
        } else {
            try {
                model.addAttribute("dni", dni);
                Double salario = this.empleadoService.obtenerSalarioPorDni(dni);
                model.addAttribute("salario", salario);
                model.addAttribute("contenido", "content/consulta-salario.html");
            } catch (RuntimeException e) {
                this.manejaException(model, e.getMessage());
            }
        }
    }

    /**
     * Maneja la lógica de redireccionamiento para la actualización o eliminación de elementos según los parámetros recibidos.
     *
     * @param model El modelo utilizado para procesar la actualización o eliminación de elementos.
     * @return null si la acción es de edición y se redirige al método correspondiente ("redireccionaActualiza"),
     * o el resultado del método "redireccionaEliminar" si la acción es de eliminación.
     * En caso contrario, redirige al listado de elementos mediante el método "redireccionaListado".
     */
    private String doGetActualiza(Model model) {
        String editar = this.getRequestParameter("editar");
        String eliminar = this.getRequestParameter("eliminar");
        if (editar != null) {
            this.redireccionaActualiza(editar, model);
        } else if (eliminar != null) {
            return this.redireccionaEliminar(eliminar, model);
        } else {
            this.redireccionaListado(model);
        }
        return null;
    }

    /**
     * Prepara el modelo para la vista de listado de empleados actualizada después de una acción de actualización o eliminación.
     *
     * @param model El modelo al que se agregan los atributos necesarios para la vista de listado actualizada:
     *              - "contenido": la ruta del archivo HTML que muestra el listado de empleados actualizado ("content/listado-actualizacion-empleados.html").
     *              - "actualizado": indica si se confirma la actualización de un empleado (true/false).
     *              - "eliminado": indica si se confirma la eliminación de un empleado (true/false).
     *              - "campo": el campo por el cual se filtrará la lista de empleados, si se especifica alguno.
     *              - "valor": el valor del campo por el cual se filtrará la lista de empleados, si se especifica alguno.
     *              - "listaEmpleados": la lista actualizada de objetos Empleado obtenida del servicio, filtrada por el campo y valor especificados.
     *              Si no se proporciona ningún campo y valor, se obtiene la lista completa de empleados.
     */
    private void redireccionaListado(Model model) {
        String campo = this.getRequestParameter("campo");
        String valor = this.getRequestParameter("valor");
        String actualizado = this.getRequestParameter("confirmar-actualizacion");
        String eliminado = this.getRequestParameter("confirmar-eliminacion");
        List<Empleado> listaEmpleados = null;
        model.addAttribute("contenido", "content/listado-actualizacion-empleados.html");
        model.addAttribute("actualizado", actualizado != null);
        model.addAttribute("eliminado", eliminado != null);
        model.addAttribute("campo", campo != null ? campo : "");
        model.addAttribute("valor", valor != null ? valor : "");
        listaEmpleados = this.empleadoService.obtenerListaEmpleados(campo, valor);
        model.addAttribute("listaEmpleados", listaEmpleados);
    }

    /**
     * Elimina un empleado identificado por su número de DNI y redirige a la página de actualización con una confirmación de eliminación.
     *
     * @param dni   El número de DNI del empleado a eliminar.
     * @param model El modelo utilizado para procesar la eliminación y redirigir a la confirmación.
     * @return La redirección a la página de actualización con un indicador de confirmación de eliminación.
     */
    private String redireccionaEliminar(String dni, Model model) {
        this.empleadoService.eliminarEmpleado(dni);
        return "redirect:/index?opcion=actualiza&confirmar-eliminacion=1";
    }

    /**
     * Prepara el modelo para la actualización de datos de un empleado identificado por su número de DNI.
     * Obtiene la información del empleado por su DNI y agrega los atributos necesarios al modelo para cargar el formulario de actualización.
     *
     * @param dni   El número de DNI del empleado cuyos datos se van a actualizar.
     * @param model El modelo al que se agregan los atributos necesarios para el formulario de actualización:
     *              - "editar": el número de DNI del empleado a editar.
     *              - "empleado": los datos del empleado obtenidos del servicio o un objeto Empleado vacío si no se encuentra.
     *              - "contenido": la ruta del archivo HTML que contiene el formulario de actualización ("content/formulario-actualizar.html").
     */
    private void redireccionaActualiza(String dni, Model model) {
        Optional<Empleado> result = this.empleadoService.obtenerEmpleadoPorDni(dni);
        model.addAttribute("editar", dni);
        Empleado empl = new Empleado();
        if (!result.isEmpty()) {
            empl = result.get();
        }
        model.addAttribute("empleado", empl);
        model.addAttribute("contenido", "content/formulario-actualizar.html");
    }

    /**
     * Prepara el modelo para la vista de inicio de sesión.
     *
     * @param model El modelo al que se agrega el atributo "contenido" con la ruta del archivo HTML de inicio de sesión ("content/login.html").
     */
    private void doGetLogin(Model model) {
        model.addAttribute("contenido", "content/login.html");
    }

    /**
     * Cierra la sesión actual del usuario y redirige a la página de inicio de sesión.
     *
     * @return La redirección a la página de inicio de sesión después de invalidar la sesión actual.
     */
    private String doGetCierraSesion() {
        HttpSession sesion = this.getSession();
        sesion.invalidate();
        return this.loginRedirect();
    }

    /**
     * Prepara el modelo para la vista de bienvenida.
     *
     * @param model El modelo al que se agrega el atributo "opcion" con la ruta del archivo HTML de bienvenida ("content/bienvenida.html").
     */
    private void doGetBienvenida(Model model) {
        model.addAttribute("opcion", "content/bienvenida.html");
    }

    /**
     * Procesa la solicitud POST para registrar un nuevo empleado con la información proporcionada.
     *
     * @param model El modelo utilizado para procesar el registro del empleado y redirigir según el resultado.
     * @return Una redirección a la página de listado de empleados con un indicador de confirmación de creación,
     *         o maneja una excepción mostrando un mensaje de error en el modelo en caso de problemas durante el registro.
     */

    private String doPostRegistro(Model model) {
        String nombreRegistro = this.getRequestParameter("nombre");
        String dniRegistro = this.getRequestParameter("dni");
        String sexoRegistro = this.getRequestParameter("sexo");
        int categoriaRegistro = Integer.parseInt(this.getRequestParameter("categoria"));
        double anyosRegistro = Double.parseDouble(this.getRequestParameter("anyos"));
        String redireccion = null;
        try {
            Empleado empl = new Empleado(nombreRegistro, dniRegistro, sexoRegistro, categoriaRegistro, anyosRegistro);
            Empleado empleadoRegistrado = this.empleadoService.registraEmpleado(empl);
            if (empleadoRegistrado != null) {
                redireccion = "redirect:/index?opcion=listado&confirmar-creacion=1";
            } else {
                this.manejaException(model, "Error al registrar el empleado en la base de datos");
            }
        } catch (RuntimeException e) {
            this.manejaException(model, "Error al registrar el empleado en la base de datos: " + e.getMessage());
        } finally {
            return redireccion;
        }


    }

    /**
     * Procesa la solicitud POST para actualizar los datos de un empleado con la información proporcionada.
     *
     * @param model El modelo utilizado para procesar la actualización del empleado y redirigir según el resultado.
     * @return Una redirección a la página de actualización con un indicador de confirmación de actualización,
     *         o maneja una excepción mostrando un mensaje de error en el modelo en caso de problemas durante la actualización.
     */
    private String doPostActualiza(Model model) {
        String dniOriginal = this.getRequestParameter("editar");
        String nombre = this.getRequestParameter("nombre");
        String dni = this.getRequestParameter("dni");
        String sexo = this.getRequestParameter("sexo");
        String categoria = this.getRequestParameter("categoria");
        String anyos = this.getRequestParameter("anyos");

        String redireccion = null;

        try {
            Optional<Empleado> result = this.empleadoService.obtenerEmpleadoPorDni(dniOriginal);
            Empleado empleadoActualizado = null;
            if (!result.isEmpty()) {
                Empleado empl = result.get();

                empl.setNombre(nombre);
                empl.setSexo(sexo);
                empl.setCategoria(Integer.parseInt(categoria));
                empl.setAnyos(Double.parseDouble(anyos));
                empl.setDni(dni);
                empleadoActualizado = this.empleadoService.actualizaEmpleado(empl);
            }

            if (empleadoActualizado != null) {
                redireccion = "redirect:/index?opcion=actualiza&confirmar-actualizacion=1";
            } else {
                this.manejaException(model, "Error al actualizar el empleado en la base de datos");
            }
        } catch (RuntimeException e) {
            this.manejaException(model, "Error al actualizar el empleado en la base de datos: " + e.getMessage());
        } finally {
            return redireccion;
        }
    }

    /**
     * Procesa la solicitud POST para iniciar sesión de un administrador con las credenciales proporcionadas.
     *
     * @param model El modelo utilizado para el inicio de sesión del administrador y redirigir según el resultado.
     * @return Una redirección a la página de bienvenida si el inicio de sesión es exitoso,
     *         o invalida la sesión si las credenciales no son válidas.
     */
    private String doPostLogin(Model model) {
        HttpSession sesion = this.getSession();
        String dniLogin = this.getRequestParameter("dni");
        String email = this.getRequestParameter("email");
        String contrasenya = this.getRequestParameter("contrasenya");

        Administrador admin = new Administrador(dniLogin, email, contrasenya);

        Optional<Administrador> result = this.administradorService.accederSesion(admin);
        if (!result.isEmpty()) {
            sesion.setMaxInactiveInterval(900);
            Date expiracion = new Date(new Date().getTime() + sesion.getMaxInactiveInterval() * 1000);
            sesion.setAttribute("expiracion", expiracion.toGMTString());
        } else {
            sesion.invalidate();
        }
        return "redirect:/index?opcion=bienvenida";
    }

    /**
     * Maneja una solicitud POST por defecto y redirige a la página de bienvenida.
     *
     * @return Una redirección a la página de bienvenida por defecto.
     */
    private String doPostDefault() {
        return "redirect:/index?opcion=bienvenida";
    }

    /**
     * Verifica si el usuario está actualmente logueado comprobando la sesión y su tiempo de expiración.
     *
     * @return true si hay una sesión activa y no ha expirado; false en caso contrario.
     */
    private Boolean estaLogueado() {
        HttpSession sesion = this.getSession();
        String expiracion = (String) sesion.getAttribute("expiracion");
        Date actual = new Date();
        return expiracion != null && actual.before(new Date(Date.parse(expiracion)));
    }

    /**
     * Obtiene el objeto HttpServletRequest actual asociado a la solicitud en curso.
     *
     * @return El objeto HttpServletRequest asociado a la solicitud actual.
     */
    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    /**
     * Obtiene la sesión HTTP actual asociada a la solicitud en curso o crea una nueva si no existe.
     *
     * @return El objeto HttpSession asociado a la solicitud actual o una nueva sesión si no existe.
     */
    private HttpSession getSession() {
        return this.getHttpRequest().getSession(true);
    }

    /**
     * Genera una redirección a la página de inicio de sesión ("/index?opcion=login").
     *
     * @return Una cadena que representa una redirección a la página de inicio de sesión.
     */
    private String loginRedirect() {
        return "redirect:/index?opcion=login";
    }

    /**
     * Establece los atributos comunes en el modelo para su uso en las vistas, como la página actual y el estado de login.
     *
     * @param model El modelo al que se agregan los atributos comunes:
     *              - "paginaActual": la opción actual obtenida del contexto.
     *              - "logueado": indica si el usuario está logueado o no.
     */
    private void estableceAtributosComunes(Model model) {
        Boolean logueado = this.estaLogueado();
        model.addAttribute("paginaActual", this.getOpcion());
        model.addAttribute("logueado", logueado);
    }

    /**
     * Obtiene el parámetro "opcion" de la solicitud actual, si está presente.
     *
     * @return El valor del parámetro "opcion" de la solicitud HTTP actual, o null si no está presente.
     */
    private String getOpcion() {
        HttpServletRequest request = this.getHttpRequest();
        return request.getParameter("opcion");
    }

    /**
     * Obtiene el valor de un parámetro específico de la solicitud HTTP actual.
     *
     * @param parameter El nombre del parámetro que se desea obtener.
     * @return El valor del parámetro especificado en la solicitud HTTP actual, o null si no está presente.
     */
    private String getRequestParameter(String parameter) {
        HttpServletRequest request = this.getHttpRequest();
        return request.getParameter(parameter);
    }
    /**
     * Maneja una excepción estableciendo un mensaje de error y redirigiendo a la vista de manejo de excepciones.
     *
     * @param model   El modelo al que se agrega el mensaje de error.
     * @param mensaje El mensaje de error a ser mostrado.
     */
    private void manejaException(Model model, String mensaje) {
        model.addAttribute("mensajeError", mensaje);
        model.addAttribute("contenido", "/exception/error.html");
    }
}
