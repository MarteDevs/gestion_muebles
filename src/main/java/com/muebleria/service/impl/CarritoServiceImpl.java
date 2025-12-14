package com.muebleria.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.muebleria.entity.Carrito;
import com.muebleria.entity.Mueble;
import com.muebleria.entity.ProductoCarrito;
import com.muebleria.entity.Usuario;
import com.muebleria.repository.CarritoRepository;
import com.muebleria.repository.MuebleRepository;
import com.muebleria.repository.ProductoCarritoRepository;
import com.muebleria.service.CarritoService;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private MuebleRepository muebleRepository;

    @Autowired
    private ProductoCarritoRepository productoCarritoRepository;

    @Override
    public Carrito obtenerCarritoPorUsuario(Usuario usuario) {
        // Buscar carrito activo del usuario
        return carritoRepository.findByUsuario_IdAndActivo(usuario.getId(), true)
                .orElseGet(() -> {
                    return carritoRepository.findByUsuario_Id(usuario.getId())
                            .map(existing -> {
                                existing.setActivo(true);
                                existing.setFechaCreacion(LocalDateTime.now());
                                existing.setTotal(0);
                                productoCarritoRepository.deleteAllByCarrito(existing);
                                if (existing.getProductos() == null) {
                                    existing.setProductos(new java.util.ArrayList<>());
                                }
                                return carritoRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                Carrito nuevoCarrito = new Carrito();
                                nuevoCarrito.setUsuario(usuario);
                                nuevoCarrito.setActivo(true);
                                nuevoCarrito.setFechaCreacion(LocalDateTime.now());
                                nuevoCarrito.setTotal(0);
                                nuevoCarrito.setProductos(new java.util.ArrayList<>());
                                return carritoRepository.save(nuevoCarrito);
                            });
                });
    }

    @Override
    public void agregarProductoAlCarrito(Usuario usuario, Long productoId, int cantidad) {
        // Obtener el carrito activo del usuario
        Carrito carrito = obtenerCarritoPorUsuario(usuario);

        // Buscar el producto en el repositorio
        Mueble mueble = muebleRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar si el producto ya está en el carrito
        java.util.Optional<ProductoCarrito> existenteOpt = productoCarritoRepository.findByCarritoAndMueble(carrito, mueble);
        if (existenteOpt.isPresent()) {
            ProductoCarrito existente = existenteOpt.get();
            int nuevaCantidad = existente.getCantidad() + cantidad;
            if (nuevaCantidad <= 0) {
                carrito.getProductos().remove(existente);
                productoCarritoRepository.delete(existente);
            } else {
                existente.setCantidad(nuevaCantidad);
                productoCarritoRepository.save(existente);
            }
        } else {
            int nuevaCantidad = cantidad;
            if (nuevaCantidad > 0) {
                ProductoCarrito nuevoProducto = new ProductoCarrito();
                nuevoProducto.setCarrito(carrito);
                nuevoProducto.setMueble(mueble);
                nuevoProducto.setCantidad(nuevaCantidad);
                nuevoProducto.setPrecio(mueble.getPrecio());
                productoCarritoRepository.save(nuevoProducto);
            }
        }
    }

    @Override
    public int obtenerCantidadProductosEnCarrito(Long id) {
        return carritoRepository.findByUsuario_IdAndActivo(id, true)
                .map(c -> {
                    java.util.List<ProductoCarrito> ps = c.getProductos() != null ? c.getProductos() : java.util.Collections.emptyList();
                    return ps.stream().mapToInt(ProductoCarrito::getCantidad).sum();
                })
                .orElse(0);
    }

    @Override
    public Carrito guardarCarrito(Carrito carrito) {
        // Guardar el carrito en la base de datos
        return carritoRepository.save(carrito);
    }

    @Override
    public void eliminarProductoDelCarrito(Usuario usuario, Long productoId) {
        // Obtener el carrito activo del usuario
        Carrito carrito = obtenerCarritoPorUsuario(usuario);

        // Buscar el producto en el carrito
        Mueble mueble = muebleRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoCarrito productoCarrito = productoCarritoRepository.findByCarritoAndMueble(carrito, mueble)
                .orElseThrow(() -> new RuntimeException("Producto no está en el carrito"));


        carrito.getProductos().remove(productoCarrito); 
        // Eliminar el producto del carrito
        productoCarritoRepository.delete(productoCarrito);

        // Recalcular el total del carrito
        double nuevoTotal = carrito.getProductos().stream()
                .filter(p -> !p.getMueble().getId().equals(productoId)) // Excluir el producto eliminado
                .mapToDouble(p -> p.getPrecio() * p.getCantidad())
                .sum();

        carrito.setTotal(nuevoTotal);

        // Guardar el carrito actualizado
        carritoRepository.save(carrito);
    }

    @Override
    public void vaciarCarrito(Usuario usuario) {
        // Obtener el carrito activo del usuario
        Carrito carrito = obtenerCarritoPorUsuario(usuario);

        // Eliminar todos los productos del carrito
        productoCarritoRepository.deleteAllByCarrito(carrito);
        carrito.setTotal(0);
        carritoRepository.save(carrito);
    }

    public void recalcularTotal(Carrito carrito) {
        java.util.List<ProductoCarrito> ps = carrito.getProductos() != null ? carrito.getProductos() : java.util.Collections.emptyList();
        double total = ps.stream()
                .mapToDouble(p -> p.getCantidad() * p.getPrecio())
                .sum();
        carrito.setTotal(total);
        carritoRepository.save(carrito); // Guarda el total actualizado en la base de datos
    }

    @Override
    public double calcularTotalCarrito(Usuario usuario) {
        Carrito carrito = obtenerCarritoPorUsuario(usuario);
        java.util.List<ProductoCarrito> ps = carrito.getProductos() != null ? carrito.getProductos() : java.util.Collections.emptyList();
        return ps.stream()
                .mapToDouble(p -> p.getPrecio() * p.getCantidad())
                .sum();
    }

}
