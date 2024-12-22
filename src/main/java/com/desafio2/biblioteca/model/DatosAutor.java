package com.desafio2.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DatosAutor(

        @JsonAlias("name") String nombreAutor,
        @JsonAlias("birth_year") String FechaNacimiento,
        @JsonAlias("death_year") String FechaDeMuerte
) {
}
