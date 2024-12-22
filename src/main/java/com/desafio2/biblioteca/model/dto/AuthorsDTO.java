package com.desafio2.biblioteca.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AuthorsDTO {
	
    private String name;
    private int birthYear;
    private int deathYear;
    private String title;

    public AuthorsDTO(String name, int birthYear, int deathYear, String title) {
        this.name = name;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
        this.title = title;
    }
}
