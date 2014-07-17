/*
 * This class is part of the book "iText in Action - 2nd Edition"
 * written by Bruno Lowagie (ISBN: 9781935182610)
 * For more info, go to: http://itextpdf.com/examples/
 * This example only works with the AGPL version of iText.
 */

package columntext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import utils.ConnectionFactory;
import utils.FilmFonts;
import utils.PojoFactory;
import utils.PojoToElementFactory;
import beans.Country;
import beans.Director;
import beans.Movie;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class MovieColumns1 {

	/** The resulting PDF file. */
	public static final String RESULT = "movie_columns1.pdf";

	/** Definition of two columns */
	public static final float[][] COLUMNS = { { 36, 36, 296, 806 },
			{ 299, 36, 559, 806 } };

	/**
	 * Creates a PDF with information about the movies
	 * 
	 * @param filename
	 *            the name of the PDF file that will be created.
	 * @throws DocumentException
	 * @throws IOException
	 * @throws SQLException
	 */
	public void createPdf(String filename) throws Exception {
		// Create a database connection
		Connection connection = ConnectionFactory.getConnection();
		// step 1
		Document document = new Document();
		// step 2
		PdfWriter writer = PdfWriter.getInstance(document,
				new FileOutputStream(filename));
		// step 3
		document.open();
		// step 4
		List<Movie> movies = PojoFactory.getMovies(connection);
		ColumnText ct = new ColumnText(writer.getDirectContent());
		for (Movie movie : movies) {
			ct.addText(createMovieInformation(movie));
			ct.addText(Chunk.NEWLINE);
		}
		ct.setAlignment(Element.ALIGN_JUSTIFIED);
		ct.setExtraParagraphSpace(6);
		ct.setLeading(0, 1.2f);
		ct.setFollowingIndent(27);
		int linesWritten = 0;
		int column = 0;
		int status = ColumnText.START_COLUMN;
		while (ColumnText.hasMoreText(status)) {
			column = Math.abs(column - 1);
			if (column == 0)
				document.newPage();
			ct.setSimpleColumn(COLUMNS[column][0], COLUMNS[column][1],
					COLUMNS[column][2], COLUMNS[column][3]);
			ct.setYLine(COLUMNS[column][3]);
			status = ct.go();
			linesWritten += ct.getLinesWritten();
		}

		ct.addText(new Phrase("Lines written: " + linesWritten));
		ct.go();
		// step 5
		document.close();
		// Close the database connection
		connection.close();
	}

	/**
	 * Creates a Phrase containing information about a movie.
	 * 
	 * @param movie
	 *            the movie for which you want to create a Paragraph
	 */
	public Phrase createMovieInformation(Movie movie) {
		Phrase p = new Phrase();
		p.setFont(FilmFonts.NORMAL);
		p.add(new Phrase("Title: ", FilmFonts.BOLDITALIC));
		p.add(PojoToElementFactory.getMovieTitlePhrase(movie));
		p.add(" ");
		if (movie.getOriginalTitle() != null) {
			p.add(new Phrase("Original title: ", FilmFonts.BOLDITALIC));
			p.add(PojoToElementFactory.getOriginalTitlePhrase(movie));
			p.add(" ");
		}
		p.add(new Phrase("Country: ", FilmFonts.BOLDITALIC));
		for (Country country : movie.getCountries()) {
			p.add(PojoToElementFactory.getCountryPhrase(country));
			p.add(" ");
		}
		p.add(new Phrase("Director: ", FilmFonts.BOLDITALIC));
		for (Director director : movie.getDirectors()) {
			p.add(PojoToElementFactory.getDirectorPhrase(director));
			p.add(" ");
		}
		p.add(new Chunk("Year: ", FilmFonts.BOLDITALIC));
		p.add(new Chunk(String.valueOf(movie.getYear()), FilmFonts.NORMAL));
		p.add(new Chunk(" Duration: ", FilmFonts.BOLDITALIC));
		p.add(new Chunk(String.valueOf(movie.getDuration()), FilmFonts.NORMAL));
		p.add(new Chunk(" minutes", FilmFonts.NORMAL));
		p.add(new LineSeparator(0.3f, 100, null, Element.ALIGN_CENTER, -2));
		return p;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            no arguments needed
	 * @throws DocumentException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception {
		new MovieColumns1().createPdf(RESULT);
	}
}