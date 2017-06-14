package pkg.demo.modal;

public class TCountries {
    private Integer id;

    private Integer cfgdbId;

    private String commonName;

    private String twoLetter;

    private String threeLetter;

    private String teleCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCfgdbId() {
        return cfgdbId;
    }

    public void setCfgdbId(Integer cfgdbId) {
        this.cfgdbId = cfgdbId;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getTwoLetter() {
        return twoLetter;
    }

    public void setTwoLetter(String twoLetter) {
        this.twoLetter = twoLetter;
    }

    public String getThreeLetter() {
        return threeLetter;
    }

    public void setThreeLetter(String threeLetter) {
        this.threeLetter = threeLetter;
    }

    public String getTeleCode() {
        return teleCode;
    }

    public void setTeleCode(String teleCode) {
        this.teleCode = teleCode;
    }
}