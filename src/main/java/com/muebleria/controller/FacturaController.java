package com.muebleria.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.muebleria.entity.Factura;
import com.muebleria.repository.FacturaRepository;
import com.muebleria.service.CarritoService;
import com.muebleria.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/factura/{id}")
    public String verFactura(@PathVariable Long id, Model model) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        model.addAttribute("factura", factura);
        return "factura";
    }

    @PostMapping("/factura/{id}/confirmar")
    public String confirmarPago(@PathVariable Long id, HttpSession session, Model model) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        factura.setEstado("pagada");
        facturaRepository.save(factura);

        Usuario usuario = factura.getUsuario();
        session.setAttribute("carrito", carritoService.obtenerCarritoPorUsuario(usuario));

        model.addAttribute("factura", factura);
        return "compra-exitosa";
    }

    @GetMapping("/factura/{id}/descargar")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        StringBuilder sb = new StringBuilder();
        sb.append("Factura #").append(factura.getId()).append("\n");
        sb.append("Cliente: ").append(factura.getUsuario().getUsername()).append("\n");
        sb.append("Fecha: ").append(factura.getFechaEmision()).append("\n");
        sb.append("Estado: ").append(factura.getEstado()).append("\n\n");
        sb.append("Detalles:\n");
        factura.getDetalles().forEach(d -> sb.append("- ")
                .append(d.getMueble().getNombre()).append(" x ")
                .append(d.getCantidad()).append(" @ ")
                .append(d.getPrecio()).append(" = ")
                .append(d.getTotal()).append("\n"));
        sb.append("\nTotal: ").append(factura.getTotal()).append("\n");

        byte[] bytes = sb.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Factura-" + factura.getId() + ".txt");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
