package pkg.demo.modal;

import lombok.Data;

@Data
public class TCountries {
	
	private Integer id;

	private Integer cfgdbId;

	private String commonName;

	private String twoLetter;

	private String threeLetter;

	private String teleCode;
}