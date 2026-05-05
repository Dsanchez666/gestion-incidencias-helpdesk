package com.company.backendinc.categoria.application;

import com.company.backendinc.categoria.Categoria;
import com.company.backendinc.categoria.adapter.out.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListCategoriasUseCase {
    private final CategoriaRepository categoriaRepository;

    public ListCategoriasUseCase(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> execute() {
        return categoriaRepository.list();
    }
}
