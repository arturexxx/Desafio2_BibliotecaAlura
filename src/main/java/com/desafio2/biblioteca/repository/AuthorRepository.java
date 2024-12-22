package com.desafio2.biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.desafio2.biblioteca.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

	@Query(
		    value = """
			select a.birth_year, '|' || a."name" || '|' AS name, a.death_year, STRING_AGG(b.title, ', ') as title from authors a 
			INNER join book_author ba  on a.id = ba.author_id
			inner join books b on b.id = ba.book_id 
			GROUP BY a."name", a.birth_year, a.death_year
		    """,
		    nativeQuery = true
		)
		List<Object[]> findAuthorsWithBooks();
		
		@Query(
			    value = """
			        SELECT a.birth_year, '|' || a."name" || '|' AS name, a.death_year, 
			               STRING_AGG(b.title, ', ') AS title
			        FROM authors a
			        INNER JOIN book_author ba ON a.id = ba.author_id
			        INNER JOIN books b ON b.id = ba.book_id
			        WHERE UPPER(a."name") LIKE UPPER(:namex)
			        GROUP BY a.birth_year, a."name", a.death_year;
			    """,
			    nativeQuery = true
			)
			List<Object[]> findAuthorsNameBooks(@Param("namex") String namex);

			
		
		@Query(
			    value = """
			        SELECT a.birth_year, '|' || a."name" || '|' AS name, a.death_year, 
			               STRING_AGG(b.title, ', ') AS title 
			        FROM authors a
			        INNER JOIN book_author ba ON a.id = ba.author_id
			        INNER JOIN books b ON b.id = ba.book_id
			        WHERE a.birth_year <= :date 
			          AND (a.death_year IS NULL OR a.death_year > :date)
			        GROUP BY a."name", a.birth_year, a.death_year
			    """,
			    nativeQuery = true
			)
		List<Object[]> findLivingAuthorsByDate(Integer date);


}
