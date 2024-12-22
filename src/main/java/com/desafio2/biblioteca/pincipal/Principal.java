package com.desafio2.biblioteca.pincipal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.desafio2.biblioteca.model.Author;
import com.desafio2.biblioteca.model.Books;
import com.desafio2.biblioteca.model.dto.BookDTO;
import com.desafio2.biblioteca.repository.AuthorRepository;
import com.desafio2.biblioteca.repository.BooksRepository;
import com.desafio2.biblioteca.service.ConsumoAPI;
import com.desafio2.biblioteca.service.ConvierteDatos;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class Principal {
	
	@Autowired
	private BooksRepository booksRepository;
	
	@Autowired
	private AuthorRepository authorRepository;
	
	private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<Books> books = new ArrayList<>();
    private final String URL_BASE_BOOKS ="https://gutendex.com/books/";
    private final String URL_BASE = "https://gutendex.com/books/?search=";
	
	public void muestraElMenu() {
		var opcion = -1;
		while (opcion != 0) {
			try {
	        var menu = """
	                1 - Buscar libro por titulo 
	                2 - Buscar libro registrado por nombre de Autor
	                3 - Listar libros Registrados
	                4 - Listar autores Registrados
	                5 - Listar autores vivos en un determinado año
	                6 - Listar libros por idioma
	                              
	                0 - Salir
	                Seleccione una opción, ingresando un numero del menu:
	                """;
	        System.out.println(menu);
	        opcion = teclado.nextInt();
	        teclado.nextLine();
	        switch (opcion) {
	            case 1:
	            	findAndSaveBook();
	            	break;
	            case 2:
	            	buscarponombre();
	            	break;
	            case 3:
	            	listAllBooks();
	            	break;
	            case 4:
	            	listAllAuthors();
	            	break;
	            case 5:
	            	listarAUnaFechaDeterminada();
	            	break;
	            case 6:
	            	verLibrosporLengua();
	            	break;
	            case 0:
	            	System.out.println("Saliendo...");
	            	break;
	            default:
	            	System.out.println("Opción inválida.");
	        	}		
		} catch (InputMismatchException e) {
	        System.out.println("Error: Debes ingresar un número entero.");
	        teclado.nextLine();  // Limpiar el buffer para que el programa no quede atrapado
	    }
	 }
}

	private void findAndSaveBook() {
	    System.out.println("Ingrese el nombre del libro que desea buscar:");
	    var nombreLibro = teclado.nextLine();
	    var url = URL_BASE + nombreLibro.replace(" ", "%20");
	    
	    var json = consumoApi.obtenerDatos(url);
	    ConvierteDatos convierteDatos = new ConvierteDatos();
	    var rootNode = convierteDatos.obtenerDatos(json, JsonNode.class);
	    var results = rootNode.get("results");

	    if (results.isArray() && results.size() > 0) {
	        JsonNode firstResult = results.get(0);

	        Books book = createBookFromJson(firstResult);
	        if (booksRepository.existsById(book.getExternalId())) {
	        	printBookDetails(book);
	        }else{
	        	booksRepository.save(book);
		        printBookDetails(book);
		        pauseExecution(5000); // Pausa de 3 segundos
	        }
	    } else {
	        System.out.println("No se encontraron libros para ese título.");
	    }
	}
	
	private Books createBookFromJson(JsonNode bookNode) {
	    Books book = new Books();
	    book.setId(bookNode.get("id").asLong());
	    book.setExternalId(bookNode.get("id").asLong());
	    book.setTitle(bookNode.get("title").asText());
	    book.setLanguages(processLanguages(bookNode.get("languages")));
	    book.setDownloadCount(bookNode.get("download_count").asLong());
	    book.setAuthor_book(processAuthors(bookNode.get("authors")));
	    return book;
	}

	private List<Author> processAuthors(JsonNode authorsNode) {
	    List<Author> authors = new ArrayList<>();
	    if (authorsNode != null && authorsNode.isArray()) {
	        for (JsonNode authorNode : authorsNode) {
	            Author author = new Author();
	            author.setName(authorNode.get("name").asText());
	            if (authorNode.has("birth_year")) {
	                author.setBirthYear(authorNode.get("birth_year").asInt());
	            }
	            if (authorNode.has("death_year")) {
	                author.setDeathYear(authorNode.get("death_year").asInt());
	            }
	            authors.add(author);
	        }
	    }
	    return authors;
	}

	private List<String> processLanguages(JsonNode languagesNode) {
	    List<String> languages = new ArrayList<>();
	    if (languagesNode != null && languagesNode.isArray()) {
	        for (JsonNode languageNode : languagesNode) {
	            languages.add(languageNode.asText());
	        }
	    }
	    return languages;
	}

	private void printBookDetails(Books book) {
	    System.out.println("------- LIBRO -------");
	    System.out.println("Título: " + book.getTitle());
	    if (!book.getAuthor_book().isEmpty()) {
	        System.out.println("Autor: " + book.getAuthor_book().get(0).getName());
	    }
	    if (!book.getLanguages().isEmpty()) {
	        System.out.println("Idioma: " + book.getLanguages());
	    }
	    System.out.println("Número de Descargas: " + book.getDownloadCount());
	    System.out.println("----------------------");
	}

	private void pauseExecution(int millis) {
	    try {
	        Thread.sleep(millis);
	        System.out.println();
	        System.out.println("---------------------------------");
	    } catch (InterruptedException e) {
	        System.err.println("La pausa fue interrumpida: " + e.getMessage());
	    }
	}
	
	private void pauseExecutionOp3(int millis) {
	    try {
	        Thread.sleep(millis);
	        System.out.println();
	        System.out.println("---------------------------------");
	    } catch (InterruptedException e) {
	        System.err.println("La pausa fue interrumpida: " + e.getMessage());
	    }
	}

	private void listAllBooks() {
	    List<BookDTO> books = booksRepository.findBookDetails();

	    if (books.isEmpty()) {
	        System.out.println("No hay libros registrados en la base de datos.");
	        return;
	    }

	    System.out.println("------- LISTA DE LIBROS -------");
	    for (BookDTO book : books) {
	        System.out.println("Título: " + book.getTitle());
	        System.out.println("Autores: " + String.join(", ", book.getAuthors()));
	        System.out.println("Idiomas: " + String.join(", ", book.getLanguages()));
	        System.out.println("Número de Descargas: " + book.getDownloadCount());
	        System.out.println("---------------------------------");
	        pauseExecution(2000);
	    }
	}
	
	private void listAllAuthors() {
		
		List<Object[]> results = authorRepository.findAuthorsWithBooks();
		if(results.size() == 0) {
			System.out.println("No hay libros registrados en la base de datos.");
	        return;
		}
		results.forEach(row -> {
		    List<String> rowData = Arrays.stream(row)
		                                 .map(Object::toString)
		                                 .toList();
		    
		    String birthYear = rowData.get(0);
		    String author = rowData.get(1).replace("|", "");
		    String deathYear = rowData.get(2);
		    String books = String.join(", ", rowData.subList(3, rowData.size()));
		    pauseExecutionOp3(2000);
		    System.out.println("Autor: " + author);
		    System.out.println("Fecha de Nacimiento: " + birthYear);
		    System.out.println("Fecha de Fallecimiento: " + deathYear);
		    System.out.println("Libros: [" +books+"]");
		});
		System.out.println();
	}

	private void buscarponombre() {
		System.out.println("Ingrese el nombre del [Autor] registrado que desee buscar:");
	    var nombreautor = teclado.nextLine();
	    String parametroBusqueda = "%" + nombreautor + "%";
	    List<Object[]> results = authorRepository.findAuthorsNameBooks(parametroBusqueda);
	    if(results.size() == 0) {
			System.out.println("No hay libros registrados en la base de datos, con el nombre buscado.");
			System.out.println();
	        return;
		}
		results.forEach(row -> {
		    List<String> rowData = Arrays.stream(row)
		                                 .map(Object::toString)
		                                 .toList();
		    
		    String birthYear = rowData.get(0);
		    String author = rowData.get(1).replace("|", "");
		    String deathYear = rowData.get(2);
		    String books = String.join(", ", rowData.subList(3, rowData.size()));
		    pauseExecutionOp3(2000);
		    System.out.println("Autor: " + author);
		    System.out.println("Fecha de Nacimiento: " + birthYear);
		    System.out.println("Fecha de Fallecimiento: " + deathYear);
		    System.out.println("Libros: [" +books+"]");
		});
		System.out.println();
	}
	
	private void listarAUnaFechaDeterminada() {
		System.out.println("Ingrese el año para buscar autores vivos en los registros: ");
		int fecha = teclado.nextInt();
      	teclado.nextLine();
		LocalDate fechaBusqueda = LocalDate.of(fecha, 1,1);
		List<Object[]> autoresVivos = authorRepository.findLivingAuthorsByDate(fecha);
	
		if(autoresVivos.size() == 0) {
			System.out.println("No hay libros registrados en la base de datos.");
	        return;
		}
		System.out.println();
		System.out.println("Autores vivos encontrados:");
		System.out.println();
		for (Object[] autor : autoresVivos) {
	        // Descomponer los elementos del arreglo
	        Integer birthYear = (Integer) autor[0];
	        String name = (String) autor[1];
	        Integer deathYear = (Integer) autor[2];
	        String books = (String) autor[3];

	        // Imprimir cada autor
	        System.out.println("Nombre: " + name);
	        System.out.println("Año de nacimiento: " + birthYear);
	        System.out.println("Año de fallecimiento: " + (deathYear != null ? deathYear : "Vivo"));
	        System.out.println("Libros escritos: " + books);
	        System.out.println("-------------------------------");
	    }
		
//		
//        System.out.println("Ingrese el año para buscar autores vivos en los registros: ");
//        int fecha = teclado.nextInt();
//        teclado.nextLine();
//        var jsonautor = consumoApi.obtenerDatos(URL_BASE_BOOKS);
//        var datosAutor = conversor.obtenerDatos(jsonautor, Datos.class);
//        LocalDate fechaBusqueda = LocalDate.of(fecha, 1,1);
//        Map<String, List<String>> autoresConLibros = datosAutor.resultados().stream()
//                .flatMap(libro -> libro.autorList().stream()
//                        .filter(autor -> autor.FechaNacimiento() != null && autor.FechaDeMuerte() != null)
//                        .filter(autor -> {
//                            int anioNacimiento = Integer.parseInt(autor.FechaNacimiento());
//                            int anioMuerte = Integer.parseInt(autor.FechaDeMuerte());
//                            return anioNacimiento <= fecha && anioMuerte >= fecha;
//                        })
//                        .map(autor -> Map.entry(autor.nombreAutor(), libro.titulo())) // Referencias corregidas
//                )
//                .collect(Collectors.groupingBy(
//                        Map.Entry::getKey,                       // Agrupar por autor
//                        Collectors.mapping(Map.Entry::getValue, // Extraer títulos de libros
//                                Collectors.toList())
//                ));
//        pauseExecutionOp3(2000);
//        System.out.println();
//        System.out.println("Autores vivos en el año " + fecha + " y sus libros:");
//        System.out.println("============================================");
//        for (String autor : autoresConLibros.keySet()) {
//            Optional<DatosAutor> autorEncontrado = datosAutor.resultados().stream()
//                    .flatMap(libro -> libro.autorList().stream())
//                    .filter(autorItem -> autorItem.nombreAutor().equals(autor))
//                    .findFirst();
//            System.out.println("Autor: " + autor);
//            autorEncontrado.ifPresent(a -> {
//                String fechaNacimiento = a.FechaNacimiento() != null ? a.FechaNacimiento() : "Desconocida";
//                String fechaMuerte = a.FechaDeMuerte() != null ? a.FechaDeMuerte() : "Desconocida";
//                System.out.println("Fecha de Nacimiento: " + fechaNacimiento);
//                System.out.println("Fecha de Muerte: " + fechaMuerte);
//            });
//            List<String> libros = autoresConLibros.get(autor);
//            if (libros != null && !libros.isEmpty()) {
//                System.out.println("Libros: " + String.join(", ", libros));
//            } else {
//                System.out.println("No hay libros registrados para este autor.");
//            }
//            System.out.println("============================================");
//        }

	}

	
	private void verLibrosporLengua() {

		List<Object[]> lenguas = booksRepository.findDistinctLanguages();
		if (lenguas.size() != 0) {
			System.out.println("Lista de idiomas y numero de libros Registrados");
		    for (int i = 0; i < lenguas.size(); i++) {
		        String idioma = (String) lenguas.get(i)[0];
		        Long numeroLibros = (Long) lenguas.get(i)[1];
		        System.out.printf("[ %d.] Idioma: %s --> Número de Libros : %d%n", i + 1, idioma.toUpperCase(), numeroLibros);
		    }
        System.out.println("Seleccione un idioma, ingresando el numero de referencia:");
        int opcion = teclado.nextInt();
        teclado.nextLine();
        if (opcion > 0 && opcion <= lenguas.size()) {
        	String idiomaSeleccionado = (String) lenguas.get(opcion - 1)[0];
        	List<BookDTO> books = booksRepository.findBooksByLanguage(idiomaSeleccionado);
        	System.out.println("------- LISTA DE LIBROS X IDIOMA -------");
    	    for (BookDTO book : books) {
    	        System.out.println("Título: " + book.getTitle());
    	        System.out.println("Autores: " + String.join(", ", book.getAuthors()));
    	        System.out.println("Idiomas: " + String.join(", ", book.getLanguages()));
    	        System.out.println("Número de Descargas: " + book.getDownloadCount());
    	        System.out.println("---------------------------------");
    	        pauseExecution(2000);
    	    }
        } else {
            System.out.println("Por favor, ingrese un número válido.");
        }
	}else {
		System.out.println("No existen libros Registrados en la BD.");
		return;
	}
}

}
