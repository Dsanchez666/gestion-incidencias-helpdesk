package com.company.backendinc.categoria;

import com.company.backendinc.categoria.application.ListCategoriasUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final ListCategoriasUseCase listCategoriasUseCase;

    public CategoriaController(ListCategoriasUseCase listCategoriasUseCase) {
        this.listCategoriasUseCase = listCategoriasUseCase;
    }

    @GetMapping
    public List<Categoria> list() {
        return listCategoriasUseCase.execute();
    }
}
