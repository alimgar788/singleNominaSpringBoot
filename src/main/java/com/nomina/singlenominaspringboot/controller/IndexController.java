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
     * Comprueba si se necesita iniciar sesión para acceder a una página.
     *
     * @return true si se necesita iniciar sesión, de lo contrario false.
     */
    private Boolean necesitaLogin() {
        String opcion = this.getOpcion();
        Boolean logueado = this.estaLogueado();
        return !logueado && (opcion == null || !opcion.equals("login"));
    }

    /**
     * Método que maneja la solicitud GET para la página de registro.
     *
     * @param model Model
     */
    private void doGetRegistro(Model model) {
        model.addAttribute("contenido", "content/registro.html");
    }

    /**
     * Método que maneja la solicitud GET para la lista de empleados.
     *
     * @param model Model
     */
    private void doGetListado(Model model) {
        List<Empleado> listaEmpleados = this.empleadoService.obtenerListaEmpleados();
        Boolean creacion = this.getRequestParameter("confirmar-creacion") != null;
        model.addAttribute("usuarioCreado", creacion);
        model.addAttribute("listaEmpleados", listaEmpleados);
        model.addAttribute("contenido", "content/listado-empleados.html");
    }

    /**
     * Método que maneja la solicitud GET para la consulta de empleados.
     *
     * @param model Model
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
     * Método que maneja la solicitud GET para la actualización de empleados.
     *
     * @param model Model
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
     * Redirige a la página de listado de empleados, realizando consultas según los parámetros proporcionados.
     *
     * @param model Model
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
     * Redirige a la eliminación de un empleado específico según el DNI proporcionado.
     *
     * @param dni   El DNI del empleado a eliminar.
     * @param model Model
     */
    private String redireccionaEliminar(String dni, Model model) {
        this.empleadoService.eliminarEmpleado(dni);
        return "redirect:/index?opcion=actualiza&confirmar-eliminacion=1";
    }

    /**
     * Redirige a la página de actualización de un empleado específico según el DNI proporcionado.
     *
     * @param dni   El DNI del empleado a actualizar.
     * @param model Model
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
     * Método que maneja la solicitud GET para la página de inicio de sesión.
     *
     * @param model Model
     */
    private void doGetLogin(Model model) {
        model.addAttribute("contenido", "content/login.html");
    }

    private String doGetCierraSesion() {
        HttpSession sesion = this.getSession();
        sesion.invalidate();
        return this.loginRedirect();
    }

    /**
     * Método que maneja la solicitud GET para la página de bienvenida.
     *
     * @param model Model
     */
    private void doGetBienvenida(Model model) {
        model.addAttribute("opcion", "content/bienvenida.html");
    }

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
     * Método que maneja la solicitud POST para la actualización de empleados.
     *
     * @param model Model
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
     * Método que maneja la solicitud POST para la página de inicio de sesión.
     *
     * @param model Model
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
     * Método que maneja la solicitud POST para la página por defecto.
     */
    private String doPostDefault() {
        return "redirect:/index?opcion=bienvenida";
    }


    /**
     * Comprueba si el usuario ha iniciado sesión.
     *
     * @return true si el usuario ha iniciado sesión, de lo contrario false.
     */
    private Boolean estaLogueado() {
        HttpSession sesion = this.getSession();
        String expiracion = (String) sesion.getAttribute("expiracion");
        Date actual = new Date();
        return expiracion != null && actual.before(new Date(Date.parse(expiracion)));
    }

    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }

    private HttpSession getSession() {
        return this.getHttpRequest().getSession(true);
    }

    private String loginRedirect() {
        return "redirect:/index?opcion=login";
    }

    private void estableceAtributosComunes(Model model) {
        Boolean logueado = this.estaLogueado();
        model.addAttribute("paginaActual", this.getOpcion());
        model.addAttribute("logueado", logueado);
    }

    private String getOpcion() {
        HttpServletRequest request = this.getHttpRequest();
        return request.getParameter("opcion");
    }

    private String getRequestParameter(String parameter) {
        HttpServletRequest request = this.getHttpRequest();
        return request.getParameter(parameter);
    }

    private void manejaException(Model model, String mensaje) {
        model.addAttribute("mensajeError", mensaje);
        model.addAttribute("contenido", "/exception/error.html");
    }
}
