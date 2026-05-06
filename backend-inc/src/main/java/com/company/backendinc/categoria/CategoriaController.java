package com.company.backendinc.categoria;

import com.company.backendinc.categoria.application.ListCategoriasUseCase;
import com.company.backendinc.categoria.adapter.out.CategoriaRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final ListCategoriasUseCase listCategoriasUseCase;
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(ListCategoriasUseCase listCategoriasUseCase, CategoriaRepository categoriaRepository) {
        this.listCategoriasUseCase = listCategoriasUseCase;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> list() {
        return listCategoriasUseCase.execute();
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CategoriaUpsertRequest request) {
        categoriaRepository.create(request.getNombre(), request.getAbreviatura(), request.getColorHex());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CategoriaUpsertRequest request) {
        categoriaRepository.update(id, request.getNombre(), request.getAbreviatura(), request.getColorHex());
        return ResponseEntity.noContent().build();
    }
}
