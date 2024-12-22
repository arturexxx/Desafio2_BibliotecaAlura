package com.desafio2.biblioteca.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Datoslibros(

        @JsonAlias("id") Long codigoLibro,
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<DatosAutor> autorList,
        @JsonAlias("languages") List<String> idiomas,
        @JsonAlias("download_count") Long descargas
) {

}