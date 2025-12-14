package com.muebleria.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.muebleria.entity.Mueble;
import com.muebleria.repository.MuebleRepository;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private MuebleRepository muebleRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (muebleRepository.count() == 0) {
            Mueble m1 = new Mueble();
            m1.setNombre("Mueble de Pino");
            m1.setDescripcion("Madera de pino tratada");
            m1.setPrecio(500.0);
            m1.setImagen("/img/muebles/mueble1.png");

            Mueble m2 = new Mueble();
            m2.setNombre("Mueble de Nogal");
            m2.setDescripcion("Acabado en nogal americano");
            m2.setPrecio(1200.0);
            m2.setImagen("/img/muebles/mueble2.png");

            Mueble m3 = new Mueble();
            m3.setNombre("Mesa de Roble");
            m3.setDescripcion("Roble macizo premium");
            m3.setPrecio(1500.0);
            m3.setImagen("/img/muebles/mueble3.png");

            Mueble m4 = new Mueble();
            m4.setNombre("Silla de Cedro");
            m4.setDescripcion("Cedro con tapizado");
            m4.setPrecio(800.0);
            m4.setImagen("/img/muebles/mueble4.png");

            muebleRepository.saveAll(Arrays.asList(m1, m2, m3, m4));
        }
    }
}
